package com.example.khushu

import android.content.Context
import android.graphics.Color
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.google.android.gms.maps.model.CircleOptions

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
        val placeToRemove = places.value.orEmpty().find { doesPlaceNameLocationExist(place) }
        val currentPlaces = places.value.orEmpty().toMutableList()
        currentPlaces.remove(placeToRemove)
        preferencesRepository.savePlaces(currentPlaces)
    }

    fun getPlaceByNameLocation(place: Place): Place? {
        return places.value.orEmpty().find { it.name == place.name && it.lat == place.lat && it.lng == place.lng }
    }

    fun doesPlaceExist(place: Place): Boolean {
        return places.value.orEmpty().contains(place)
    }

    fun doesPlaceNameLocationExist(place: Place): Boolean {
        return places.value.orEmpty().any { it.name == place.name && it.lat == place.lat && it.lng == place.lng }
    }

    fun latLngToLocation(latLng: LatLng): Location {
        val location = Location("")
        location.latitude = latLng.latitude
        location.longitude = latLng.longitude
        return location
    }

    fun prettifyDouble(double: Double): String {
        return String.format("%.2f", double)
    }

    fun addCirclesToMap(googleMap: GoogleMap) {
        val places = places.value.orEmpty()
        for (place in places) {
            val latLng = LatLng(place.lat, place.lng)
            googleMap.addCircle(
                CircleOptions()
                    .center(latLng)
                    .radius(50.0) // Example radius in meters
                    .strokeColor(Color.BLUE) // Border color
                    .strokeWidth(2f) // Border width
                    .fillColor(Color.argb(50, 0, 10, 175)) // Fill color with transparency
            )
        }
    }

    fun addExistingPlacesToMap(googleMap: GoogleMap) {
        val places = places.value.orEmpty()
        for (place in places) {
            val latLng = LatLng(place.lat, place.lng)
            val marker = googleMap.addMarker(
                MarkerOptions().position(latLng).title(place.name).snippet(place.address)
            )
            marker?.setIcon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))  // How to set color of
            marker?.tag = place
        }
    }

    fun selectionToRadius(selection: String): Int {
        return when (selection) {
            "1 km" -> 1000
            "2 km" -> 2000
            "5 km" -> 5000
            "10 km" -> 10000
            "20 km" -> 20000
            else -> 1500
        }

    }

    fun fetchNearbyPlaces(googleMap: GoogleMap, location: Location, placeType: String, radius: String) {

        val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${location.latitude},${location.longitude}&radius=${selectionToRadius(radius)}&type=${placeType}&key=${apiKey}"
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