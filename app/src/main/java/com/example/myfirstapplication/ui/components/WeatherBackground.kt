package com.example.myfirstapplication.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.myfirstapplication.ui.util.WeatherCondition
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal data class Palette(
    val top: Color,
    val bottom: Color,
    val blobA: Color,
    val blobB: Color
)

// Koyu ve doygun tonlar: beyaz yazı rahat okunur, ekran "soluk" durmaz
internal fun palette(condition: WeatherCondition?, isDay: Boolean): Palette =
    when (condition) {
        WeatherCondition.CLEAR ->
            if (isDay) Palette(Color(0xFFE8833A), Color(0xFFB23A48), Color(0xFFFFB347), Color(0xFFFF4D6D))
            else Palette(Color(0xFF0B1026), Color(0xFF1B2A5C), Color(0xFF4C3FD1), Color(0xFF1E6FD9))
        WeatherCondition.CLOUDY ->
            Palette(Color(0xFF3B4E7A), Color(0xFF16213E), Color(0xFF5B7CCF), Color(0xFF3F51B5))
        WeatherCondition.FOG ->
            Palette(Color(0xFF55657A), Color(0xFF2A3441), Color(0xFF8FA3B5), Color(0xFF607D8B))
        WeatherCondition.RAIN ->
            Palette(Color(0xFF1B4965), Color(0xFF08141F), Color(0xFF1565C0), Color(0xFF00897B))
        WeatherCondition.SNOW ->
            Palette(Color(0xFF5B84B1), Color(0xFF24406A), Color(0xFF90CAF9), Color(0xFF5C8DDB))
        WeatherCondition.STORM ->
            Palette(Color(0xFF2B2D42), Color(0xFF0D0D14), Color(0xFF5E35B1), Color(0xFF283593))
        null ->
            Palette(Color(0xFF1E2A4A), Color(0xFF0E1426), Color(0xFF3F5BD8), Color(0xFF5C6BC0))
    }

@Composable
fun WeatherBackground(
    condition: WeatherCondition?,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val target = palette(condition, isDay)
    val top by animateColorAsState(target.top, tween(900), label = "top")
    val bottom by animateColorAsState(target.bottom, tween(900), label = "bottom")
    val blobA by animateColorAsState(target.blobA, tween(900), label = "blobA")
    val blobB by animateColorAsState(target.blobB, tween(900), label = "blobB")

    val transition = rememberInfiniteTransition(label = "aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, bottom)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val a = phase * 2f * PI.toFloat()
            val w = size.width
            val h = size.height
            blob(blobA, Offset(w * (0.2f + 0.15f * sin(a)), h * (0.18f + 0.08f * cos(a))), w * 0.75f)
            blob(blobB, Offset(w * (0.85f - 0.15f * cos(a)), h * (0.55f + 0.08f * sin(a))), w * 0.85f)
            blob(blobA, Offset(w * (0.3f + 0.2f * cos(a)), h * (0.92f - 0.06f * sin(a))), w * 0.65f)

            // Alt tarafı hafifçe karart: derinlik verir, kartlar öne çıkar
            drawRect(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f)),
                    startY = h * 0.5f,
                    endY = h
                )
            )
        }
        content()
    }
}

private fun DrawScope.blob(color: Color, center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.32f), Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}