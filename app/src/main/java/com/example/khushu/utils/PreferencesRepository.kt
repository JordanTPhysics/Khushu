package com.example.khushu.utils

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.khushu.lib.Place

class PreferencesRepository(private val sharedPreferences: SharedPreferences) {

    private val _places = MutableLiveData<List<Place>>()
    private var _previousDndMode = -1

    val places: LiveData<List<Place>> = _places  // Expose LiveData
    val previousDndMode: Int = _previousDndMode
    val geofences = mutableListOf<com.google.android.gms.location.Geofence>()


    init {
        loadPlaces()  // Load saved places when the repository is initialized
        getDndMode()
    }

    // 🔹 Use this method in ViewModel to get LiveData of places
    fun getPlacesLiveData(): LiveData<List<Place>> {
        return places
    }

    fun getPlacesSync(): List<Place>? {
        return places.value
    }

    fun addPlace(place: Place) {
        val currentPlaces = places.value.orEmpty().toMutableList()
        currentPlaces.add(place)
        savePlaces(currentPlaces)
    }

    // 🔹 Fetch and update LiveData (_places) from SharedPreferences
    private fun loadPlaces() {
        val savedPlaces = sharedPreferences.getStringSet("SavedPlaces", emptySet())
        val placesList = savedPlaces?.mapNotNull { json ->
            json.toPlaceOrNull()  // Convert JSON to Place safely
        } ?: emptyList()

        _places.value = placesList
    }

    // 🔹 Saves places to SharedPreferences and updates LiveData
    fun savePlaces(places: List<Place>) {
        val jsonPlaces = places.map { it.toJson() }.toSet()
        sharedPreferences.edit().putStringSet("SavedPlaces", jsonPlaces).apply()

        _places.value = places  // Update LiveData
    }

    fun saveDndMode(dndMode: Int) {
        sharedPreferences.edit().putInt("dndMode", dndMode).apply()
    }

    fun getDndMode() {
        _previousDndMode = sharedPreferences.getInt("dndMode", -1)
    }
}

fun String.toPlaceOrNull(): Place? {
    return try {
        val parts = split(",")
        Place(parts[0], parts[1].toDouble(), parts[2].toDouble(), parts[3], parts[4])
    } catch (e: Exception) {
        null  // Return null if parsing fails
    }
}
