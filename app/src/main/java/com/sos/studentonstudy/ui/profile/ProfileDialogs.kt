package com.sos.studentonstudy.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import com.sos.studentonstudy.ui.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.sos.studentonstudy.data.AppLanguage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosPurple
import com.sos.studentonstudy.ui.SosSurface
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.SosTextField
import com.sos.studentonstudy.ui.TagChip
import com.sos.studentonstudy.ui.formatIdr

private val AmountPresets = listOf(50_000L, 100_000L, 250_000L)

/** Amount prompt for Withdraw and Buy Credits. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BalanceAmountDialog(
    dialog: BalanceDialog,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val note = when (dialog.action) {
        BalanceAction.Withdraw -> "Moves money out of your SOS balance. This is a demo, nothing is sent to a bank."
        BalanceAction.BuyCredits -> "Adds credits to your SOS balance. This is a demo, no real payment is made."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialog.action.title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(note, color = SosMuted, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AmountPresets.forEach { preset ->
                        TagChip(
                            text = formatIdr(preset),
                            selected = dialog.amount == preset.toString(),
                            fontSize = 13,
                            onClick = { onAmountChange(preset.toString()) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                SosTextField(
                    label = "Amount (IDR)",
                    value = dialog.amount,
                    onValueChange = onAmountChange,
                    placeholder = "e.g. 50000",
                    error = dialog.error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(dialog.action.confirm, color = SosPurple, fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = SosMuted) } },
        containerColor = SosSurface
    )
}

/** Language and Display pickers; the choice applies (and is saved) immediately. */
@Composable
internal fun SettingDialogContent(
    setting: SettingDialog,
    darkMode: Boolean,
    language: AppLanguage,
    onDarkMode: (Boolean) -> Unit,
    onLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val title: String
    val options: List<Triple<String, Boolean, () -> Unit>>
    when (setting) {
        SettingDialog.Language -> {
            title = "Language"
            options = AppLanguage.entries.map { Triple(it.label, it == language) { onLanguage(it) } }
        }
        SettingDialog.Display -> {
            title = "Display"
            options = listOf(
                Triple("Light mode", !darkMode) { onDarkMode(false) },
                Triple("Dark mode", darkMode) { onDarkMode(true) }
            )
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEach { (label, selected, onSelect) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, color = SosText, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        if (selected) Icon(Icons.Filled.Check, contentDescription = "Selected", tint = SosPurple)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = SosPurple, fontWeight = FontWeight.SemiBold) } },
        containerColor = SosSurface
    )
}
