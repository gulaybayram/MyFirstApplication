package com.example.myfirstapplication.ui.util

object WeatherAdvisor {

    fun advice(
        condition: WeatherCondition,
        feelsLike: Double,
        rainProbability: Int,
        windSpeed: Double
    ): String = when {
        condition == WeatherCondition.STORM ->
            "Gök gürültülü fırtına var, mümkünse içeride kal ⛈️"
        condition == WeatherCondition.RAIN || rainProbability >= 60 ->
            "Şemsiyesiz çıkarsan sırılsıklam olabilirsin ☂️"
        condition == WeatherCondition.SNOW ->
            "Kar yağıyor, kaymayan ayakkabını giy ❄️"
        feelsLike <= 0 ->
            "Buz gibi bir hava var, sıkı giyinmeden çıkma 🧣"
        feelsLike <= 10 ->
            "Hava serin, ceketini almayı unutma 🧥"
        feelsLike >= 32 ->
            "Çok sıcak, yanına su al ve gölgeyi seç 🥵"
        windSpeed >= 40 ->
            "Rüzgar sert, hafif eşyalarını sıkı tut 💨"
        condition == WeatherCondition.CLEAR ->
            "Hava harika, kendine bir dışarı çıkma borcun var ☀️"
        else ->
            "Sakin bir gün, rahatça çıkabilirsin 🙂"
    }
}