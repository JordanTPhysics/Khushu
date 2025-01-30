package com.example.khushu.ui.mapview

import android.content.Context
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import com.example.khushu.R
import com.example.khushu.lib.MapsViewModelFactory
import com.example.khushu.utils.LocationService
import com.example.khushu.utils.PreferencesRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng


class MapsFragment : Fragment() {

    private lateinit var locationService: LocationService
    private lateinit var mapsViewModel: MapsViewModel
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private fun getUserLocation(googleMap: GoogleMap) {
        locationService.getLastLocation { location: Location? ->
            location?.let {
                val userPosition = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 15f))}
                println("User location: $location")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val preferencesRepository = PreferencesRepository(sharedPreferences)

        mapsViewModel = ViewModelProvider(this, MapsViewModelFactory(preferencesRepository, requireContext())).get(MapsViewModel::class.java)
        locationService = LocationService(requireContext())

        if(locationService.isPermissionGranted()) {
            mapFragment?.getMapAsync { googleMap ->
                getUserLocation(googleMap)
            }
        } else {
            locationService.requestPermission(requireActivity(), LOCATION_PERMISSION_REQUEST_CODE)
        }

        val searchButton = view.findViewById<Button>(R.id.search_button)
        val dropdown = view.findViewById<Spinner>(R.id.category_spinner)
        searchButton.setOnClickListener {
            locationService.getLastLocation { location: Location? ->
                location?.let {
                    mapFragment?.getMapAsync { googleMap ->
                        mapsViewModel.fetchNearbyPlaces(googleMap, it, dropdown.selectedItem.toString())
                    }
                }
            }
        }
    }
}