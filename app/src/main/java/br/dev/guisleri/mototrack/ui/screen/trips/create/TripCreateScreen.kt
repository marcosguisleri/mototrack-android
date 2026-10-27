package br.dev.guisleri.mototrack.ui.screen.trips.create

import br.dev.guisleri.mototrack.ui.components.warmFieldColors
import br.dev.guisleri.mototrack.ui.components.WarmTextField
import br.dev.guisleri.mototrack.ui.components.WarmFieldShape
import br.dev.guisleri.mototrack.ui.components.FormSection
import br.dev.guisleri.mototrack.ui.components.ChoicePill
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.TripOrigin
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import br.dev.guisleri.mototrack.ui.components.clearFocusOnTap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.ui.components.CircleActionButton
import br.dev.guisleri.mototrack.data.place.City
import br.dev.guisleri.mototrack.ui.components.RoadDecoration
import br.dev.guisleri.mototrack.ui.components.motorcycleColor
import br.dev.guisleri.mototrack.ui.theme.GraphiteHeroBrush
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberDeep
import br.dev.guisleri.mototrack.ui.theme.WarmEmberOnDark
import br.dev.guisleri.mototrack.ui.theme.WarmEmberTint
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale


private enum class PlaceField { ORIGIN, DESTINATION }

@Composable
fun TripCreateScreen(
    uiState: TripCreateUiState,
    onBackClick: () -> Unit,
    onModeSelected: (TripCreateMode) -> Unit,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onOriginCitySelected: (City) -> Unit,
    onDestinationCitySelected: (City) -> Unit,
    onSwapRouteClick: () -> Unit,
    onDistanceChange: (String) -> Unit,
    onApplyDistanceSuggestionClick: () -> Unit,
    onTripDateSelected: (LocalDate) -> Unit,
    onTerrainSelected: (TripCreateTerrain) -> Unit,
    onMotorcycleSelected: (Long) -> Unit,
    onRetryMotorcyclesClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onRetryTripClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by rememberSaveable {
        mutableStateOf(false)
    }

    var lastFocusedField by rememberSaveable {
        mutableStateOf(PlaceField.ORIGIN)
    }

    val destinationFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .clearFocusOnTap()
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

            if (uiState.isEditing) {
                TripEditHeaderCard(status = uiState.editingStatus)
            } else {
                TripCreateModeCard(
                    selectedMode = uiState.mode,
                    enabled = !uiState.isSubmitting,
                    onModeSelected = onModeSelected
                )
            }

            if (uiState.isLoadingTrip || uiState.loadTripErrorMessage != null) {
                TripLoadStatus(
                    isLoading = uiState.isLoadingTrip,
                    message = uiState.loadTripErrorMessage,
                    onRetryClick = onRetryTripClick
                )
                return@Column
            }

            RouteSection(
                uiState = uiState,
                destinationFocusRequester = destinationFocusRequester,
                onOriginFocused = { lastFocusedField = PlaceField.ORIGIN },
                onDestinationFocused = { lastFocusedField = PlaceField.DESTINATION },
                onOriginChange = onOriginChange,
                onDestinationChange = onDestinationChange,
                onOriginCitySelected = onOriginCitySelected,
                onDestinationCitySelected = onDestinationCitySelected,
                onSwapRouteClick = onSwapRouteClick,
                onDistanceChange = onDistanceChange,
                onApplyDistanceSuggestionClick = onApplyDistanceSuggestionClick,
                onPlaceClick = { place ->
                    val target = when {
                        uiState.origin.isBlank() -> PlaceField.ORIGIN
                        uiState.destination.isBlank() -> PlaceField.DESTINATION
                        else -> lastFocusedField
                    }

                    if (target == PlaceField.ORIGIN) {
                        onOriginChange(place)

                        if (uiState.destination.isBlank()) {
                            destinationFocusRequester.requestFocus()
                        }
                    } else {
                        onDestinationChange(place)
                    }
                }
            )

            TripSection(
                uiState = uiState,
                onTripDateSelected = onTripDateSelected,
                onDateClick = { showDatePicker = true },
                onTerrainSelected = onTerrainSelected
            )

            MotorcycleSection(
                uiState = uiState,
                onMotorcycleSelected = onMotorcycleSelected,
                onRetryClick = onRetryMotorcyclesClick
            )
        }

        TripCreateSubmitBar(
            uiState = uiState,
            onSubmitClick = onSubmitClick
        )
    }

    if (showDatePicker) {
        TripDatePickerDialog(
            mode = uiState.mode,
            selectedDate = uiState.tripDate,
            onDateSelected = onTripDateSelected,
            onDismiss = { showDatePicker = false }
        )
    }
}

