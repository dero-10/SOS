-- SOS — Student on Study: Supabase schema
-- Run once in the Supabase dashboard → SQL Editor → New query → paste → Run.
-- Safe to re-run: every statement is idempotent.
--
-- Model: every person is a row in `profiles` (linked 1:1 to auth.users). A helper is just a
-- profile with is_helper = true, so helpers and students can chat, request and reply as real users.

-- ---------------------------------------------------------------------------
-- Profiles
-- ---------------------------------------------------------------------------
create table if not exists public.profiles (
    id            uuid primary key references auth.users (id) on delete cascade,
    name          text not null default '',
    email         text not null default '',
    major         text not null default '',
    university    text not null default '',
    linkedin_url  text not null default '',
    balance       bigint not null default 250000 check (balance >= 0),
    -- Helper fields (Explore → Helper tab, helper profile)
    is_helper     boolean not null default false,
    skills        text[] not null default '{}',
    portfolio     text not null default '',
    last_seen_at  timestamptz not null default now(),
    created_at    timestamptz not null default now()
);

-- Creates the profile row when someone signs up. The app sends the name as user metadata.
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
    insert into public.profiles (id, name, email)
    values (new.id, coalesce(new.raw_user_meta_data ->> 'name', ''), coalesce(new.email, ''))
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- ---------------------------------------------------------------------------
-- Requests (the CRUD module)
-- ---------------------------------------------------------------------------
create table if not exists public.requests (
    id               bigint generated always as identity primary key,
    user_id          uuid not null references public.profiles (id) on delete cascade,
    -- Set when the request was sent privately to one helper ("Chat Helper"); hidden from the feed.
    helper_id        uuid references public.profiles (id) on delete set null,
    title            text not null check (char_length(title) >= 5),
    details          text not null check (char_length(details) >= 10),
    tags             text[] not null default '{}',
    estimated_time   smallint not null default 0 check (estimated_time between 0 and 2),
    deadline         date not null,
    budget           bigint not null default 0 check (budget >= 0),
    attachment_name  text,
    image_path       text,   -- object path in the `request-images` storage bucket
    status           text not null default 'open' check (status in ('open', 'in_progress', 'done', 'cancelled')),
    created_at       timestamptz not null default now()
);
create index if not exists requests_user_idx on public.requests (user_id);
create index if not exists requests_helper_idx on public.requests (helper_id);
create index if not exists requests_feed_idx on public.requests (created_at desc) where helper_id is null;

-- ---------------------------------------------------------------------------
-- Replies on a request
-- ---------------------------------------------------------------------------
create table if not exists public.replies (
    id          bigint generated always as identity primary key,
    request_id  bigint not null references public.requests (id) on delete cascade,
    author_id   uuid not null references public.profiles (id) on delete cascade,
    message     text not null check (char_length(message) > 0),
    link        text,
    created_at  timestamptz not null default now()
);
create index if not exists replies_request_idx on public.replies (request_id);

-- ---------------------------------------------------------------------------
-- Chat messages between two profiles
-- ---------------------------------------------------------------------------
create table if not exists public.messages (
    id               bigint generated always as identity primary key,
    sender_id        uuid not null references public.profiles (id) on delete cascade,
    receiver_id      uuid not null references public.profiles (id) on delete cascade,
    text             text not null default '',
    attachment_name  text,
    request_id       bigint references public.requests (id) on delete set null,
    created_at       timestamptz not null default now(),
    check (sender_id <> receiver_id)
);
create index if not exists messages_pair_idx
    on public.messages (least(sender_id, receiver_id), greatest(sender_id, receiver_id), created_at);

-- ---------------------------------------------------------------------------
-- Reviews (replaces the seeded help_history / rating numbers)
-- ---------------------------------------------------------------------------
create table if not exists public.reviews (
    id           bigint generated always as identity primary key,
    request_id   bigint not null references public.requests (id) on delete cascade,
    helper_id    uuid not null references public.profiles (id) on delete cascade,
    reviewer_id  uuid not null references public.profiles (id) on delete cascade,
    stars        smallint not null check (stars between 1 and 5),
    comment      text not null default '',
    created_at   timestamptz not null default now(),
    unique (request_id, reviewer_id)
);
create index if not exists reviews_helper_idx on public.reviews (helper_id);

-- Helper stats computed from real data instead of stored numbers.
create or replace view public.helper_stats with (security_invoker = true) as
select p.id                                         as helper_id,
       coalesce(round(avg(r.stars)::numeric, 1), 0) as rating,
       count(r.id)                                  as reviews,
       (select count(*) from public.requests q where q.helper_id = p.id and q.status = 'done') as helps,
       (select count(*) from public.requests q where q.helper_id = p.id)                       as requests
from public.profiles p
left join public.reviews r on r.helper_id = p.id
where p.is_helper
group by p.id;

