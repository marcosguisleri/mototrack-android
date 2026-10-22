package br.dev.guisleri.mototrack.ui.screen.trips.details.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.dev.guisleri.mototrack.data.photo.TripPhoto
import br.dev.guisleri.mototrack.ui.components.WarmDialog

/** Full-screen view of the trip's photos, one swipe apart, with the cover and delete actions. */
@Composable
internal fun TripPhotoViewer(
    photos: List<TripPhoto>,
    initialIndex: Int,
    onMakeCover: (String) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }
    var confirmDelete by remember { mutableStateOf(false) }

    val current = photos.getOrNull(pagerState.currentPage)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { photos.getOrNull(it)?.id ?: it }
            ) { page ->
                TripPhotoImage(
                    file = photos[page].file,
                    targetPx = 1200,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    placeholder = Color.Transparent
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ViewerButton(Icons.Filled.Close, "Fechar", onDismiss)

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${pagerState.currentPage + 1} de ${photos.size}",
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (current != null) {
                val isCover = pagerState.currentPage == 0

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ViewerAction(
                        icon = if (isCover) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        text = if (isCover) "Esta é a capa" else "Tornar capa",
                        enabled = !isCover,
                        tint = if (isCover) Color(0xFFFFC857) else Color.White,
                        onClick = { onMakeCover(current.id) }
                    )

                    ViewerAction(
                        icon = Icons.Outlined.DeleteOutline,
                        text = "Excluir",
                        tint = Color(0xFFFF8A80),
                        onClick = { confirmDelete = true }
                    )
                }
            }
        }

        if (confirmDelete && current != null) {
            WarmDialog(
                icon = Icons.Outlined.DeleteOutline,
                title = "Excluir esta foto?",
                message = "A foto sai da galeria desta viagem.",
                warning = "Esta ação é permanente e não pode ser desfeita.",
                confirmText = "Excluir foto",
                onConfirm = {
                    confirmDelete = false
                    onRemove(current.id)
                },
                onDismissRequest = { confirmDelete = false },
                dismissText = "Cancelar",
                destructive = true
            )
        }
    }
}

@Composable
private fun ViewerButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(22.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun ViewerAction(
    icon: ImageVector,
    text: String,
    tint: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = tint
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.7f)
        )
    }
}
