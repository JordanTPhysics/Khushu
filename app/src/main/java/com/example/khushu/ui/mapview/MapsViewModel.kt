package com.example.khushu.ui.mapview

import android.location.Location
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.khushu.R
import com.example.khushu.lib.Place
import com.example.khushu.utils.PreferencesRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val context: Context
) : ViewModel() {

    // Backing property for the list of items
    private val _places = MutableLiveData<List<Place>?>(preferencesRepository.getPlaces()?.toList())
    val places: LiveData<List<Place>?> = _places

    // Add an item to the list
    fun addPlace(place: Place) {
        val updatedList = _places.value?.toMutableList() ?: mutableListOf()
        updatedList.add(place)
        _places.value = updatedList
        preferencesRepository.savePlaces(updatedList.toMutableSet())
    }

    fun fetchNearbyPlaces(googleMap: GoogleMap, location: Location, placeType: String) {
        val apiKey = context.getString(R.string.google_maps_key)
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${location.latitude},${location.longitude}&radius=1500&type=${placeType}&key=$apiKey"
        println("creating web request to $url")
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val results = response.getJSONArray("results")
                for (i in 0 until results.length()) {
                    val place = results.getJSONObject(i)
                    val lat = place.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                    val lng = place.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                    val address = place.getString("vicinity")
                    val name = place.getString("name")
                    val latLng = LatLng(lat, lng)
                    val marker = googleMap.addMarker(MarkerOptions().position(latLng).title(name).snippet(address))
                    println("Adding marker for $name at $latLng")
                    marker?.tag = Place(name, lat, lng, address, placeType)
                }
            },
            { error ->
                println("Error: $error")
            }
        )

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }
}