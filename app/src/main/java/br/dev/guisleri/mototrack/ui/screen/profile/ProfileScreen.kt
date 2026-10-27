package br.dev.guisleri.mototrack.ui.screen.profile

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import br.dev.guisleri.mototrack.ui.components.WarmTextField
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.notification.canPostNotifications
import br.dev.guisleri.mototrack.ui.components.BrandMark
import br.dev.guisleri.mototrack.ui.components.RideOutlinedButton
import br.dev.guisleri.mototrack.ui.components.ShimmerBlock
import br.dev.guisleri.mototrack.ui.components.WarmDialog
import br.dev.guisleri.mototrack.ui.components.formatDistance
import br.dev.guisleri.mototrack.ui.components.formatNumber
import br.dev.guisleri.mototrack.ui.theme.GraphiteHeroBrush
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberOnDark
import br.dev.guisleri.mototrack.ui.theme.WarmEmberTint
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted
import br.dev.guisleri.mototrack.ui.theme.WarmSkeleton

private const val BADGES_PER_ROW = 3

// The server accepts up to 100 characters when the name is edited.
private const val MAX_NAME_LENGTH = 100

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    trips: List<TripResponseDTO>?,
    motorcycleCount: Int?,
    onRetryUserClick: () -> Unit,
    onEditNameClick: () -> Unit,
    onDismissNameEditor: () -> Unit,
    onSaveName: (String) -> Unit,
    onChangeEmailClick: () -> Unit,
    remindersEnabled: Boolean,
    onRemindersChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false
) {
    val stats = if (trips != null && motorcycleCount != null) {
        profileStatsOf(trips, motorcycleCount)
    } else {
        null
    }

    var openedAchievement by rememberSaveable { mutableStateOf<AchievementKind?>(null) }
    var showLogoutConfirmation by rememberSaveable { mutableStateOf(false) }

    if (showLogoutConfirmation) {
        WarmDialog(
            icon = Icons.AutoMirrored.Outlined.Logout,
            title = "Sair da conta?",
            message = "Você vai precisar entrar de novo para ver suas viagens. " +
                    "Os lembretes de viagem deste aparelho ficam pausados até lá.",
            confirmText = "Sair",
            onConfirm = {
                showLogoutConfirmation = false
                onLogoutClick()
            },
            onDismissRequest = { showLogoutConfirmation = false },
            dismissText = "Continuar no app"
        )
    }

    if (uiState.isEditingName) {
        EditNameDialog(
            currentName = uiState.user?.name.orEmpty(),
            isSaving = uiState.isSavingName,
            errorMessage = uiState.nameErrorMessage,
            onSave = onSaveName,
            onDismiss = onDismissNameEditor
        )
    }

    openedAchievement
        ?.let { kind -> stats?.achievements?.firstOrNull { it.kind == kind } }
        ?.let { achievement ->
            AchievementDialog(
                achievement = achievement,
                onDismiss = { openedAchievement = null }
            )
        }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        BrandMark(modifier = Modifier.padding(bottom = 4.dp))

        Text(
            text = "Perfil",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk
        )

        IdentityCard(
            uiState = uiState,
            unlockedCount = stats?.unlockedCount,
            achievementCount = stats?.achievements?.size,
            onRetryClick = onRetryUserClick,
            onEditNameClick = onEditNameClick
        )

        stats?.let { LevelCard(riderLevelOf(it)) }

        StatsRow(stats)

        SectionTitle(
            title = "Conquistas",
            trailing = stats?.let { "${it.unlockedCount} de ${it.achievements.size}" }
        )

        AchievementsGrid(
            achievements = stats?.achievements,
            onAchievementClick = { openedAchievement = it.kind }
        )

        SectionTitle(title = "Destinos favoritos")

        FavoriteDestinationsCard(stats)

        SectionTitle(title = "Conta")

        AccountCard(onChangeEmailClick = onChangeEmailClick)

        SectionTitle(title = "Preferências")

        RemindersCard(
            enabled = remindersEnabled,
            onChange = onRemindersChange
        )

        RideOutlinedButton(
            text = if (isLoggingOut) "Saindo..." else "Sair da conta",
            icon = Icons.AutoMirrored.Outlined.Logout,
            onClick = { if (!isLoggingOut) showLogoutConfirmation = true },
            color = WarmInkSoft,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** The rider's level: a badge, the XP and how far the next level is. */
@Composable
private fun LevelCard(level: RiderLevel) {
    val fraction by animateFloatAsState(targetValue = level.fraction, label = "level-progress")

    WarmSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(WarmEmberTint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level.level.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmEmber
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NÍVEL ${level.level}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.6.sp,
                        color = WarmEmber
                    )

                    Text(
                        text = level.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmInk
                    )
                }

                Text(
                    text = "${formatXp(level.xp)} XP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmInk
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(WarmHairline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceAtLeast(0.03f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(WarmEmber)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = level.nextName
                    ?.let { "Faltam ${formatXp(level.xpToNext)} XP para $it" }
                    ?: "Você chegou ao nível máximo. Respeito, estradeiro!",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = WarmInkSoft
            )

            Text(
                text = "1 km = 1 XP · +50 por viagem concluída · +25 por destino diferente",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted
            )
        }
    }
}

private fun formatXp(xp: Int): String =
    java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("pt-BR")).format(xp)

