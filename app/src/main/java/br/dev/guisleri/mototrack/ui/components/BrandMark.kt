package br.dev.guisleri.mototrack.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.R
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmInk

/** The MotoTrack symbol and name, for the top of the main screens. */
@Composable
internal fun BrandMark(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_mototrack_symbol),
            contentDescription = null,
            modifier = Modifier.size(
                width = 39.dp,
                height = 24.dp
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = WarmInk)) {
                    append("Moto")
                }
                withStyle(SpanStyle(color = WarmEmber)) {
                    append("Track")
                }
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic
        )
    }
}
