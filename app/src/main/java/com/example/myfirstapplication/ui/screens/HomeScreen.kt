package com.example.myfirstapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfirstapplication.data.model.FavoriteCity
import com.example.myfirstapplication.data.model.WeatherResponse
import com.example.myfirstapplication.ui.components.FavoriteWeatherCard
import com.example.myfirstapplication.ui.components.WeatherBackground
import com.example.myfirstapplication.ui.util.WeatherAdvisor
import com.example.myfirstapplication.ui.util.WeatherCondition
import com.example.myfirstapplication.ui.viewmodel.HomeUiState
import kotlin.math.roundToInt

import androidx.compose.foundation.background

import com.example.myfirstapplication.ui.components.WeatherIllustration
import com.example.myfirstapplication.ui.components.glass

import androidx.compose.foundation.clickable

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    myLocation: FavoriteCity,
    onSearchClick: () -> Unit,
    onEscapeClick: () -> Unit,
    onCityClick: (FavoriteCity) -> Unit,
    onRetry: () -> Unit
) {
    val myWeather = uiState.myLocation
    val condition = myWeather?.let { WeatherCondition.fromCode(it.current.weatherCode) }
    val isDay = myWeather?.current?.isDay != 0

    WeatherBackground(condition = condition, isDay = isDay) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Hava Durumu",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Filled.Search, contentDescription = "Şehir ara", tint = Color.White)
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                myWeather != null -> MyLocationCard(
                    city = myLocation,
                    weather = myWeather,
                    onClick = { onCityClick(myLocation) }
                )

                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color.White) }

                else -> Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("İnternet bağlantını kontrol et.", color = Color.White)
                    Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                        Text("Tekrar dene")
                    }
                }
            }

            if (myWeather != null) {
                Spacer(Modifier.height(12.dp))
                EscapeEntryCard(
                    badWeather = condition != WeatherCondition.CLEAR,
                    onClick = onEscapeClick
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Favorilerim",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))

            if (uiState.favorites.isEmpty()) {
                Text(
                    "Henüz favori şehrin yok. Sağ üstten şehir ara, " +
                            "detay ekranında ♡ simgesine dokunup ekle.",
                    color = Color.White.copy(alpha = 0.8f)
                )
            } else {
                uiState.favorites.forEach { item ->
                    FavoriteWeatherCard(
                        item = item,
                        onClick = { onCityClick(item.city) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MyLocationCard(
    city: FavoriteCity,
    weather: WeatherResponse,
    onClick: () -> Unit
) {
    val current = weather.current
    val condition = WeatherCondition.fromCode(current.weatherCode)
    val advice = WeatherAdvisor.advice(
        condition = condition,
        feelsLike = current.feelsLike,
        rainProbability = weather.daily.rainProbability.firstOrNull() ?: 0,
        windSpeed = current.windSpeed
    )
    val shape = RoundedCornerShape(28.dp)

    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().glass(shape)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            WeatherIllustration(
                condition = condition,
                isDay = current.isDay != 0,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(150.dp)
                    .padding(top = 8.dp, end = 8.dp)
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${city.name} · varsayılan",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${current.temperature.roundToInt()}°",
                    color = Color.White,
                    fontSize = 84.sp,
                    fontWeight = FontWeight.Thin
                )
                Text(
                    "${condition.label} · Hissedilen ${current.feelsLike.roundToInt()}°",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    advice,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun EscapeEntryCard(badWeather: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🧭", fontSize = 28.sp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Nereye kaçsam?",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                if (badWeather) "Hava böyleyse güneşi birlikte bulalım"
                else "Güzel havayı en iyi yere taşı",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }
        Text("›", color = Color.White, fontSize = 24.sp)
    }
}