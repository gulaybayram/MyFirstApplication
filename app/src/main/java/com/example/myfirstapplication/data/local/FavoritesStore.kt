package com.example.myfirstapplication.data.local

import android.content.Context
import com.example.myfirstapplication.data.model.FavoriteCity
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesStore private constructor(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _favorites = MutableStateFlow(load())
    val favorites: StateFlow<List<FavoriteCity>> = _favorites.asStateFlow()

    fun toggle(city: FavoriteCity) {
        val current = _favorites.value
        val exists = current.any { it.latitude == city.latitude && it.longitude == city.longitude }
        val updated = if (exists) {
            current.filterNot { it.latitude == city.latitude && it.longitude == city.longitude }
        } else {
            current + city
        }
        _favorites.value = updated
        prefs.edit().putString(KEY, gson.toJson(updated)).apply()
    }

    private fun load(): List<FavoriteCity> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            gson.fromJson(json, Array<FavoriteCity>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val KEY = "favorite_cities"

        @Volatile
        private var instance: FavoritesStore? = null

        fun get(context: Context): FavoritesStore =
            instance ?: synchronized(this) {
                instance ?: FavoritesStore(context).also { instance = it }
            }
    }
}