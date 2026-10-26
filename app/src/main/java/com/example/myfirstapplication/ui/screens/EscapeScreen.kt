package com.example.myfirstapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfirstapplication.data.model.Destination
import com.example.myfirstapplication.ui.components.EscapeCard
import com.example.myfirstapplication.ui.components.EscapeRadarView
import com.example.myfirstapplication.ui.components.WeatherBackground
import com.example.myfirstapplication.ui.components.glass
import com.example.myfirstapplication.ui.util.EscapeAdvisor
import com.example.myfirstapplication.ui.util.EscapeResult
import com.example.myfirstapplication.ui.util.TripFilter
import com.example.myfirstapplication.ui.util.WeatherCondition
import com.example.myfirstapplication.ui.viewmodel.EscapeUiState

@Composable
fun EscapeScreen(
    uiState: EscapeUiState,
    filter: TripFilter,
    originName: String,
    onFilterChange: (TripFilter) -> Unit,
    onOptionClick: (Destination) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    val result = (uiState as? EscapeUiState.Success)?.result
    val condition = result?.let { WeatherCondition.fromCode(it.originWeather.current.weatherCode) }
    val isDay = result?.originWeather?.current?.isDay != 0

    WeatherBackground(condition = condition, isDay = isDay) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White
                )
            }

            when (uiState) {
                is EscapeUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color.White) }

                is EscapeUiState.Error -> Box(
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

                is EscapeUiState.Success -> EscapeContent(
                    result = uiState.result,
                    filter = filter,
                    originName = originName,
                    onFilterChange = onFilterChange,
                    onOptionClick = onOptionClick
                )
            }
        }
    }
}

@Composable
private fun EscapeContent(
    result: EscapeResult,
    filter: TripFilter,
    originName: String,
    onFilterChange: (TripFilter) -> Unit,
    onOptionClick: (Destination) -> Unit
) {
    val options = remember(result, filter) { EscapeAdvisor.forFilter(result, filter) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            "Nereye kaçsam?",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            EscapeAdvisor.headline(originName, result.originScore, options.firstOrNull()),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TripFilter.entries.forEach { f ->
                FilterPill(
                    label = f.label,
                    selected = f == filter,
                    onClick = { onFilterChange(f) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(24.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            EscapeRadarView(
                options = options,
                maxHours = filter.maxHours,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Merkez: $originName · Halkalar yaklaşık yolculuk süresini gösterir",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )

        Spacer(Modifier.height(16.dp))

        if (options.isEmpty()) {
            Text("Bu aralıkta rota bulunamadı.", color = Color.White)
        }
        options.forEach { option ->
            EscapeCard(
                option = option,
                originScore = result.originScore,
                onClick = { onOptionClick(option.destination) }
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    Text(
        text = label,
        color = if (selected) Color(0xFF1A2236) else Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier
            .clip(shape)
            .then(if (selected) Modifier.background(Color.White) else Modifier.glass(shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    )
}