package com.example.myfirstapplication

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myfirstapplication.data.model.FavoriteCity
import com.example.myfirstapplication.ui.screens.CityListScreen
import com.example.myfirstapplication.ui.screens.EscapeScreen
import com.example.myfirstapplication.ui.screens.HomeScreen
import com.example.myfirstapplication.ui.screens.WeatherDetailScreen
import com.example.myfirstapplication.ui.theme.MyFirstApplicationTheme
import com.example.myfirstapplication.ui.viewmodel.CityViewModel
import com.example.myfirstapplication.ui.viewmodel.EscapeViewModel
import com.example.myfirstapplication.ui.viewmodel.HomeViewModel
import com.example.myfirstapplication.ui.viewmodel.WeatherViewModel

import android.util.Log

private fun NavController.openWeather(lat: Double, lon: Double, name: String) {
    Log.d("Hava", "Tıklandı: $name")
    navigate("weather/$lat/$lon/${Uri.encode(name)}")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyFirstApplicationTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {

                    composable("home") {
                        val viewModel: HomeViewModel = viewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                        HomeScreen(
                            uiState = uiState,
                            myLocation = HomeViewModel.MY_LOCATION,
                            onSearchClick = { navController.navigate("search") },
                            onEscapeClick = { navController.navigate("escape") },
                            onCityClick = { city: FavoriteCity ->
                                navController.openWeather(city.latitude, city.longitude, city.name)
                            },
                            onRetry = viewModel::retry
                        )
                    }

                    composable("search") {
                        val viewModel: CityViewModel = viewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        val query by viewModel.query.collectAsStateWithLifecycle()

                        CityListScreen(
                            uiState = uiState,
                            query = query,
                            onQueryChange = viewModel::onQueryChange,
                            onCityClick = { city ->
                                navController.openWeather(city.latitude, city.longitude, city.name)
                            },
                            onRetry = viewModel::retry,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("escape") {
                        val viewModel: EscapeViewModel = viewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        val filter by viewModel.filter.collectAsStateWithLifecycle()

                        EscapeScreen(
                            uiState = uiState,
                            filter = filter,
                            originName = viewModel.originName,
                            onFilterChange = viewModel::setFilter,
                            onOptionClick = { d ->
                                navController.openWeather(d.latitude, d.longitude, d.name)
                            },
                            onBack = { navController.popBackStack() },
                            onRetry = viewModel::retry
                        )
                    }

                    composable("weather/{lat}/{lon}/{name}") { entry ->
                        val lat = entry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                        val lon = entry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
                        val name = entry.arguments?.getString("name").orEmpty()

                        val viewModel: WeatherViewModel = viewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        val favorites by viewModel.favorites.collectAsStateWithLifecycle()
                        val isFavorite = favorites.any {
                            it.latitude == lat && it.longitude == lon
                        }

                        LaunchedEffect(lat, lon) { viewModel.load(lat, lon) }

                        WeatherDetailScreen(
                            cityName = name,
                            uiState = uiState,
                            isFavorite = isFavorite,
                            onToggleFavorite = {
                                viewModel.toggleFavorite(FavoriteCity(name, lat, lon))
                            },
                            onBack = { navController.popBackStack() },
                            onRetry = viewModel::retry
                        )
                    }
                }
            }
        }
    }
}