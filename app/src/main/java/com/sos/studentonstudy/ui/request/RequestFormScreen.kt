package com.sos.studentonstudy.ui.request

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sos.studentonstudy.data.local.EstimatedTime
import com.sos.studentonstudy.data.local.RequestTags
import com.sos.studentonstudy.ui.BackTopBar
import com.sos.studentonstudy.ui.ConfirmDialog
import com.sos.studentonstudy.ui.FieldError
import com.sos.studentonstudy.ui.SosButton
import com.sos.studentonstudy.ui.SosField
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosPurple
import com.sos.studentonstudy.ui.SosStroke
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.SosTextField
import com.sos.studentonstudy.ui.SosViewModelFactory
import com.sos.studentonstudy.ui.TagChip
import com.sos.studentonstudy.ui.formatDate
import com.sos.studentonstudy.ui.formatIdr

/** "Add Request Details" screen from Figma, used for both create and edit. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RequestFormScreen(
    onDone: () -> Unit,
    viewModel: RequestFormViewModel = viewModel(factory = SosViewModelFactory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var timeMenuOpen by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onAttachmentChange(context.displayName(it)) }
    }
    LaunchedEffect(state.finished) { if (state.finished) onDone() }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        BackTopBar(title = if (state.isEdit) "Edit Request" else "Add Request Details", onBack = onDone) {
            if (state.isEdit) {
                IconButton(onClick = viewModel::requestDelete) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete request", tint = SosText)
                }
            }
        }
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = SosPurple) }
            return@Column
        }
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 8.dp)
        ) {
            SosTextField(
                label = "Title",
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = "Input title",
                error = state.titleError,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            Spacer(Modifier.height(20.dp))
            SosTextField(
                label = "Request details",
                value = state.details,
                onValueChange = viewModel::onDetailsChange,
                placeholder = "Input text",
                error = state.detailsError,
                singleLine = false,
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            Spacer(Modifier.height(12.dp))
            val attachment = state.attachmentName
            if (attachment == null) {
                GrayBox(onClick = { pickFile.launch("*/*") }) {
                    Text("+ Add Attachment", color = SosText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                GrayBox(onClick = { pickFile.launch("*/*") }) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 14.dp)) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = SosText)
                        Spacer(Modifier.width(12.dp))
                        Text(attachment, color = SosText, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Remove attachment",
                            tint = SosMuted,
                            modifier = Modifier.clickable { viewModel.onAttachmentChange(null) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("Estimated time", color = SosText, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Box {
                GrayBox(onClick = { timeMenuOpen = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp)) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = SosText, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(EstimatedTime.label(state.estimatedTime), color = SosText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose estimated time", tint = SosText)
                    }
                }
                DropdownMenu(expanded = timeMenuOpen, onDismissRequest = { timeMenuOpen = false }) {
                    EstimatedTime.all.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(EstimatedTime.label(option)) },
                            onClick = {
                                viewModel.onEstimatedTimeChange(option)
                                timeMenuOpen = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("Deadline", color = SosText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DeadlineOption.entries.forEach { option ->
                    val selected = state.deadlineOption == option
                    val label = if (option == DeadlineOption.Custom && selected && state.deadline != null) formatDate(state.deadline!!) else option.label
                    TagChip(
                        text = label,
                        selected = selected,
                        fontSize = 14,
                        onClick = {
                            if (option == DeadlineOption.Custom) showDatePicker = true else viewModel.onDeadlineOption(option)
                        }
                    )
                }
            }
            state.deadlineError?.let { FieldError(it) }

            Spacer(Modifier.height(28.dp))
            Text("Tags", color = SosText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RequestTags.all.forEach { tag ->
                    TagChip(text = tag, selected = tag in state.tags, fontSize = 14, onClick = { viewModel.toggleTag(tag) })
                }
            }
            state.tagsError?.let { FieldError(it) }

            Spacer(Modifier.height(28.dp))
            Text("Budget", color = SosText, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .background(SosField, RoundedCornerShape(8.dp))
                    .border(1.dp, SosStroke, RoundedCornerShape(8.dp))
                    .padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formatIdr(state.budget), color = SosText, fontSize = 14.sp, modifier = Modifier.width(100.dp))
                Column {
                    Icon(
                        Icons.Filled.ArrowDropUp,
                        contentDescription = "Increase budget",
                        tint = SosText,
                        modifier = Modifier.size(24.dp).clickable { viewModel.changeBudget(up = true) }
                    )
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Decrease budget",
                        tint = SosText,
                        modifier = Modifier.size(24.dp).clickable { viewModel.changeBudget(up = false) }
                    )
                }
            }
            state.budgetError?.let { FieldError(it) }
            Spacer(Modifier.height(24.dp))
        }
        SosButton(
            text = if (state.isEdit) "Save" else "Send",
            onClick = viewModel::save,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 20.dp)
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.deadline)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(viewModel::onCustomDeadline)
                    showDatePicker = false
                }) { Text("OK", color = SosPurple) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = SosMuted) }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (state.confirmDelete) {
        ConfirmDialog(
            title = "Delete request?",
            message = "\"${state.title}\" will be permanently removed.",
            confirmText = "Delete",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
private fun GrayBox(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(SosField, RoundedCornerShape(8.dp))
            .border(1.dp, SosStroke, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

private fun android.content.Context.displayName(uri: Uri): String =
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    } ?: uri.lastPathSegment ?: "attachment"
