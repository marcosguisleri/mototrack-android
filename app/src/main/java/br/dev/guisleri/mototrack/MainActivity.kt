package br.dev.guisleri.mototrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

import br.dev.guisleri.mototrack.ui.navigation.MainDestination
import br.dev.guisleri.mototrack.ui.screen.home.HomeScreen
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesScreen
import br.dev.guisleri.mototrack.ui.screen.profile.ProfileScreen
import br.dev.guisleri.mototrack.ui.screen.trips.TripsScreen
import br.dev.guisleri.mototrack.ui.theme.MotoTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotoTrackTheme {
                MotoTrackApp()
            }
        }
    }
}

@Composable
fun MotoTrackApp() {

    val backStack = remember {
        mutableStateListOf(MainDestination.HOME)
    }

    val currentDestination = backStack.last()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                MainDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination,
                        onClick = {
                            if (currentDestination != destination) {
                                backStack.clear()
                                backStack.add(MainDestination.HOME)

                                if (destination != MainDestination.HOME) {
                                    backStack.add(destination)
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = destination.iconResId),
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
    ) { innerPadding ->

        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            },
            entryProvider = { destination ->
                when (destination) {
                    MainDestination.HOME -> NavEntry(destination) {
                        HomeScreen(
                            Modifier.padding(innerPadding)
                        )
                    }
                    MainDestination.TRIPS -> NavEntry(destination) {
                        TripsScreen(
                            Modifier.padding(innerPadding)
                        )
                    }
                    MainDestination.MOTORCYCLES -> NavEntry(destination) {
                        MotorcyclesScreen(
                            Modifier.padding(innerPadding)
                        )
                    }
                    MainDestination.PROFILE -> NavEntry(destination) {
                        ProfileScreen(
                            Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        )

    }
}