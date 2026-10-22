package br.dev.guisleri.mototrack.ui.screen.trips.details.photos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.data.photo.MAX_TRIP_PHOTOS
import br.dev.guisleri.mototrack.data.photo.TripPhoto
import br.dev.guisleri.mototrack.ui.components.ShimmerBlock
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberTint
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted

// The cover is wider than tall, so it fills the card without towering over the rest of the page.
private const val COVER_ASPECT = 1.25f
private val TileShape = RoundedCornerShape(18.dp)

/** The best photos of the trip: the cover on show, a way into all of them, and a viewer. */
@Composable
internal fun TripPhotosSection(
    state: TripPhotosUiState,
    onAddPhotos: (List<String>) -> Unit,
    onMakeCover: (String) -> Unit,
    onRemovePhoto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewerIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    val addPhotos = rememberPhotoPicker(state.remaining, onAddPhotos)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(WarmCard)
            .border(BorderStroke(1.dp, WarmHairline), RoundedCornerShape(28.dp))
            .padding(vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FOTOS",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = WarmEmber
            )

            Text(
                text = "${state.photos.size}/$MAX_TRIP_PHOTOS",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(WarmHairline)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = WarmInkSoft
            )

            if (state.photos.isNotEmpty() && (state.remaining > 0 || state.isAdding)) {
                Spacer(modifier = Modifier.width(8.dp))

                AddChip(isAdding = state.isAdding, onClick = addPhotos)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "As melhores fotos desta viagem",
            modifier = Modifier.padding(horizontal = 22.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (state.photos.isEmpty() && !state.isLoading) {
            EmptyGallery(
                isAdding = state.isAdding,
                onAddClick = addPhotos,
                modifier = Modifier.padding(horizontal = 22.dp)
            )
        } else {
            val cover = state.photos.firstOrNull()

            if (cover == null) {
                ShimmerBlock(
                    modifier = Modifier
                        .padding(horizontal = 22.dp)
                        .fillMaxWidth()
                        .aspectRatio(COVER_ASPECT),
                    cornerRadius = 22.dp
                )
            } else {
                CoverPhoto(
                    cover = cover,
                    totalPhotos = state.photos.size,
                    onClick = { viewerIndex = 0 },
                    modifier = Modifier.padding(horizontal = 22.dp)
                )
            }
        }
    }

    viewerIndex?.let { index ->
        if (state.photos.isEmpty()) {
            viewerIndex = null
        } else {
            TripPhotoViewer(
                photos = state.photos,
                initialIndex = index.coerceIn(0, state.photos.lastIndex),
                onMakeCover = onMakeCover,
                onRemove = onRemovePhoto,
                onDismiss = { viewerIndex = null }
            )
        }
    }
}

@Composable
private fun CoverBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(WarmInk.copy(alpha = 0.75f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            modifier = Modifier.size(11.dp),
            tint = Color(0xFFFFC857)
        )

        Spacer(modifier = Modifier.width(3.dp))

        Text(
            text = "CAPA",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

/** The cover, alone and big: the rest of the photos are one tap away. */
@Composable
private fun CoverPhoto(
    cover: TripPhoto,
    totalPhotos: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(COVER_ASPECT)
            .clip(shape)
            .clickable(role = Role.Button, onClickLabel = "Abrir foto de capa", onClick = onClick)
    ) {
        TripPhotoImage(
            file = cover.file,
            targetPx = 1000,
            modifier = Modifier.fillMaxSize()
        )

        // A soft shade keeps the labels readable on any photo.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(84.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)))
                )
        )

        CoverBadge(
            Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )

        if (totalPhotos > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "+${totalPhotos - 1}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun AddChip(
    isAdding: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(WarmEmberTint)
            .clickable(enabled = !isAdding, role = Role.Button, onClickLabel = "Adicionar fotos", onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isAdding) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = WarmEmber
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = WarmEmber
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Adicionar",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmEmber
                )
            }
        }
    }
}

@Composable
private fun EmptyGallery(
    isAdding: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(TileShape)
            .background(WarmEmberTint)
            .dashedBorder()
            .clickable(enabled = !isAdding, role = Role.Button, onClick = onAddClick)
            .padding(vertical = 22.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAdding) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp,
                color = WarmEmber
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = WarmEmber
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Guarde as melhores fotos desta viagem",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = WarmInk,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Até $MAX_TRIP_PHOTOS fotos. Toque para escolher.",
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun Modifier.dashedBorder(): Modifier =
    drawBehind {
        drawRoundRect(
            color = WarmEmber.copy(alpha = 0.55f),
            cornerRadius = CornerRadius(18.dp.toPx()),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx()))
            )
        )
    }
