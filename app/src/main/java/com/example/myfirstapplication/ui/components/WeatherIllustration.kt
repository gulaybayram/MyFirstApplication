package com.example.myfirstapplication.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.myfirstapplication.ui.util.WeatherCondition
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private const val TWO_PI = (2.0 * PI).toFloat()

private val stars = listOf(
    Offset(0.12f, 0.20f), Offset(0.82f, 0.14f), Offset(0.72f, 0.82f),
    Offset(0.20f, 0.78f), Offset(0.92f, 0.52f), Offset(0.48f, 0.06f)
)

@Composable
fun WeatherIllustration(
    condition: WeatherCondition?,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "illustration")
    val loop by transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "loop"
    )
    val fast by transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "fast"
    )
    val slow by transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(24000, easing = LinearEasing)), label = "slow"
    )
    val pulse by transition.animateFloat(
        0.94f, 1.06f,
        infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val s = size.minDimension
        val c = Offset(size.width / 2f, size.height / 2f)
        val drift = sin(slow * TWO_PI) * s * 0.04f

        when (condition) {
            WeatherCondition.CLEAR ->
                if (isDay) drawSun(c, s * 0.2f, loop, pulse)
                else drawNight(c, s * 0.2f, loop)

            WeatherCondition.CLOUDY -> {
                drawCloud(c.x - s * 0.12f + drift, c.y - s * 0.12f, s * 0.5f, Color.White.copy(alpha = 0.5f))
                drawCloud(c.x + drift * 1.5f, c.y + s * 0.02f, s * 0.8f, Color.White.copy(alpha = 0.92f))
            }

            WeatherCondition.FOG -> {
                drawCloud(c.x + drift, c.y - s * 0.12f, s * 0.7f, Color.White.copy(alpha = 0.6f))
                for (i in 0..2) {
                    val y = c.y + s * (0.14f + 0.1f * i)
                    val shift = sin(loop * TWO_PI + i) * s * 0.05f
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(c.x - s * 0.25f + shift, y),
                        end = Offset(c.x + s * 0.25f + shift, y),
                        strokeWidth = s * 0.025f,
                        cap = StrokeCap.Round
                    )
                }
            }

            WeatherCondition.RAIN -> {
                val w = s * 0.78f
                val cy = c.y - s * 0.12f
                drawCloud(c.x + drift, cy, w, Color(0xFFCFD8DC))
                drawPrecip(c.x + drift, cy, w, fast, snow = false, wave = loop)
            }

            WeatherCondition.SNOW -> {
                val w = s * 0.78f
                val cy = c.y - s * 0.12f
                drawCloud(c.x + drift, cy, w, Color.White.copy(alpha = 0.95f))
                drawPrecip(c.x + drift, cy, w, loop, snow = true, wave = loop)
            }

            WeatherCondition.STORM -> {
                val w = s * 0.78f
                val cy = c.y - s * 0.12f
                drawCloud(c.x + drift, cy, w, Color(0xFF9AA3AD))
                drawPrecip(c.x + drift, cy, w, fast, snow = false, wave = loop)
                val flash = loop < 0.03f || (loop > 0.06f && loop < 0.09f)
                if (flash) drawBolt(c.x + drift, cy + w * 0.2f, w)
            }

            null -> drawCloud(c.x, c.y, s * 0.7f, Color.White.copy(alpha = 0.4f))
        }
    }
}

private fun DrawScope.drawCloud(cx: Float, cy: Float, w: Float, color: Color) {
    val h = w * 0.5f
    drawCircle(color, radius = h * 0.5f, center = Offset(cx - w * 0.22f, cy))
    drawCircle(color, radius = h * 0.65f, center = Offset(cx + w * 0.02f, cy - h * 0.25f))
    drawCircle(color, radius = h * 0.45f, center = Offset(cx + w * 0.27f, cy + h * 0.02f))
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - w * 0.35f, cy - h * 0.02f),
        size = Size(w * 0.7f, h * 0.5f),
        cornerRadius = CornerRadius(h * 0.25f)
    )
}

