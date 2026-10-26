package com.example.myfirstapplication.ui.util

enum class WeatherCondition(val label: String) {
    CLEAR("Açık"),
    CLOUDY("Bulutlu"),
    FOG("Sisli"),
    RAIN("Yağmurlu"),
    SNOW("Karlı"),
    STORM("Fırtına");

    companion object {
        // WMO hava durumu kodları
        fun fromCode(code: Int): WeatherCondition = when (code) {
            0, 1 -> CLEAR
            2, 3 -> CLOUDY
            45, 48 -> FOG
            in 51..67, in 80..82 -> RAIN
            in 71..77, 85, 86 -> SNOW
            in 95..99 -> STORM
            else -> CLOUDY
        }
    }
}