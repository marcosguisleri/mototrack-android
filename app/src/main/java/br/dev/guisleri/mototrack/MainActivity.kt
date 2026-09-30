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

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Home",
        modifier = modifier
    )
}

@Composable
fun TripsScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Trips",
        modifier = modifier
    )
}

@Composable
fun MotorcyclesScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Motorcycles",
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Profile",
        modifier = modifier
    )
}

enum class MainDestination(
    val label: String,
    val iconResId: Int
) {
    HOME("Home", R.drawable.ic_home),
    TRIPS("Viagens", R.drawable.ic_trips),
    MOTORCYCLES("Motos", R.drawable.ic_motorcycle),
    PROFILE("Perfil", R.drawable.ic_profile)
}