/** What the rider can do about the credentials of the account. */
@Composable
private fun AccountCard(
    onChangeEmailClick: () -> Unit
) {
    WarmSurface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClickLabel = "Alterar e-mail",
                    role = Role.Button,
                    onClick = onChangeEmailClick
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(WarmEmberTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = WarmEmber,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Alterar e-mail",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarmInk
                )

                Text(
                    text = "Confirmamos o novo endereço com um código.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmMuted
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = WarmMuted
            )
        }
    }
}

@Composable
private fun EditNameDialog(
    currentName: String,
    isSaving: Boolean,
    errorMessage: String?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    WarmDialog(
        icon = Icons.Outlined.Edit,
        title = "Editar nome",
        message = "É assim que o app te chama na Home e no seu perfil.",
        confirmText = if (isSaving) "Salvando..." else "Salvar",
        onConfirm = { onSave(name) },
        onDismissRequest = onDismiss,
        dismissText = "Cancelar",
        isLoading = isSaving
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        WarmTextField(
            value = name,
            onValueChange = { name = it.take(MAX_NAME_LENGTH) },
            label = "Nome",
            leadingIcon = Icons.Outlined.Person,
            error = errorMessage,
            enabled = !isSaving,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun IdentityCard(
    uiState: ProfileUiState,
    unlockedCount: Int?,
    achievementCount: Int?,
    onRetryClick: () -> Unit,
    onEditNameClick: () -> Unit
) {
    val user = uiState.user

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(GraphiteHeroBrush)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(WarmEmber),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialOf(user?.name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                when {
                    user != null -> {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    uiState.isLoading -> {
                        ShimmerBlock(Modifier.size(width = 140.dp, height = 20.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        ShimmerBlock(Modifier.size(width = 180.dp, height = 14.dp))
                    }

                    else -> {
                        Text(
                            text = "Piloto",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        TextButton(
                            onClick = onRetryClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "Tentar novamente",
                                color = WarmEmberOnDark,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2
                            )
                        }
                    }
                }

                if (unlockedCount != null && achievementCount != null) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EmojiEvents,
                            contentDescription = null,
                            tint = WarmEmberOnDark,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "$unlockedCount de $achievementCount conquistas",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (user != null) {
                IconButton(onClick = onEditNameClick) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar nome",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(stats: ProfileStats?) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            value = stats?.totalKm,
            label = "km rodados",
            modifier = Modifier.weight(1f)
        )

        StatCard(
            value = stats?.completedTrips?.toDouble(),
            label = "viagens",
            modifier = Modifier.weight(1f)
        )

        StatCard(
            value = stats?.destinationCount?.toDouble(),
            label = "destinos",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: Double?,
    label: String,
    modifier: Modifier = Modifier
) {
    WarmSurface(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (value == null) {
                ShimmerBlock(Modifier.size(width = 44.dp, height = 26.dp))
            } else {
                Text(
                    text = formatNumber(animatedFrom0(value.toFloat()).toDouble()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmInk,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = WarmMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    trailing: String? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk
        )

        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = WarmMuted
            )
        }
    }
}

@Composable
private fun AchievementsGrid(
    achievements: List<Achievement>?,
    onAchievementClick: (Achievement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (achievements == null) {
            repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(BADGES_PER_ROW) {
                        ShimmerBlock(
                            modifier = Modifier
                                .weight(1f)
                                .height(132.dp),
                            cornerRadius = 20.dp
                        )
                    }
                }
            }
            return@Column
        }

        achievements.chunked(BADGES_PER_ROW).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { achievement ->
                    AchievementTile(
                        achievement = achievement,
                        onClick = { onAchievementClick(achievement) },
                        modifier = Modifier.weight(1f)
                    )
                }

                repeat(BADGES_PER_ROW - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AchievementTile(
    achievement: Achievement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unlocked = achievement.unlocked

    Card(
        onClick = onClick,
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) WarmEmberTint else WarmCard
        ),
        border = BorderStroke(1.dp, if (unlocked) WarmEmber.copy(alpha = 0.35f) else WarmHairline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (unlocked) WarmEmber else WarmSkeleton),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (unlocked) achievement.kind.icon() else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = if (unlocked) Color.White else WarmMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (unlocked) WarmInk else WarmInkSoft,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            if (unlocked) {
                Text(
                    text = "Conquistada",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmEmber
                )
            } else {
                val animatedFraction = animatedFrom0(achievement.fraction)

                LinearProgressIndicator(
                    progress = { animatedFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(5.dp)
                        .clip(CircleShape),
                    color = WarmEmber,
                    trackColor = WarmSkeleton,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}

@Composable
private fun AchievementDialog(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    val progress = if (achievement.unlocked) {
        "Conquistada!"
    } else {
        "Progresso: ${formatProgress(achievement.current)} de ${formatProgress(achievement.target)}"
    }

    WarmDialog(
        icon = achievement.kind.icon(),
        title = achievement.title,
        message = "${achievement.description}.\n$progress",
        confirmText = "Fechar",
        onConfirm = onDismiss,
        onDismissRequest = onDismiss
    )
}

@Composable
private fun FavoriteDestinationsCard(stats: ProfileStats?) {
    WarmSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            when {
                stats == null -> {
                    ShimmerBlock(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp)
                    )
                }

                stats.topDestinations.isEmpty() -> {
                    Text(
                        text = "Conclua uma viagem para ver aqui os lugares que você mais visita.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInkSoft
                    )
                }

                else -> {
                    stats.topDestinations.forEachIndexed { index, destination ->
                        DestinationRow(position = index + 1, destination = destination)
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationRow(
    position: Int,
    destination: DestinationVisit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (position == 1) WarmEmber else WarmEmberTint),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (position == 1) Color.White else WarmEmber
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = destination.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WarmInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${visitsLabel(destination.visits)} · ${formatDistance(destination.totalKm)}",
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted
            )
        }
    }
}

@Composable
private fun RemindersCard(
    enabled: Boolean,
    onChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onChange(true)
        } else {
            Toast.makeText(
                context,
                "Permita as notificações do MotoTrack nas configurações do aparelho.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    WarmSurface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(WarmEmberTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = WarmEmber,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lembretes de viagem",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarmInk
                )

                Text(
                    text = "Avisos das viagens (véspera às 18h e dia às 7h30), de viagem que " +
                        "ficou em andamento, de gastos a registrar e da manutenção da moto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmMuted
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = enabled,
                onCheckedChange = { wanted ->
                    if (wanted && !canPostNotifications(context)) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onChange(wanted)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = WarmEmber,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = WarmSkeleton,
                    uncheckedThumbColor = Color.White,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun WarmSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WarmCard),
        border = BorderStroke(1.dp, WarmHairline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        content()
    }
}

private fun AchievementKind.icon(): ImageVector = when (this) {
    AchievementKind.FIRST_TRIP -> Icons.Outlined.Flag
    AchievementKind.TEN_TRIPS -> Icons.Outlined.Repeat
    AchievementKind.KM_100 -> Icons.Outlined.Route
    AchievementKind.KM_1000 -> Icons.Outlined.Speed
    AchievementKind.KM_10000 -> Icons.Outlined.EmojiEvents
    AchievementKind.EXPLORER -> Icons.Outlined.Explore
    AchievementKind.LONG_HAUL -> Icons.Outlined.Map
    AchievementKind.OFF_ROAD -> Icons.Outlined.Terrain
    AchievementKind.GARAGE -> Icons.Outlined.TwoWheeler
}

internal fun initialOf(name: String?): String =
    name?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "M"

internal fun visitsLabel(visits: Int): String =
    if (visits == 1) "1 visita" else "$visits visitas"

private fun formatProgress(value: Double): String =
    formatNumber(value.coerceAtLeast(0.0))

/** Counts up from zero the first time it is shown, then follows [target] smoothly. */
@Composable
private fun animatedFrom0(target: Float): Float {
    var shown by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(target) { shown = target }

    return animateFloatAsState(
        targetValue = shown,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "profile-count"
    ).value
}
