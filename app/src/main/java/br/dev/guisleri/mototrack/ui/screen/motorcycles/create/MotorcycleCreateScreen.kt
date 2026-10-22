package br.dev.guisleri.mototrack.ui.screen.motorcycles.create

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleType
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.ui.components.ChoicePill
import br.dev.guisleri.mototrack.ui.components.CircleActionButton
import br.dev.guisleri.mototrack.ui.components.FormSection
import br.dev.guisleri.mototrack.ui.components.MotorcycleColorOptions
import br.dev.guisleri.mototrack.ui.components.RoadDecoration
import br.dev.guisleri.mototrack.ui.components.WarmTextField
import br.dev.guisleri.mototrack.ui.components.motorcycleColor
import br.dev.guisleri.mototrack.ui.theme.GraphiteHeroBrush
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberOnDark
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted

private val PopularBrands = listOf(
    "Honda", "Yamaha", "Kawasaki", "Suzuki", "BMW", "Triumph",
    "Harley-Davidson", "Royal Enfield", "KTM", "Ducati", "Dafra", "Shineray"
)

@Composable
fun MotorcycleCreateScreen(
    uiState: MotorcycleCreateUiState,
    onBackClick: () -> Unit,
    onTypeSelected: (MotorcycleType) -> Unit,
    onBrandChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onEngineCapacityChange: (String) -> Unit,
    onColorSelected: (String) -> Unit,
    onCustomColorSelected: () -> Unit,
    onCustomColorChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 12.dp,
                    bottom = 24.dp
                ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            CircleActionButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Voltar",
                onClick = onBackClick
            )

            MotorcyclePreviewCard(uiState = uiState)

            TypeSection(
                uiState = uiState,
                onTypeSelected = onTypeSelected
            )

            MotorcycleSection(
                uiState = uiState,
                onBrandChange = onBrandChange,
                onModelChange = onModelChange
            )

            DetailsSection(
                uiState = uiState,
                onYearChange = onYearChange,
                onEngineCapacityChange = onEngineCapacityChange
            )

            ColorSection(
                uiState = uiState,
                onColorSelected = onColorSelected,
                onCustomColorSelected = onCustomColorSelected,
                onCustomColorChange = onCustomColorChange
            )
        }

        SubmitBar(
            uiState = uiState,
            onSubmitClick = onSubmitClick
        )
    }
}

// ---------------------------------------------------------------------------
// Live preview
// ---------------------------------------------------------------------------

