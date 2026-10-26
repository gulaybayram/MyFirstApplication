package com.example.myfirstapplication.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfirstapplication.ui.util.EscapeOption
import com.example.myfirstapplication.ui.util.WeatherCondition
import com.example.myfirstapplication.ui.util.durationLabel
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

internal fun scoreColor(score: Int): Color = when {
    score >= 8 -> Color(0xFF81C784)
    score >= 5 -> Color(0xFFFFD54F)
    else -> Color(0xFFE57373)
}

@Composable
fun EscapeRadarView(
    options: List<EscapeOption>,
    maxHours: Double,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val transition = rememberInfiniteTransition(label = "radar")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "sweep"
    )

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val c = center
        val radius = size.minDimension / 2f * 0.85f
        val ring = Stroke(width = 1.dp.toPx())

        // Halkalar ve artı çizgileri
        for (i in 1..3) {
            drawCircle(Color.White.copy(alpha = 0.2f), radius * i / 3f, c, style = ring)
        }
        drawLine(
            Color.White.copy(alpha = 0.12f),
            Offset(c.x - radius, c.y), Offset(c.x + radius, c.y), 1.dp.toPx()
        )
        drawLine(
            Color.White.copy(alpha = 0.12f),
            Offset(c.x, c.y - radius), Offset(c.x, c.y + radius), 1.dp.toPx()
        )

        // Dönen tarama çizgisi
        rotate(degrees = sweep * 360f, pivot = c) {
            drawArc(
                color = Color.White.copy(alpha = 0.10f),
                startAngle = -130f,
                sweepAngle = 40f,
                useCenter = true,
                topLeft = Offset(c.x - radius, c.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            drawLine(
                Color.White.copy(alpha = 0.4f),
                c, Offset(c.x, c.y - radius), 2.dp.toPx()
            )
        }

        // Rotalar: yön = gerçek yön, uzaklık = yolculuk süresi
        options.forEach { o ->
            val dist = (o.hours / maxHours).coerceIn(0.12, 1.0).toFloat() * radius
            val angle = Math.toRadians(o.bearing).toFloat()
            val p = Offset(c.x + sin(angle) * dist, c.y - cos(angle) * dist)
            val color = scoreColor(o.score)

            drawCircle(color.copy(alpha = 0.3f), 13.dp.toPx(), p)
            drawCircle(color, 6.dp.toPx(), p)

            val label = measurer.measure(
                o.destination.name,
                TextStyle(color = Color.White, fontSize = 10.sp)
            )
            val gap = 10.dp.toPx()
            val x = if (p.x > c.x) p.x + gap else p.x - gap - label.size.width
            drawText(
                label,
                topLeft = Offset(
                    x.coerceIn(0f, size.width - label.size.width),
                    p.y - label.size.height / 2f
                )
            )
        }

        // Merkez: bulunduğun yer
        drawCircle(Color.White.copy(alpha = 0.3f), 14.dp.toPx(), c)
        drawCircle(Color.White, 7.dp.toPx(), c)
    }
}

@Composable
fun EscapeCard(
    option: EscapeOption,
    originScore: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    val destCondition = WeatherCondition.fromCode(option.destWeather.current.weatherCode)
    val midCondition = WeatherCondition.fromCode(option.midWeather.current.weatherCode)
    val isDay = option.destWeather.current.isDay != 0
    val colors = palette(destCondition, isDay)
    val gain = option.score - originScore

    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(colors.top.copy(alpha = 0.9f), colors.bottom.copy(alpha = 0.9f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.25f), shape)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WeatherIllustration(
                    condition = destCondition,
                    isDay = isDay,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        option.destination.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        option.destination.tagline,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Text(
                        "🚗 ${option.durationLabel()} · ${option.roadKm} km",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${option.destWeather.current.temperature.roundToInt()}°",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        "${option.score}/10",
                        color = Color(0xFF1A2236),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(scoreColor(option.score), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "Yolda: ${midCondition.label} ${option.midWeather.current.temperature.roundToInt()}°" +
                        "  →  Varışta: ${destCondition.label}",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            if (gain >= 1) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Bulunduğun yere göre +$gain puan daha güzel ✨",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }
    }
}