// ---------------------------------------------------------------------------
// Edit header
// ---------------------------------------------------------------------------

@Composable
private fun TripEditHeaderCard(status: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(GraphiteHeroBrush)
    ) {
        RoadDecoration(
            modifier = Modifier.matchParentSize(),
            color = Color.White
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Text(
                text = "EDITAR VIAGEM",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = WarmEmberOnDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ajuste o que mudou",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (status) {
                    "IN_PROGRESS" ->
                        "A viagem segue em andamento. Rota, data e moto podem ser corrigidas."

                    "COMPLETED" ->
                        "Corrija a rota, a data ou a moto da viagem já concluída."

                    else ->
                        "A viagem continua planejada. Rota, data e moto podem mudar."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun TripLoadStatus(
    isLoading: Boolean,
    message: String?,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp,
                color = WarmEmber
            )
        } else {
            Text(
                text = message.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            TextButton(onClick = onRetryClick) {
                Text(
                    text = "Tentar novamente",
                    color = WarmEmber,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Mode question
// ---------------------------------------------------------------------------

@Composable
private fun TripCreateModeCard(
    selectedMode: TripCreateMode,
    enabled: Boolean,
    onModeSelected: (TripCreateMode) -> Unit
) {
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Text(
                text = "NOVA VIAGEM",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = WarmEmberOnDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "O que você quer fazer?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeOption(
                    selected = selectedMode == TripCreateMode.PLANNED,
                    enabled = enabled,
                    icon = Icons.Outlined.CalendarMonth,
                    title = "Planejar",
                    subtitle = "Próxima aventura",
                    onClick = { onModeSelected(TripCreateMode.PLANNED) },
                    modifier = Modifier.weight(1f)
                )

                ModeOption(
                    selected = selectedMode == TripCreateMode.COMPLETED,
                    enabled = enabled,
                    icon = Icons.Outlined.TaskAlt,
                    title = "Já realizada",
                    subtitle = "Registrar histórico",
                    onClick = { onModeSelected(TripCreateMode.COMPLETED) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Crossfade(
                targetState = selectedMode,
                label = "trip-create-mode-copy"
            ) { mode ->
                Text(
                    text = when (mode) {
                        TripCreateMode.PLANNED ->
                            "Defina a rota, a data e a moto. Quando chegar a hora, é só iniciar."

                        TripCreateMode.COMPLETED ->
                            "Guarde no seu histórico a rota, a data e a moto da viagem."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f)
                )
            }
        }
    }
}

@Composable
private fun ModeOption(
    selected: Boolean,
    enabled: Boolean,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color.White.copy(alpha = 0.10f),
        label = "mode-option-container"
    )

    val titleColor by animateColorAsState(
        targetValue = if (selected) WarmInk else Color.White,
        label = "mode-option-title"
    )

    val subtitleColor by animateColorAsState(
        targetValue = if (selected) WarmInkSoft else Color.White.copy(alpha = 0.68f),
        label = "mode-option-subtitle"
    )

    val iconContainerColor by animateColorAsState(
        targetValue = if (selected) WarmEmberTint else Color.White.copy(alpha = 0.12f),
        label = "mode-option-icon-container"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) WarmEmber else Color.White,
        label = "mode-option-icon"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconContainerColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = titleColor
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = subtitleColor
        )
    }
}

// ---------------------------------------------------------------------------
// Route
// ---------------------------------------------------------------------------

@Composable
private fun RouteSection(
    uiState: TripCreateUiState,
    destinationFocusRequester: FocusRequester,
    onOriginFocused: () -> Unit,
    onDestinationFocused: () -> Unit,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onOriginCitySelected: (City) -> Unit,
    onDestinationCitySelected: (City) -> Unit,
    onSwapRouteClick: () -> Unit,
    onDistanceChange: (String) -> Unit,
    onApplyDistanceSuggestionClick: () -> Unit,
    onPlaceClick: (String) -> Unit
) {
    FormSection(title = "ROTA") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WarmTextField(
                    value = uiState.origin,
                    onValueChange = onOriginChange,
                    label = "Origem",
                    leadingIcon = Icons.Outlined.TripOrigin,
                    error = uiState.originError,
                    enabled = !uiState.isSubmitting,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.onFocusChanged {
                        if (it.isFocused) onOriginFocused()
                    }
                )

                CitySuggestions(
                    cities = uiState.originSuggestions,
                    onCityClick = onOriginCitySelected
                )

                WarmTextField(
                    value = uiState.destination,
                    onValueChange = onDestinationChange,
                    label = "Destino",
                    leadingIcon = Icons.Outlined.Place,
                    error = uiState.destinationError,
                    enabled = !uiState.isSubmitting,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .focusRequester(destinationFocusRequester)
                        .onFocusChanged {
                            if (it.isFocused) onDestinationFocused()
                        }
                )

                CitySuggestions(
                    cities = uiState.destinationSuggestions,
                    onCityClick = onDestinationCitySelected
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(WarmEmberTint)
                    .clickable(
                        enabled = !uiState.isSubmitting,
                        onClickLabel = "Inverter origem e destino",
                        role = Role.Button,
                        onClick = onSwapRouteClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SwapVert,
                    contentDescription = "Inverter origem e destino",
                    modifier = Modifier.size(22.dp),
                    tint = WarmEmber
                )
            }
        }

        if (uiState.recentPlaces.isNotEmpty()) {
            RecentPlaces(
                places = uiState.recentPlaces,
                enabled = !uiState.isSubmitting,
                onPlaceClick = onPlaceClick
            )
        }

        WarmTextField(
            value = uiState.distance,
            onValueChange = onDistanceChange,
            label = when (uiState.mode) {
                TripCreateMode.PLANNED -> "Distância estimada"
                TripCreateMode.COMPLETED -> "Distância percorrida"
            },
            leadingIcon = Icons.Outlined.Route,
            suffix = "km",
            error = uiState.distanceError,
            enabled = !uiState.isSubmitting,
            trailingIcon = if (uiState.isCalculatingDistance) {
                {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = WarmEmber
                    )
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            )
        )

        if (uiState.isCalculatingDistance) {
            DistanceNote(text = "Calculando a rota entre as cidades...")
        } else {
            when (uiState.distanceSource) {
                DistanceSource.ROUTE ->
                    DistanceNote(text = "Distância calculada pela rota entre as cidades. Você pode ajustar.")

                DistanceSource.ESTIMATE ->
                    DistanceNote(text = "Estimativa em linha reta: não foi possível traçar a rota. Ajuste se souber o valor.")

                null -> Unit
            }
        }

        uiState.distanceSuggestion?.let { suggestion ->
            SuggestionPill(
                text = "Usar ${formatKm(suggestion)} km da viagem anterior",
                onClick = onApplyDistanceSuggestionClick
            )
        }
    }
}

/** The cities that match what was typed; tapping one fills the field with it. */
@Composable
private fun CitySuggestions(
    cities: List<City>,
    onCityClick: (City) -> Unit
) {
    if (cities.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmCanvas)
            .border(BorderStroke(1.dp, WarmHairline), RoundedCornerShape(16.dp))
    ) {
        cities.forEachIndexed { index, city ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(WarmHairline)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClickLabel = "Escolher ${city.label}",
                        role = Role.Button,
                        onClick = { onCityClick(city) }
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = WarmEmber
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = WarmInk)) {
                            append(city.name)
                        }
                        withStyle(SpanStyle(color = WarmMuted)) {
                            append(", ${city.uf}")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DistanceNote(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp),
        style = MaterialTheme.typography.bodySmall,
        color = WarmMuted
    )
}

