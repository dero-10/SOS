package com.sos.studentonstudy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SosButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, loading: Boolean = false) {
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = SosPurple)
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Text(text, fontSize = 17.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun SosLogo(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("SOS", color = SosPurple, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
        Text("Student on Study", color = SosMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(text = title, modifier = modifier, color = SosText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
}

/** Light card with a soft shadow, as used for request and helper cards in Figma. */
@Composable
internal fun SosCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = SosCard, shadowElevation = 4.dp, content = content)
    } else {
        Surface(modifier = modifier, shape = shape, color = SosCard, shadowElevation = 4.dp, content = content)
    }
}

@Composable
internal fun SosTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, color = SosText, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            placeholder = { Text(placeholder, color = SosMuted) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = SosMuted) } },
            trailingIcon = trailingIcon,
            isError = error != null,
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SosPurple,
                unfocusedBorderColor = SosStroke,
                focusedContainerColor = SosField,
                unfocusedContainerColor = SosField,
                errorContainerColor = SosField
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        if (error != null) {
            FieldError(error)
        }
    }
}

@Composable
internal fun FieldError(error: String) {
    Text(error, color = SosError, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
}

@Composable
internal fun SosSearchBar(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, placeholder: String = "Search") {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = SosText, fontSize = 14.sp),
        cursorBrush = SolidColor(SosPurple),
        modifier = modifier,
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, color = Color(0xFFBDBDBD), fontSize = 14.sp)
                    inner()
                }
                if (value.isNotEmpty()) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Clear search",
                        tint = SosMuted,
                        modifier = Modifier.size(18.dp).clickable { onValueChange("") }
                    )
                }
            }
        }
    )
}

/** Teal pill used for tags and deadline options. */
@Composable
internal fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    fontSize: Int = 12
) {
    Row(
        modifier = modifier
            .background(if (selected) SosPurple else SosTeal, CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = if (selected) Color.White else SosText, fontSize = fontSize.sp)
        if (onRemove != null) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Rounded.Close, contentDescription = "Remove $text", tint = SosText, modifier = Modifier.size(14.dp).clickable(onClick = onRemove))
        }
    }
}

/** Blue-to-purple gradient circle with the user's initial (Figma profile avatar). */
@Composable
internal fun InitialAvatar(name: String, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .background(Brush.linearGradient(listOf(SosGradientStart, SosGradientEnd)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(name.firstOrNull()?.uppercase().orEmpty(), color = Color.White, fontSize = (size.value * .36f).sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun BackTopBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = SosPurple)
        }
        Spacer(Modifier.width(4.dp))
        Text(title, color = SosText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        actions()
    }
}

@Composable
internal fun ConfirmDialog(title: String, message: String, confirmText: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText, color = SosError, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SosMuted) }
        },
        containerColor = Color.White
    )
}

/** Teal stat tile with a purple number (Figma helper-profile stats). */
@Composable
internal fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = SosTeal, shadowElevation = 3.dp) {
        Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = SosPurple, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(label, color = SosText, fontSize = 12.sp)
        }
    }
}

@Composable
internal fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = SosText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = SosMuted, fontSize = 13.sp)
    }
}

@Composable
internal fun SosBottomBar(content: @Composable RowScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(SosBackground)) {
        HorizontalDivider(color = Color(0xFFE6E6E6))
        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
internal fun BottomItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) SosPurple else SosText
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

