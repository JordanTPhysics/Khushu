package com.example.khushu.ui.mapview

import android.app.AlertDialog
import android.content.Context
import android.location.Location
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.khushu.MainViewModel
import com.example.khushu.R
import com.example.khushu.lib.MainViewModelFactory
import com.example.khushu.lib.Place
import com.example.khushu.utils.LocationService
import com.example.khushu.utils.PreferencesRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.example.khushu.utils.GeofenceService

class MapsFragment : Fragment() {

    private lateinit var locationService: LocationService
    private lateinit var mainViewModel: MainViewModel
    private lateinit var dropdown: Spinner
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private fun getUserLocation(googleMap: GoogleMap) {
        locationService.getLastLocation { location: Location? ->
            location?.let {
                val userPosition = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 15f))
            }
        }
    }

    private fun showAddPlaceDialog(place: Place) {
        val dialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Add Place")
            setMessage("Do you want to add ${place.name} to your list?")
            setPositiveButton("Add") { _, _ ->
                mainViewModel.addPlace(place)
                Toast.makeText(requireContext(), "Place added", Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("Cancel", null)
        }.create()

        dialog.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val preferencesRepository = PreferencesRepository(sharedPreferences)
        val viewModelFactory = MainViewModelFactory(preferencesRepository, requireContext())

        mainViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(MainViewModel::class.java)


        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        dropdown = view.findViewById(R.id.category_spinner)

        locationService = LocationService(requireContext())

        if (locationService.isPermissionGranted()) {
            mapFragment?.getMapAsync { googleMap ->
                getUserLocation(googleMap)

                googleMap.setOnMarkerClickListener { marker ->
                    val place = Place(
                        marker.title ?: "",
                        marker.position.latitude,
                        marker.position.longitude,
                        marker.snippet ?: "",
                        dropdown.selectedItem.toString()
                    )

                    showAddPlaceDialog(place)
                    true
                }
            }
        } else {
            locationService.requestPermission(requireActivity(), LOCATION_PERMISSION_REQUEST_CODE)
        }

        val searchButton = view.findViewById<Button>(R.id.search_button)
//        val showGeofencesButton = view.findViewById<Button>(R.id.show_geofences_button)

        searchButton.setOnClickListener {
            locationService.getLastLocation { location: Location? ->
                location?.let {
                    mapFragment?.getMapAsync { googleMap ->
                        mainViewModel.fetchNearbyPlaces(
                            googleMap,
                            it,
                            dropdown.selectedItem.toString()
                        )
                    }
                }
            }
        }


    }
}

//        showGeofencesButton.setOnClickListener {
//            Toast.makeText(requireContext(), geofenceService.getCurrentGeofences().toString(), Toast.LENGTH_LONG).show()
//        }}

