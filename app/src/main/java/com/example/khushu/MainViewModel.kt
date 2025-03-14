package com.example.khushu

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.khushu.lib.Place
import com.example.khushu.utils.GeofenceService
import com.example.khushu.utils.PreferencesRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class MainViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val context: Context
) : ViewModel() {

    private val _geofences = MutableLiveData<List<Geofence>>()
    val geofences: LiveData<List<Geofence>> get() = _geofences

    private val geofenceService: GeofenceService = GeofenceService()

    init {
        geofenceService.geofenceLiveData.observeForever { geofences ->
            _geofences.value = geofences
        }
    }

    val places: LiveData<List<Place>> = preferencesRepository.getPlacesLiveData()

    fun addPlace(place: Place) {
        val currentPlaces = places.value.orEmpty().toMutableList()
        currentPlaces.add(place)
        preferencesRepository.savePlaces(currentPlaces)  // Saves and updates LiveData
    }

    fun removePlace(place: Place) {
        val currentPlaces = places.value.orEmpty().toMutableList()
        currentPlaces.remove(place)
        preferencesRepository.savePlaces(currentPlaces)
    }

    fun doesPlaceExist(place: Place): Boolean {
        return places.value.orEmpty().contains(place)
    }

    fun fetchNearbyPlaces(googleMap: GoogleMap, location: Location, placeType: String) {
        val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${location.latitude},${location.longitude}&radius=1500&type=${placeType}&key=${apiKey}"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val results = response.getJSONArray("results")
                for (i in 0 until results.length()) {
                    val place = results.getJSONObject(i)
                    val lat =
                        place.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                    val lng =
                        place.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                    val address = place.getString("vicinity").replace(",", " ")
                    val name = place.getString("name")
                    val latLng = LatLng(lat, lng)
                    val marker = googleMap.addMarker(
                        MarkerOptions().position(latLng).title(name).snippet(address)
                    )

                    val placeDataObject = Place(name, lat, lng, address, placeType)
                    marker?.setIcon(BitmapDescriptorFactory
                        .defaultMarker(if (doesPlaceExist(placeDataObject)) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_RED))  // How to set color of
                    marker?.tag = placeDataObject
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