-- ---------------------------------------------------------------------------
-- Balance changes go through this function only (clients cannot write `balance` directly).
-- Top up / withdraw are still simulated: there is no payment gateway yet.
-- Returns the new balance, or null when the balance would go below zero.
-- ---------------------------------------------------------------------------
create or replace function public.adjust_balance(delta bigint)
returns bigint language plpgsql security definer set search_path = public as $$
declare
    new_balance bigint;
begin
    update public.profiles
       set balance = balance + delta
     where id = auth.uid() and balance + delta >= 0
    returning balance into new_balance;
    return new_balance;
end;
$$;
revoke all on function public.adjust_balance(bigint) from public, anon;
grant execute on function public.adjust_balance(bigint) to authenticated;

-- ---------------------------------------------------------------------------
-- Row Level Security
-- ---------------------------------------------------------------------------
alter table public.profiles enable row level security;
alter table public.requests enable row level security;
alter table public.replies  enable row level security;
alter table public.messages enable row level security;
alter table public.reviews  enable row level security;

-- Profiles: any signed-in user can read (needed for author names, helper list); only you can edit yours.
drop policy if exists "profiles readable" on public.profiles;
create policy "profiles readable" on public.profiles for select to authenticated using (true);
drop policy if exists "profiles own update" on public.profiles;
create policy "profiles own update" on public.profiles for update to authenticated
    using (id = auth.uid()) with check (id = auth.uid());
-- Column-level guard: balance and email are not client-writable.
revoke update on public.profiles from authenticated;
grant update (name, major, university, linkedin_url, is_helper, skills, portfolio, last_seen_at)
    on public.profiles to authenticated;

-- Requests: public ones are visible to everyone; private ones only to the author and that helper.
drop policy if exists "requests readable" on public.requests;
create policy "requests readable" on public.requests for select to authenticated
    using (helper_id is null or user_id = auth.uid() or helper_id = auth.uid());
drop policy if exists "requests own insert" on public.requests;
create policy "requests own insert" on public.requests for insert to authenticated
    with check (user_id = auth.uid());
drop policy if exists "requests own update" on public.requests;
create policy "requests own update" on public.requests for update to authenticated
    using (user_id = auth.uid()) with check (user_id = auth.uid());
drop policy if exists "requests own delete" on public.requests;
create policy "requests own delete" on public.requests for delete to authenticated
    using (user_id = auth.uid());

-- Replies: readable when the request is readable; written as yourself.
drop policy if exists "replies readable" on public.replies;
create policy "replies readable" on public.replies for select to authenticated
    using (exists (select 1 from public.requests q where q.id = replies.request_id));
drop policy if exists "replies own insert" on public.replies;
create policy "replies own insert" on public.replies for insert to authenticated
    with check (author_id = auth.uid() and exists (select 1 from public.requests q where q.id = replies.request_id));
drop policy if exists "replies own delete" on public.replies;
create policy "replies own delete" on public.replies for delete to authenticated
    using (author_id = auth.uid());

-- Messages: only the two people in the conversation can read; you can only send as yourself.
drop policy if exists "messages participants" on public.messages;
create policy "messages participants" on public.messages for select to authenticated
    using (sender_id = auth.uid() or receiver_id = auth.uid());
drop policy if exists "messages own insert" on public.messages;
create policy "messages own insert" on public.messages for insert to authenticated
    with check (sender_id = auth.uid());

-- Reviews: readable by all; only the request's author can review its helper.
drop policy if exists "reviews readable" on public.reviews;
create policy "reviews readable" on public.reviews for select to authenticated using (true);
drop policy if exists "reviews by requester" on public.reviews;
create policy "reviews by requester" on public.reviews for insert to authenticated
    with check (reviewer_id = auth.uid()
                and exists (select 1 from public.requests q
                            where q.id = reviews.request_id and q.user_id = auth.uid() and q.helper_id = reviews.helper_id));

-- ---------------------------------------------------------------------------
-- Realtime for chat (the app subscribes to new messages)
-- ---------------------------------------------------------------------------
do $$
begin
    if not exists (select 1 from pg_publication_tables
                   where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = 'messages') then
        alter publication supabase_realtime add table public.messages;
    end if;
end $$;

-- ---------------------------------------------------------------------------
-- Storage: request pictures. Files are stored as <user id>/<file name>.
-- ---------------------------------------------------------------------------
insert into storage.buckets (id, name, public)
values ('request-images', 'request-images', true)
on conflict (id) do nothing;

drop policy if exists "request images upload own folder" on storage.objects;
create policy "request images upload own folder" on storage.objects for insert to authenticated
    with check (bucket_id = 'request-images' and (storage.foldername(name))[1] = auth.uid()::text);
drop policy if exists "request images delete own folder" on storage.objects;
create policy "request images delete own folder" on storage.objects for delete to authenticated
    using (bucket_id = 'request-images' and (storage.foldername(name))[1] = auth.uid()::text);
