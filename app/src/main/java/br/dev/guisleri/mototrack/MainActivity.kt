package br.dev.guisleri.mototrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import br.dev.guisleri.mototrack.ui.MotoTrackApp
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