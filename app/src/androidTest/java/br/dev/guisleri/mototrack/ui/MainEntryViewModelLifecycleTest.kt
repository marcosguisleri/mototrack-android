package br.dev.guisleri.mototrack.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import br.dev.guisleri.mototrack.data.network.HomeApiService
import br.dev.guisleri.mototrack.data.repository.HomeRepository
import br.dev.guisleri.mototrack.ui.navigation.AppDestination
import br.dev.guisleri.mototrack.ui.screen.home.HomeViewModel
import br.dev.guisleri.mototrack.ui.screen.home.HomeViewModelFactory
import br.dev.guisleri.mototrack.ui.screen.main.MainScreen
import br.dev.guisleri.mototrack.ui.theme.MotoTrackTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class MainEntryViewModelLifecycleTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun removingMainClearsHomeViewModelAndReenteringLoadsFreshHome() {
        val requests = AtomicInteger()
        val cleared = AtomicInteger()
        val instances = mutableListOf<HomeViewModel>()
        lateinit var backStack: NavBackStack<NavKey>
        val repository = HomeRepository(object : HomeApiService {
            override suspend fun getHome() = HomeResponseDTO(
                nextTrip = null,
                lastCompletedTrip = null,
                totalCompletedDistanceKm = 0.0,
                completedTrips = 0,
                motorcycleCount = requests.incrementAndGet().toLong()
            )
        })
        val restoration = StateRestorationTester(compose)

        restoration.setContent {
            MotoTrackTheme {
                val stack = rememberNavBackStack(AppDestination.MAIN)
                SideEffect { backStack = stack }

                NavDisplay(
                    backStack = stack,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = { destination ->
                        when (destination) {
                            AppDestination.MAIN -> NavEntry(destination) {
                                val homeViewModel: HomeViewModel = viewModel(
                                    factory = HomeViewModelFactory(repository)
                                )
                                SideEffect {
                                    if (homeViewModel !in instances) {
                                        instances.add(homeViewModel)
                                        homeViewModel.addCloseable(AutoCloseable {
                                            cleared.incrementAndGet()
                                        })
                                    }
                                }
                                MainScreen(
                                    homeViewModel = homeViewModel,
                                    onLogoutClick = {
                                        stack.clear()
                                        stack.add(AppDestination.LOGIN)
                                    }
                                )
                            }
                            AppDestination.LOGIN -> NavEntry(destination) {
                                Button(onClick = {
                                    stack.clear()
                                    stack.add(AppDestination.MAIN)
                                }) {
                                    Text("Entrar novamente")
                                }
                            }
                            else -> error("Unexpected test destination: $destination")
                        }
                    }
                )
            }
        }

        compose.onNodeWithText("Motos: 1").assertExists()
        lateinit var first: HomeViewModel
        compose.runOnIdle { first = instances.single() }

        compose.onNodeWithText("Viagens").performClick()
        compose.onNodeWithText("Motos").performClick()
        compose.onNodeWithText("Home").performClick()
        compose.onNodeWithText("Motos: 1").assertExists()
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("Sair").assertExists()
        compose.runOnIdle {
            assertEquals(listOf(AppDestination.MAIN), backStack.toList())
            assertSame(first, instances.single())
            assertEquals(1, requests.get())
            assertEquals(0, cleared.get())
        }

        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Sair").assertExists()
        lateinit var restored: HomeViewModel
        compose.runOnIdle {
            assertEquals(listOf(AppDestination.MAIN), backStack.toList())
            assertEquals(2, instances.size)
            restored = instances.last()
            assertNotSame(first, restored)
            assertEquals(2, requests.get())
            assertEquals(1, cleared.get())
        }

        compose.onNodeWithText("Sair").performClick()
        compose.onNodeWithText("Entrar novamente").assertExists()
        compose.waitUntil(timeoutMillis = 5_000) { cleared.get() == 2 }
        compose.runOnIdle {
            assertEquals(listOf(AppDestination.LOGIN), backStack.toList())
        }

        compose.onNodeWithText("Entrar novamente").performClick()
        compose.onNodeWithText("Motos: 3").assertExists()
        compose.runOnIdle {
            assertEquals(listOf(AppDestination.MAIN), backStack.toList())
            assertEquals(3, instances.size)
            assertNotSame(restored, instances.last())
            assertEquals(3, requests.get())
            assertEquals(2, cleared.get())
        }
    }
}
