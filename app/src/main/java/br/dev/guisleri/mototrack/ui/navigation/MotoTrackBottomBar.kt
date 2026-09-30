package br.dev.guisleri.mototrack.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@Composable
fun MotoTrackBottomBar(
    currentDestination: MainDestination,
    onDestinationSelected: (MainDestination) -> Unit
) {

    NavigationBar {

        MainDestination.entries.forEach { destination ->

            NavigationBarItem(
                selected = currentDestination == destination,
                onClick = {
                    onDestinationSelected(destination)
                },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = destination.iconResId
                        ),
                        contentDescription = null
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