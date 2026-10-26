package com.example.myfirstapplication.ui.util

data class Activity(
    val emoji: String,
    val title: String,
    val hint: String,
    val score: Int,        // 1..10
    val indoor: Boolean
)

data class ActivitySuggestion(
    val headline: String,
    val activities: List<Activity>
)

object ActivityAdvisor {

    fun suggest(
        condition: WeatherCondition,
        feelsLike: Double,
        windSpeed: Double,
        rainProbability: Int,
        isDay: Boolean
    ): ActivitySuggestion {
        val comfort = outdoorComfort(condition, feelsLike, windSpeed, rainProbability)
        val list = mutableListOf<Activity>()

        fun outdoor(emoji: String, title: String, hint: String, adjust: Int = 0) {
            list += Activity(emoji, title, hint, (comfort + adjust).coerceIn(1, 10), false)
        }

        fun indoor(emoji: String, title: String, hint: String, adjust: Int = 0) {
            val base = 5 + (10 - comfort) / 2
            list += Activity(emoji, title, hint, (base + adjust).coerceIn(3, 10), true)
        }

        // Açık hava
        outdoor(
            "🏃", "Koşu", "Hafif tempoyla açılmak için ideal",
            adjust = (if (feelsLike > 26) -2 else 0) + (if (!isDay) -1 else 0)
        )
        outdoor(
            "🧺", "Piknik", "Battaniye, atıştırmalık, güzel bir park",
            adjust = (if (feelsLike < 16) -3 else 0) +
                    (if (windSpeed >= 20) -2 else 0) +
                    (if (!isDay) -5 else 0)
        )
        outdoor(
            "🚴", "Bisiklet", "Rüzgarı da hesaba kat",
            adjust = if (windSpeed >= 20) -2 else 0
        )
        outdoor("🚶", "Yürüyüş", "Telefonu cebe at, kulaklığı tak")

        if (condition == WeatherCondition.CLEAR && !isDay) {
            list += Activity("🔭", "Yıldız gözlemi", "Gökyüzü açık, şehirden biraz uzaklaş", comfort, false)
        }
        if (condition == WeatherCondition.SNOW) {
            list += Activity("⛄", "Kardan adam", "Eldivenleri unutma", 9, false)
        }

        // Ev içi ve kapalı mekan
        indoor("🎬", "Sinema", "Bir film, bir mısır")
        indoor(
            "📖", "Kitap ve sıcak içecek", "Pencere kenarı, battaniye",
            adjust = if (feelsLike < 10) 1 else 0
        )
        indoor(
            "⛸️", "Buz pateni", "Kapalı pistte hava durumu fark etmez",
            adjust = if (feelsLike < 8 || condition == WeatherCondition.SNOW) 1 else 0
        )
        indoor("🖼️", "Müze veya sergi", "Kapalı, sakin ve ilham verici")
        indoor("🍪", "Fırında bir şeyler", "Evi güzel kokutmanın tam zamanı")
        indoor("🎲", "Oyun gecesi", "Arkadaşlarla kutu oyunu")

        val headline = when {
            comfort >= 7 -> "Dışarısı keyifli, açık hava planları önde 🌿"
            comfort >= 4 -> "Hava idare eder, hem dışarısı hem içerisi olur"
            else -> "Hava dışarı çıkmaya pek uygun değil, içeride güzel planlar var 🏠"
        }

        return ActivitySuggestion(
            headline = headline,
            activities = list.sortedByDescending { it.score }.take(8)
        )
    }

    private fun outdoorComfort(
        condition: WeatherCondition,
        feelsLike: Double,
        wind: Double,
        rain: Int
    ): Int {
        var s = 10
        s -= when (condition) {
            WeatherCondition.STORM -> 9
            WeatherCondition.RAIN -> 7
            WeatherCondition.SNOW -> 4
            WeatherCondition.FOG -> 1
            else -> 0
        }
        s -= when {
            rain >= 60 -> 4
            rain >= 30 -> 2
            else -> 0
        }
        s -= when {
            wind >= 40 -> 4
            wind >= 25 -> 2
            else -> 0
        }
        s -= when {
            feelsLike <= -5 -> 5
            feelsLike <= 2 -> 3
            feelsLike < 8 -> 1
            feelsLike >= 35 -> 5
            feelsLike >= 30 -> 2
            else -> 0
        }
        return s.coerceIn(1, 10)
    }
}