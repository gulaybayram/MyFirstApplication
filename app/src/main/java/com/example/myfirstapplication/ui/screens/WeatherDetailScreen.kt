package com.example.myfirstapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfirstapplication.data.model.WeatherResponse
import com.example.myfirstapplication.ui.components.ActivityPanel
import com.example.myfirstapplication.ui.components.ActivityTrigger
import com.example.myfirstapplication.ui.components.WeatherBackground
import com.example.myfirstapplication.ui.components.WeatherIllustration
import com.example.myfirstapplication.ui.components.glass
import com.example.myfirstapplication.ui.components.palette
import com.example.myfirstapplication.ui.util.ActivityAdvisor
import com.example.myfirstapplication.ui.util.WeatherAdvisor
import com.example.myfirstapplication.ui.util.WeatherCondition
import com.example.myfirstapplication.ui.viewmodel.WeatherUiState
import kotlin.math.roundToInt

@Composable
fun WeatherDetailScreen(
    cityName: String,
    uiState: WeatherUiState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    val success = uiState as? WeatherUiState.Success
    val condition = success?.let { WeatherCondition.fromCode(it.weather.current.weatherCode) }
    val isDay = success?.weather?.current?.isDay != 0
    var showPanel by rememberSaveable { mutableStateOf(false) }

    WeatherBackground(condition = condition, isDay = isDay) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite
                        else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = Color.White
                    )
                }
            }

            when (uiState) {
                is WeatherUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color.White) }

                is WeatherUiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.message, color = Color.White)
                        Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                            Text("Tekrar dene")
                        }
                    }
                }

                is WeatherUiState.Success -> WeatherContent(cityName, uiState.weather)
            }
        }

        if (success != null) {
            val bottomColor = palette(condition, isDay).bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, bottomColor)))
                    .navigationBarsPadding()
                    .padding(top = 32.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                ActivityTrigger(
                    onClick = { showPanel = true },
                    color = bottomColor
                )
            }
        }
    }

    if (showPanel && success != null) {
        val c = success.weather.current
        ActivityPanel(
            suggestion = ActivityAdvisor.suggest(
                condition = WeatherCondition.fromCode(c.weatherCode),
                feelsLike = c.feelsLike,
                windSpeed = c.windSpeed,
                rainProbability = success.weather.daily.rainProbability.firstOrNull() ?: 0,
                isDay = c.isDay != 0
            ),
            containerColor = palette(condition, isDay).bottom,
            onDismiss = { showPanel = false }
        )
    }
}

@Composable
private fun WeatherContent(cityName: String, weather: WeatherResponse) {
    val current = weather.current
    val daily = weather.daily
    val condition = WeatherCondition.fromCode(current.weatherCode)
    val todayRain = daily.rainProbability.firstOrNull() ?: 0

    val advice = WeatherAdvisor.advice(
        condition = condition,
        feelsLike = current.feelsLike,
        rainProbability = todayRain,
        windSpeed = current.windSpeed
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            WeatherIllustration(
                condition = condition,
                isDay = current.isDay != 0,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(170.dp)
            )
            Column {
                Text(cityName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                Text(condition.label, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${current.temperature.roundToInt()}°",
                    color = Color.White,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Thin
                )
                Text(
                    text = "Hissedilen ${current.feelsLike.roundToInt()}°",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = advice,
            color = Color.White,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Spacer(Modifier.height(24.dp))

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("Rüzgar", "${current.windSpeed.roundToInt()} km/s")
                InfoItem("Yağış ihtimali", "%$todayRain")
                InfoItem(
                    "Bugün",
                    "${daily.tempMin.first().roundToInt()}° / ${daily.tempMax.first().roundToInt()}°"
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        GlassCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Önümüzdeki 7 gün", color = Color.White, fontWeight = FontWeight.Medium)
                daily.time.indices.forEach { i ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(dayLabel(i, daily.time[i]), color = Color.White)
                        Text(
                            WeatherCondition.fromCode(daily.weatherCode[i]).label,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "${daily.tempMin[i].roundToInt()}° / ${daily.tempMax[i].roundToInt()}°",
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Aktivite şeridinin altında kalmasın diye bol boşluk (içeriğin EN SONUNDA)
        Spacer(Modifier.height(160.dp))
    }
}

@Composable
private fun GlassCard(content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().glass(RoundedCornerShape(24.dp))) {
        content()
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

private fun dayLabel(index: Int, isoDate: String): String = when (index) {
    0 -> "Bugün"
    1 -> "Yarın"
    else -> "${isoDate.substring(8, 10)}.${isoDate.substring(5, 7)}"
}