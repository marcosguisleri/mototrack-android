package br.dev.guisleri.mototrack.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.ui.theme.MotoTrackAccent
import br.dev.guisleri.mototrack.ui.theme.MotoTrackTeal
import br.dev.guisleri.mototrack.ui.theme.MotoTrackTextSecondary

@Composable
fun MotoTrackBottomBar(
    currentDestination: MainDestination,
    onDestinationSelected: (MainDestination) -> Unit
) {

    NavigationBar {

        MainDestination.entries.forEach { destination ->

            NavigationBarItem(
                selected = currentDestination == destination,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MotoTrackTeal,
                    selectedTextColor = MotoTrackTeal,
                    unselectedIconColor = MotoTrackTextSecondary,
                    unselectedTextColor = MotoTrackTextSecondary,
                    indicatorColor = MotoTrackAccent.copy(
                        alpha = 0.14f
                    )
                ),
                onClick = {
                    onDestinationSelected(destination)
                },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = destination.iconResId
                        ),
                        contentDescription = destination.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = destination.label
                    )
                }
            )
        }
    }
}