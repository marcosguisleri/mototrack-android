package br.dev.guisleri.mototrack.ui.screen.trips.details

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.dev.guisleri.mototrack.data.model.motorcycle.motorcycleType
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.MotorcycleHeroImage
import br.dev.guisleri.mototrack.ui.components.formatDistance
import br.dev.guisleri.mototrack.ui.components.formatTerrain
import br.dev.guisleri.mototrack.ui.components.formatTripDate
import br.dev.guisleri.mototrack.ui.theme.GraphiteHeroBrush
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberOnDark
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Shows the picture that will be shared and sends exactly what is on screen:
 * the card is drawn once and captured, so what the rider sees is what goes out.
 */
@Composable
internal fun TripShareDialog(
    trip: TripResponseDTO,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    var isPreparing by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isPreparing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The recorded layer is square; only what is shown on screen has rounded corners.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    }
            ) {
                TripShareCard(trip = trip)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isPreparing) return@Button

                    isPreparing = true
                    scope.launch {
                        try {
                            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                            val uri = saveShareImage(context, bitmap, trip.id)
                            shareTripImage(context, uri, tripShareText(trip))
                            onDismiss()
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            Toast.makeText(
                                context,
                                "Não foi possível preparar a imagem.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            isPreparing = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmEmber,
                    contentColor = Color.White
                )
            ) {
                if (isPreparing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(text = "Compartilhar imagem", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = onDismiss,
                enabled = !isPreparing
            ) {
                Text(text = "Fechar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** The poster itself; fixed colours, so it looks the same whatever the app's theme. */
@Composable
private fun TripShareCard(
    trip: TripResponseDTO
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .background(GraphiteHeroBrush)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White)) { append("MOTO") }
                        withStyle(SpanStyle(color = WarmEmberOnDark)) { append("TRACK") }
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = tripShareStatusLabel(trip.status),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = WarmEmberOnDark
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = trip.origin,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "→ ${trip.destination}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            MotorcycleHeroImage(
                type = trip.motorcycle.motorcycleType,
                colorName = trip.motorcycle.color,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShareFact(
                    value = formatDistance(trip.distanceKm),
                    label = "distância",
                    modifier = Modifier.weight(1f)
                )

                ShareFact(
                    value = formatTerrain(trip.terrain),
                    label = "terreno",
                    modifier = Modifier.weight(1f)
                )

                ShareFact(
                    value = formatTripDate(trip.tripDate),
                    label = "data",
                    modifier = Modifier.weight(1.3f)
                )
            }
        }
    }
}

@Composable
private fun ShareFact(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.55f)
        )
    }
}
