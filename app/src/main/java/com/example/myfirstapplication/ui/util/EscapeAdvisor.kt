package com.example.myfirstapplication.ui.util

import com.example.myfirstapplication.data.model.Destination
import com.example.myfirstapplication.data.model.WeatherResponse
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class TripFilter(val label: String, val minHours: Double, val maxHours: Double) {
    DAY("Günübirlik (1-2 saat)", 0.0, 2.5),
    WEEKEND("Hafta sonu (3-5 saat)", 2.5, 5.5)
}

data class EscapeOption(
    val destination: Destination,
    val roadKm: Int,
    val hours: Double,
    val bearing: Double,
    val destWeather: WeatherResponse,
    val midWeather: WeatherResponse,
    val score: Int
)

data class EscapeResult(
    val originWeather: WeatherResponse,
    val originScore: Int,
    val options: List<EscapeOption>
)

object EscapeAdvisor {

    private const val ROAD_FACTOR = 1.35      // düz mesafe -> tahmini yol mesafesi
    private const val AVG_SPEED_KMH = 80.0

    /** İstek sırası: [çıkış], sonra her rota için [orta nokta, varış] */
    fun points(
        originLat: Double,
        originLon: Double,
        destinations: List<Destination>
    ): List<Pair<Double, Double>> = buildList {
        add(originLat to originLon)
        destinations.forEach { d ->
            add((originLat + d.latitude) / 2 to (originLon + d.longitude) / 2)
            add(d.latitude to d.longitude)
        }
    }

    fun build(
        originLat: Double,
        originLon: Double,
        destinations: List<Destination>,
        weathers: List<WeatherResponse>
    ): EscapeResult {
        val originWeather = weathers[0]
        val options = destinations.mapIndexed { i, d ->
            val mid = weathers[1 + i * 2]
            val dest = weathers[2 + i * 2]
            val straightKm = haversineKm(originLat, originLon, d.latitude, d.longitude)
            val roadKm = straightKm * ROAD_FACTOR
            EscapeOption(
                destination = d,
                roadKm = roadKm.roundToInt(),
                hours = roadKm / AVG_SPEED_KMH,
                bearing = bearingDeg(originLat, originLon, d.latitude, d.longitude),
                destWeather = dest,
                midWeather = mid,
                score = ((sunScore(dest) * 2 + sunScore(mid)) / 3.0).roundToInt()
            )
        }
        return EscapeResult(originWeather, sunScore(originWeather), options)
    }

    fun forFilter(result: EscapeResult, filter: TripFilter): List<EscapeOption> =
        result.options
            .filter { it.hours > filter.minHours && it.hours <= filter.maxHours }
            .sortedWith(compareByDescending<EscapeOption> { it.score }.thenBy { it.hours })

    fun headline(originName: String, originScore: Int, best: EscapeOption?): String {
        if (best == null) return "Bu süre aralığında rota bulunamadı"
        val gain = best.score - originScore
        return when {
            gain >= 3 -> "$originName bugün pek parlak değil, ${best.destination.name} $gain puan daha güzel ☀️"
            gain >= 1 -> "${best.destination.name} biraz daha iyi görünüyor, kaçamak yapılır 🙂"
            else -> "Bugün $originName zaten çok iyi, kaçmana gerek yok 😄"
        }
    }

    /** 1..10: ne kadar güneşli ve keyifli */
    fun sunScore(w: WeatherResponse): Int {
        val c = w.current
        var s = when (WeatherCondition.fromCode(c.weatherCode)) {
            WeatherCondition.CLEAR -> 10
            WeatherCondition.CLOUDY -> 6
            WeatherCondition.FOG -> 4
            WeatherCondition.SNOW -> 3
            WeatherCondition.RAIN -> 2
            WeatherCondition.STORM -> 1
        }
        val rain = w.daily.rainProbability.firstOrNull() ?: 0
        s -= when {
            rain >= 60 -> 3
            rain >= 30 -> 1
            else -> 0
        }
        s -= when {
            c.windSpeed >= 40 -> 2
            c.windSpeed >= 25 -> 1
            else -> 0
        }
        s -= when {
            c.feelsLike <= 0 -> 2
            c.feelsLike < 8 -> 1
            c.feelsLike >= 34 -> 2
            else -> 0
        }
        return s.coerceIn(1, 10)
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun bearingDeg(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dl = Math.toRadians(lon2 - lon1)
        val y = sin(dl) * cos(p2)
        val x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }
}

fun EscapeOption.durationLabel(): String {
    val totalMin = (hours * 60).roundToInt()
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "$h sa $m dk" else "$m dk"
}