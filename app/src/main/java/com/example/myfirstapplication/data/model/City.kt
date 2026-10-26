package com.example.myfirstapplication.data.model

import com.google.gson.annotations.SerializedName

data class City(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    @SerializedName("country_code") val countryCode: String? = null,
    val admin1: String? = null
)