/** The header mirrors what is typed, so the form feels like building the bike. */
@Composable
private fun MotorcyclePreviewCard(
    uiState: MotorcycleCreateUiState
) {
    val brand = uiState.brand.trim()
    val model = uiState.model.trim()
    val color = uiState.color.trim()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(GraphiteHeroBrush)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        WarmEmber.copy(alpha = 0.36f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.1f, size.height),
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = Offset(size.width * 0.1f, size.height)
            )
        }

        RoadDecoration(
            modifier = Modifier.matchParentSize(),
            color = Color.White
        )

        Icon(
            imageVector = Icons.Outlined.TwoWheeler,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 30.dp)
                .size(170.dp),
            tint = Color.White.copy(alpha = 0.08f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "NOVA MOTO",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = WarmEmberOnDark
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = brand.ifEmpty { "Sua marca" }.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp,
                color = if (brand.isEmpty()) {
                    Color.White.copy(alpha = 0.4f)
                } else {
                    WarmEmberOnDark
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = model.ifEmpty { "Seu modelo" },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (model.isEmpty()) {
                    Color.White.copy(alpha = 0.4f)
                } else {
                    Color.White
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                uiState.type?.let { type ->
                    PreviewChip(text = type.label)
                }

                if (uiState.year.isNotEmpty()) {
                    PreviewChip(text = uiState.year)
                }

                if (uiState.engineCapacity.isNotEmpty()) {
                    PreviewChip(text = "${uiState.engineCapacity} cc")
                }

                if (color.isNotEmpty()) {
                    PreviewChip(text = color, swatch = motorcycleColor(color))
                }
            }

            if (
                uiState.type == null &&
                uiState.year.isEmpty() &&
                uiState.engineCapacity.isEmpty() &&
                color.isEmpty()
            ) {
                Text(
                    text = "Preencha os dados abaixo e acompanhe aqui.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun PreviewChip(
    text: String,
    swatch: Color? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        swatch?.let { color ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            )

            Spacer(modifier = Modifier.width(7.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// Sections
// ---------------------------------------------------------------------------

@Composable
private fun TypeSection(
    uiState: MotorcycleCreateUiState,
    onTypeSelected: (MotorcycleType) -> Unit
) {
    FormSection(title = "TIPO") {
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MotorcycleType.entries.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { type ->
                        TypeTile(
                            type = type,
                            selected = uiState.type == type,
                            enabled = !uiState.isSubmitting,
                            onClick = { onTypeSelected(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        uiState.typeError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun TypeTile(
    type: MotorcycleType,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (type) {
        MotorcycleType.STREET -> Icons.Outlined.LocationCity
        MotorcycleType.NAKED -> Icons.Outlined.Bolt
        MotorcycleType.SPORT -> Icons.Outlined.Speed
        MotorcycleType.CUSTOM -> Icons.Outlined.Stars
        MotorcycleType.TRAIL -> Icons.Outlined.Terrain
        MotorcycleType.BIG_TRAIL -> Icons.Outlined.Explore
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) WarmInk else WarmCanvas)
            .border(
                border = BorderStroke(1.dp, if (selected) WarmInk else WarmHairline),
                shape = RoundedCornerShape(18.dp)
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (selected) WarmEmberOnDark else WarmEmber
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = type.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) Color.White else WarmInk,
                maxLines = 1
            )

            Text(
                text = type.hint,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White.copy(alpha = 0.7f) else WarmMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MotorcycleSection(
    uiState: MotorcycleCreateUiState,
    onBrandChange: (String) -> Unit,
    onModelChange: (String) -> Unit
) {
    FormSection(title = "MOTO") {
        WarmTextField(
            value = uiState.brand,
            onValueChange = onBrandChange,
            label = "Marca",
            leadingIcon = Icons.Outlined.TwoWheeler,
            error = uiState.brandError,
            enabled = !uiState.isSubmitting,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PopularBrands.forEach { brand ->
                ChoicePill(
                    label = brand,
                    selected = uiState.brand.trim().equals(brand, ignoreCase = true),
                    enabled = !uiState.isSubmitting,
                    onClick = { onBrandChange(brand) }
                )
            }
        }

        WarmTextField(
            value = uiState.model,
            onValueChange = onModelChange,
            label = "Modelo",
            leadingIcon = Icons.Outlined.Edit,
            error = uiState.modelError,
            enabled = !uiState.isSubmitting,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            )
        )
    }
}

@Composable
private fun DetailsSection(
    uiState: MotorcycleCreateUiState,
    onYearChange: (String) -> Unit,
    onEngineCapacityChange: (String) -> Unit
) {
    FormSection(title = "DETALHES") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            WarmTextField(
                value = uiState.year,
                onValueChange = onYearChange,
                label = "Ano",
                leadingIcon = Icons.Outlined.CalendarMonth,
                error = uiState.yearError,
                enabled = !uiState.isSubmitting,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.weight(1f)
            )

            WarmTextField(
                value = uiState.engineCapacity,
                onValueChange = onEngineCapacityChange,
                label = "Cilindrada",
                leadingIcon = Icons.Outlined.Speed,
                suffix = "cc",
                error = uiState.engineCapacityError,
                enabled = !uiState.isSubmitting,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSection(
    uiState: MotorcycleCreateUiState,
    onColorSelected: (String) -> Unit,
    onCustomColorSelected: () -> Unit,
    onCustomColorChange: (String) -> Unit
) {
    FormSection(title = "COR") {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MotorcycleColorOptions.forEach { (name, color) ->
                ColorSwatch(
                    name = name,
                    color = color,
                    selected = !uiState.isCustomColor && uiState.color == name,
                    enabled = !uiState.isSubmitting,
                    onClick = { onColorSelected(name) }
                )
            }

            ChoicePill(
                label = "Outra",
                selected = uiState.isCustomColor,
                enabled = !uiState.isSubmitting,
                onClick = onCustomColorSelected
            )
        }

        if (uiState.isCustomColor) {
            WarmTextField(
                value = uiState.color,
                onValueChange = onCustomColorChange,
                label = "Qual é a cor?",
                leadingIcon = Icons.Outlined.Palette,
                error = null,
                enabled = !uiState.isSubmitting,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                )
            )
        }

        uiState.colorError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    name: String,
    color: Color,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val isLight = color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f > 0.7f

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                border = if (selected) {
                    BorderStroke(3.dp, WarmEmber)
                } else {
                    BorderStroke(1.dp, WarmHairline)
                },
                shape = CircleShape
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = name,
                modifier = Modifier.size(24.dp),
                tint = if (isLight) WarmInk else Color.White
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Submit bar
// ---------------------------------------------------------------------------

@Composable
private fun SubmitBar(
    uiState: MotorcycleCreateUiState,
    onSubmitClick: () -> Unit
) {
    Surface(
        color = WarmCard,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            uiState.submitErrorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = onSubmitClick,
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 58.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmInk,
                    contentColor = Color.White,
                    disabledContainerColor = WarmInk.copy(alpha = 0.7f),
                    disabledContentColor = Color.White
                )
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Salvando...",
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Cadastrar moto",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
