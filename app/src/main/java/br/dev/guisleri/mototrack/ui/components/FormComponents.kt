package br.dev.guisleri.mototrack.ui.components

import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted

internal val WarmFieldShape = RoundedCornerShape(16.dp)

@Composable
internal fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.6.sp,
            color = WarmEmber,
            modifier = Modifier.padding(start = 6.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(WarmCard)
                .border(
                    border = BorderStroke(1.dp, WarmHairline),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
internal fun ChoicePill(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    val shape = RoundedCornerShape(50)
    val contentColor = if (selected) Color.White else WarmInkSoft

    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) WarmInk else WarmCanvas)
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) WarmInk else WarmHairline
                ),
                shape = shape
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(
                horizontal = 14.dp,
                vertical = 9.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (selected) Color.White else WarmEmber
            )

            Spacer(modifier = Modifier.width(7.dp))
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1
        )
    }
}

@Composable
internal fun WarmTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    error: String?,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardActions = keyboardActions,
        trailingIcon = trailingIcon,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        },
        suffix = suffix?.let { text ->
            {
                Text(
                    text = text,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        isError = error != null,
        supportingText = error?.let { message ->
            { Text(text = message) }
        },
        keyboardOptions = keyboardOptions,
        shape = WarmFieldShape,
        colors = warmFieldColors()
    )
}

@Composable
internal fun warmFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = WarmCard,
    unfocusedContainerColor = WarmCanvas,
    disabledContainerColor = WarmCanvas,
    errorContainerColor = WarmCanvas,
    focusedBorderColor = WarmEmber,
    unfocusedBorderColor = WarmHairline,
    disabledBorderColor = WarmHairline,
    focusedLeadingIconColor = WarmEmber,
    unfocusedLeadingIconColor = WarmMuted,
    focusedLabelColor = WarmEmber,
    unfocusedLabelColor = WarmMuted,
    focusedTextColor = WarmInk,
    unfocusedTextColor = WarmInk,
    disabledTextColor = WarmInk.copy(alpha = 0.6f),
    cursorColor = WarmEmber
)
