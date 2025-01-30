package com.example.khushu.utils

import android.content.SharedPreferences
import com.example.khushu.lib.Place

class PreferencesRepository(private val sharedPreferences: SharedPreferences) {


    fun getPlaces(): Set<Place>? {
        return sharedPreferences.getStringSet("SavedPlaces", emptySet())?.let { places ->
            return places.map { place ->
                val (name, lat, lng, address, type) = place.split(",")
                Place(name, lat.toDouble(), lng.toDouble(), address, type)
            }.toSet()
        }
    }

    fun savePlaces(places: Set<Place>) {
        val jsonPlaces = places.map { it.toJson() }.toSet()
        sharedPreferences.edit().putStringSet("SavedPlaces", jsonPlaces).apply()
    }
}