@Composable
private fun RecentPlaces(
    places: List<String>,
    enabled: Boolean,
    onPlaceClick: (String) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = WarmMuted
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Recentes",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = WarmMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            places.forEach { place ->
                ChoicePill(
                    label = place,
                    selected = false,
                    enabled = enabled,
                    onClick = { onPlaceClick(place) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionPill(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(WarmEmberTint)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                horizontal = 14.dp,
                vertical = 9.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = WarmEmberDeep
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = WarmEmberDeep
        )
    }
}

// ---------------------------------------------------------------------------
// Date and terrain
// ---------------------------------------------------------------------------

@Composable
private fun TripSection(
    uiState: TripCreateUiState,
    onTripDateSelected: (LocalDate) -> Unit,
    onDateClick: () -> Unit,
    onTerrainSelected: (TripCreateTerrain) -> Unit
) {
    val today = remember { LocalDate.now() }

    val quickDates = remember(uiState.mode, today) {
        when (uiState.mode) {
            TripCreateMode.PLANNED -> listOf(
                "Hoje" to today,
                "Amanhã" to today.plusDays(1),
                "Sábado" to today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
                    .let { if (today.dayOfWeek == DayOfWeek.SATURDAY) today.plusDays(7) else it }
            )

            TripCreateMode.COMPLETED -> listOf(
                "Hoje" to today,
                "Ontem" to today.minusDays(1),
                "Há 1 semana" to today.minusDays(7)
            )
        }
    }

    FormSection(title = "VIAGEM") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickDates.forEach { (label, date) ->
                ChoicePill(
                    label = label,
                    selected = uiState.tripDate == date,
                    enabled = !uiState.isSubmitting,
                    onClick = { onTripDateSelected(date) }
                )
            }
        }

        TripDateField(
            label = when (uiState.mode) {
                TripCreateMode.PLANNED -> "Data planejada"
                TripCreateMode.COMPLETED -> "Data da viagem"
            },
            value = formatDate(uiState.tripDate),
            enabled = !uiState.isSubmitting,
            onClick = onDateClick
        )

        Column {
            Text(
                text = "Terreno",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = WarmInkSoft
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TripCreateTerrain.entries.forEach { terrain ->
                    TerrainTile(
                        terrain = terrain,
                        selected = terrain == uiState.terrain,
                        enabled = !uiState.isSubmitting,
                        onClick = { onTerrainSelected(terrain) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TerrainTile(
    terrain: TripCreateTerrain,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (terrain) {
        TripCreateTerrain.ASPHALT -> Icons.Outlined.Route
        TripCreateTerrain.MIXED -> Icons.Outlined.Terrain
        TripCreateTerrain.OFF_ROAD -> Icons.Outlined.Landscape
    }

    val hint = when (terrain) {
        TripCreateTerrain.ASPHALT -> "Estrada"
        TripCreateTerrain.MIXED -> "Estrada e terra"
        TripCreateTerrain.OFF_ROAD -> "Trilha"
    }

    val containerColor by animateColorAsState(
        targetValue = if (selected) WarmInk else WarmCanvas,
        label = "terrain-container"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) WarmInk else WarmHairline,
        label = "terrain-border"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(18.dp)
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(
                horizontal = 8.dp,
                vertical = 14.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (selected) WarmEmberOnDark else WarmEmber
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = terrain.label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) Color.White else WarmInk,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Text(
            text = hint,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color.White.copy(alpha = 0.7f) else WarmMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// Motorcycle
// ---------------------------------------------------------------------------

@Composable
private fun MotorcycleSection(
    uiState: TripCreateUiState,
    onMotorcycleSelected: (Long) -> Unit,
    onRetryClick: () -> Unit
) {
    FormSection(title = "MOTO") {
        when (val motorcycles = uiState.motorcycles) {

            TripCreateMotorcyclesState.Loading -> {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .background(
                                color = WarmEmberTint.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(18.dp)
                            )
                    )
                }
            }

            is TripCreateMotorcyclesState.Error -> {
                Text(
                    text = motorcycles.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )

                TextButton(onClick = onRetryClick) {
                    Text(
                        text = "Tentar novamente",
                        fontWeight = FontWeight.SemiBold,
                        color = WarmEmber
                    )
                }
            }

            is TripCreateMotorcyclesState.Loaded -> {
                if (motorcycles.motorcycles.isEmpty()) {
                    Text(
                        text = "Você ainda não tem nenhuma moto cadastrada. " +
                                "Cadastre uma moto para criar viagens.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInkSoft
                    )
                } else {
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        motorcycles.motorcycles.forEach { motorcycle ->
                            MotorcycleOption(
                                motorcycle = motorcycle,
                                selected = motorcycle.id == uiState.selectedMotorcycleId,
                                enabled = !uiState.isSubmitting,
                                onClick = { onMotorcycleSelected(motorcycle.id) }
                            )
                        }
                    }
                }
            }
        }

        uiState.motorcycleError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun MotorcycleOption(
    motorcycle: MotorcycleResponseDTO,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) WarmEmberTint.copy(alpha = 0.55f) else WarmCanvas,
        label = "motorcycle-container"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) WarmEmber else WarmHairline,
        label = "motorcycle-border"
    )

    val swatch = motorcycleColor(motorcycle.color)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(
                border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
                shape = RoundedCornerShape(18.dp)
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(WarmCard, CircleShape)
                .border(1.dp, WarmHairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.TwoWheeler,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = WarmInk
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${motorcycle.brand} ${motorcycle.model}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                swatch?.let { color ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, CircleShape)
                            .border(1.dp, WarmHairline, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = "${motorcycle.engineCapacity} cc · ${motorcycle.year}",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmInkSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = if (selected) {
                Icons.Filled.CheckCircle
            } else {
                Icons.Outlined.RadioButtonUnchecked
            },
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = if (selected) WarmEmber else WarmHairline
        )
    }
}

// ---------------------------------------------------------------------------
// Building blocks
// ---------------------------------------------------------------------------




@Composable
private fun TripDateField(
    label: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            label = { Text(text = label) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            },
            shape = WarmFieldShape,
            colors = warmFieldColors()
        )

        // The text field consumes touches itself, so an overlay opens the picker.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(WarmFieldShape)
                .clickable(
                    enabled = enabled,
                    onClickLabel = "Escolher data",
                    role = Role.Button,
                    onClick = onClick
                )
        )
    }
}


// ---------------------------------------------------------------------------
// Submit bar
// ---------------------------------------------------------------------------

@Composable
private fun TripCreateSubmitBar(
    uiState: TripCreateUiState,
    onSubmitClick: () -> Unit
) {
    val summary = routeSummary(uiState)

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

            if (summary != null && uiState.submitErrorMessage == null) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmInkSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = onSubmitClick,
                enabled = !uiState.isSubmitting &&
                    !uiState.isLoadingTrip &&
                    uiState.loadTripErrorMessage == null,
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
                        text = when {
                            uiState.isEditing -> "Salvar alterações"
                            uiState.mode == TripCreateMode.PLANNED -> "Criar viagem"
                            else -> "Registrar viagem"
                        },
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

private fun routeSummary(uiState: TripCreateUiState): String? {
    val origin = uiState.origin.trim()
    val destination = uiState.destination.trim()

    if (origin.isEmpty() || destination.isEmpty()) return null

    val distance = uiState.distance
        .replace(',', '.')
        .trimEnd('.')
        .toDoubleOrNull()
        ?.takeIf { it > 0 }

    return buildString {
        append("$origin → $destination")

        if (distance != null) {
            append(" · ${formatKm(distance)} km")
        }
    }
}

// ---------------------------------------------------------------------------
// Date picker
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDatePickerDialog(
    mode: TripCreateMode,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val today = remember { LocalDate.now() }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toUtcMillis(),
        yearRange = when (mode) {
            TripCreateMode.PLANNED -> today.year..(today.year + 5)
            TripCreateMode.COMPLETED -> (today.year - 40)..today.year
        },
        selectableDates = TripDateSelectableDates(
            mode = mode,
            today = today
        )
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(millis.toUtcLocalDate())
                    }
                    onDismiss()
                }
            ) {
                Text(
                    text = "Confirmar",
                    fontWeight = FontWeight.Bold,
                    color = WarmEmber
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = WarmInkSoft
                )
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = WarmEmber,
                selectedYearContainerColor = WarmEmber,
                todayDateBorderColor = WarmEmber,
                todayContentColor = WarmEmberDeep
            )
        )
    }
}

// Mirrors the backend rules for UX only: planned trips cannot be in the past and
// completed trips cannot be in the future. The backend remains the authority.
@OptIn(ExperimentalMaterial3Api::class)
private class TripDateSelectableDates(
    private val mode: TripCreateMode,
    private val today: LocalDate
) : SelectableDates {

    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val date = utcTimeMillis.toUtcLocalDate()

        return when (mode) {
            TripCreateMode.PLANNED -> !date.isBefore(today)
            TripCreateMode.COMPLETED -> !date.isAfter(today)
        }
    }

    override fun isSelectableYear(year: Int): Boolean =
        when (mode) {
            TripCreateMode.PLANNED -> year >= today.year
            TripCreateMode.COMPLETED -> year <= today.year
        }
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun formatDate(
    date: LocalDate
): String {
    return date
        .format(
            DateTimeFormatter.ofPattern(
                "EEE, d MMM yyyy",
                Locale.forLanguageTag("pt-BR")
            )
        )
        .replace(".", "")
}

private fun formatKm(distanceKm: Double): String {
    val formatter = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("pt-BR")
    ).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 1
    }

    return formatter.format(distanceKm)
}
