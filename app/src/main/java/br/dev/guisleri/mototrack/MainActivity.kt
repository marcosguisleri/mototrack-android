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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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

    var selectedDestination by remember {
        mutableStateOf(MainDestination.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                MainDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination,
                        onClick = {
                            selectedDestination = destination
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

        when (selectedDestination) {
            MainDestination.HOME -> HomeScreen(modifier = Modifier.padding(innerPadding))
            MainDestination.TRIPS -> TripsScreen(modifier = Modifier.padding(innerPadding))
            MainDestination.MOTORCYCLES -> MotorcyclesScreen(modifier = Modifier.padding(innerPadding))
            MainDestination.PROFILE -> ProfileScreen(modifier = Modifier.padding(innerPadding))
        }

    }
}