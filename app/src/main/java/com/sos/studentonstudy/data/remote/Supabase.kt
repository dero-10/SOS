package com.sos.studentonstudy.data.remote

import com.sos.studentonstudy.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Builds the Supabase client from `supabase.url` / `supabase.anonKey` in local.properties.
 * Returns null when they are not set, and the app then runs fully offline on Room (the original behaviour).
 */
object SupabaseProvider {
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    fun createOrNull(): SupabaseClient? {
        if (!isConfigured) return null
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}

/** A row of `public.profiles` (see supabase/schema.sql). */
@Serializable
data class ProfileDto(
    val id: String,
    val name: String = "",
    val email: String = "",
    val major: String = "",
    val university: String = "",
    @SerialName("linkedin_url") val linkedinUrl: String = "",
    val balance: Long = 0
)

/** Columns the app is allowed to change on its own profile. */
@Serializable
data class ProfileUpdateDto(
    val name: String,
    val major: String,
    val university: String,
    @SerialName("linkedin_url") val linkedinUrl: String
)
