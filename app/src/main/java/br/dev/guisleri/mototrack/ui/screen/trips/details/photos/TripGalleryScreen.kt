package br.dev.guisleri.mototrack.ui.screen.trips.details.photos

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.data.photo.MAX_TRIP_PHOTOS
import br.dev.guisleri.mototrack.data.photo.TripPhoto
import br.dev.guisleri.mototrack.ui.components.CircleActionButton
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberTint
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted
import kotlinx.coroutines.delay

// The mosaic: the cover across the top, and one panorama across the middle of a long gallery.
private const val PANORAMA_INDEX = 5

private enum class TileRole { HERO, PANORAMA, HALF }

private fun roleOf(index: Int): TileRole =
    when (index) {
        0 -> TileRole.HERO
        PANORAMA_INDEX -> TileRole.PANORAMA
        else -> TileRole.HALF
    }

/** All the photos of a trip, big, in a mosaic; a photo opens in the full-screen viewer. */
@Composable
internal fun TripGalleryScreen(
    route: String,
    state: TripPhotosUiState,
    onBackClick: () -> Unit,
    onAddPhotos: (List<String>) -> Unit,
    onMakeCover: (String) -> Unit,
    onRemovePhoto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewerIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val addPhotos = rememberPhotoPicker(state.remaining, onAddPhotos)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            GalleryTopBar(
                route = route,
                count = state.photos.size,
                onBackClick = onBackClick
            )

            if (state.photos.isEmpty()) {
                EmptyGallery(
                    isLoading = state.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 6.dp,
                        // Room for the add button, when there is one.
                        bottom = if (state.remaining > 0) 120.dp else 32.dp
                    ),
                    verticalItemSpacing = 10.dp,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = state.photos,
                        key = { _, photo -> photo.id },
                        span = { index, _ ->
                            if (roleOf(index) == TileRole.HALF) {
                                StaggeredGridItemSpan.SingleLane
                            } else {
                                StaggeredGridItemSpan.FullLine
                            }
                        }
                    ) { index, photo ->
                        MosaicTile(
                            photo = photo,
                            index = index,
                            role = roleOf(index),
                            onClick = { viewerIndex = index }
                        )
                    }
                }
            }
        }

        if (state.remaining > 0 || state.isAdding) {
            AddPill(
                isAdding = state.isAdding,
                remaining = state.remaining,
                onClick = addPhotos,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp)
            )
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
private fun GalleryTopBar(
    route: String,
    count: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleActionButton(
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Voltar",
            onClick = onBackClick
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Fotos da viagem",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk,
                maxLines = 1
            )

            Text(
                text = route,
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "$count/$MAX_TRIP_PHOTOS",
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(WarmHairline)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = WarmInkSoft
        )
    }
}

@Composable
private fun MosaicTile(
    photo: TripPhoto,
    index: Int,
    role: TileRole,
    onClick: () -> Unit
) {
    val ratio = rememberPhotoAspectRatio(photo.file)

    // Each kind of tile has its own proportions; the half-width ones follow the photo's own,
    // so the two columns come out staggered.
    val aspect = when (role) {
        TileRole.HERO -> ratio.coerceIn(1.15f, 1.6f)
        TileRole.PANORAMA -> 2f
        TileRole.HALF -> ratio.coerceIn(0.7f, 1.3f)
    }

    val shape = RoundedCornerShape(if (role == TileRole.HALF) 20.dp else 28.dp)

    // Tiles come in one after the other, the first time they are shown.
    var revealed by rememberSaveable(photo.id) { mutableStateOf(false) }
    val appear = remember { Animatable(if (revealed) 1f else 0f) }

    LaunchedEffect(photo.id) {
        if (!revealed) {
            delay((index * 70L).coerceAtMost(560L))
            appear.animateTo(1f, tween(durationMillis = 480, easing = FastOutSlowInEasing))
            revealed = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .graphicsLayer {
                alpha = appear.value
                val scale = 0.92f + 0.08f * appear.value
                scaleX = scale
                scaleY = scale
                translationY = (1f - appear.value) * 28.dp.toPx()
            }
            .shadow(if (role == TileRole.HALF) 2.dp else 6.dp, shape, ambientColor = Color(0x22000000))
            .clip(shape)
            .clickable(role = Role.Button, onClickLabel = "Abrir foto", onClick = onClick)
    ) {
        TripPhotoImage(
            file = photo.file,
            targetPx = if (role == TileRole.HALF) 600 else 1000,
            modifier = Modifier.fillMaxSize()
        )

        if (role != TileRole.HALF) {
            // A soft shade at the bottom keeps the label readable on any photo.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)))
                    )
            )
        }

        if (index == 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = Color(0xFFFFC857)
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "Capa",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun AddPill(
    isAdding: Boolean,
    remaining: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .shadow(10.dp, shape, ambientColor = Color(0x55000000))
            .clip(shape)
            .background(WarmEmber)
            .clickable(enabled = !isAdding, role = Role.Button, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAdding) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.AddPhotoAlternate,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Adicionar fotos",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (remaining == 1) "1 restante" else "$remaining restantes",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.22f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EmptyGallery(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = WarmEmber, strokeWidth = 3.dp)
        } else {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(50))
                    .background(WarmEmberTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = WarmEmber
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Nenhuma foto por aqui",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Guarde até $MAX_TRIP_PHOTOS fotos desta viagem e elas ganham um mosaico só delas.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkSoft,
                textAlign = TextAlign.Center
            )
        }
    }
}
