package com.example.myfirstapplication.data

import com.example.myfirstapplication.data.model.City

object SampleData {
    val cities = listOf(
        City(1, "Ankara", 39.92, 32.85, "Türkiye", "TR", "Ankara"),
        City(2, "İstanbul", 41.01, 28.95, "Türkiye", "TR", "İstanbul"),
        City(3, "İzmir", 38.42, 27.14, "Türkiye", "TR", "İzmir")
    )
}