private fun DrawScope.drawPrecip(
    cx: Float, cy: Float, w: Float,
    progress: Float, snow: Boolean, wave: Float
) {
    val top = cy + w * 0.26f
    val range = w * 0.42f
    val count = 8
    for (i in 0 until count) {
        val x = cx - w * 0.28f + w * 0.56f * (i / (count - 1f))
        val t = (progress + i * 0.137f) % 1f
        val y = top + t * range
        val alpha = 1f - t
        if (snow) {
            val sway = sin(wave * TWO_PI + i) * w * 0.02f
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = w * 0.018f * (1f + (i % 3) * 0.4f),
                center = Offset(x + sway, y)
            )
        } else {
            drawLine(
                color = Color(0xFFBBDEFB).copy(alpha = alpha),
                start = Offset(x, y),
                end = Offset(x - w * 0.02f, y + w * 0.07f),
                strokeWidth = w * 0.012f,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawBolt(cx: Float, y0: Float, w: Float) {
    val bolt = Path().apply {
        moveTo(cx + w * 0.04f, y0)
        lineTo(cx - w * 0.08f, y0 + w * 0.22f)
        lineTo(cx - w * 0.01f, y0 + w * 0.22f)
        lineTo(cx - w * 0.07f, y0 + w * 0.42f)
        lineTo(cx + w * 0.10f, y0 + w * 0.17f)
        lineTo(cx + w * 0.02f, y0 + w * 0.17f)
        close()
    }
    drawPath(bolt, Color(0xFFFFEB3B))
}

private fun DrawScope.drawSun(c: Offset, r: Float, rotation: Float, pulse: Float) {
    val glowRadius = r * 2.6f * pulse
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x55FFF3C4), Color.Transparent),
            center = c,
            radius = glowRadius
        ),
        radius = glowRadius,
        center = c
    )
    rotate(degrees = rotation * 360f, pivot = c) {
        for (i in 0 until 12) {
            val angle = i * 30f * PI.toFloat() / 180f
            val inner = r * 1.3f
            val outer = if (i % 2 == 0) r * 1.9f else r * 1.6f
            drawLine(
                color = Color(0xFFFFF3C4).copy(alpha = 0.85f),
                start = Offset(c.x + cos(angle) * inner, c.y + sin(angle) * inner),
                end = Offset(c.x + cos(angle) * outer, c.y + sin(angle) * outer),
                strokeWidth = r * 0.09f,
                cap = StrokeCap.Round
            )
        }
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF8E1), Color(0xFFFFD54F)),
            center = c,
            radius = r
        ),
        radius = r,
        center = c
    )
}

private fun DrawScope.drawNight(c: Offset, r: Float, loop: Float) {
    stars.forEachIndexed { i, f ->
        val twinkle = 0.35f + 0.65f * abs(sin(loop * TWO_PI + i * 1.3f))
        drawCircle(
            color = Color.White.copy(alpha = twinkle),
            radius = r * 0.07f * (1f + (i % 3) * 0.3f),
            center = Offset(size.width * f.x, size.height * f.y)
        )
    }
    val bob = sin(loop * TWO_PI) * r * 0.1f
    val center = Offset(c.x, c.y + bob)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x40FFF3D6), Color.Transparent),
            center = center,
            radius = r * 2.4f
        ),
        radius = r * 2.4f,
        center = center
    )
    val moon = Path().apply { addOval(Rect(center = center, radius = r)) }
    val cut = Path().apply {
        addOval(Rect(center = Offset(center.x + r * 0.5f, center.y - r * 0.2f), radius = r * 0.85f))
    }
    drawPath(Path.combine(PathOperation.Difference, moon, cut), Color(0xFFFFF3D6))
}