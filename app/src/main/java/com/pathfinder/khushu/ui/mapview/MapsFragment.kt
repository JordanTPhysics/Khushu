package com.pathfinder.khushu.ui.mapview

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.location.Location
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.pathfinder.khushu.MainViewModel
import com.pathfinder.khushu.R
import com.pathfinder.khushu.lib.MainViewModelFactory
import com.pathfinder.khushu.lib.Place
import com.pathfinder.khushu.utils.LocationService
import com.pathfinder.khushu.utils.PreferencesRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MapsFragment : Fragment() {

    private lateinit var locationService: LocationService
    private lateinit var mainViewModel: MainViewModel
    private lateinit var categorySpinner: Spinner
    private lateinit var radiusSpinner: Spinner
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private fun getUserLocation(googleMap: GoogleMap) {
        locationService.getLastLocation { location: Location? ->
            location?.let {
                val userPosition = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 15f))
            }
        }
    }

    private fun showAddCustomPlaceDialog(latLng: LatLng) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_place_dialog, null)
        val editTextPlaceName = dialogView.findViewById<EditText>(R.id.editTextPlaceName)

        val dialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Add Custom Place")
            setView(dialogView)
            setPositiveButton("Add") { _, _ ->
                val placeName = editTextPlaceName.text.toString()
                if (placeName.isNotEmpty()) {
                    val place = Place(
                        placeName,
                        latLng.latitude,
                        latLng.longitude,
                        "$placeName at ${latLng.latitude}, ${latLng.longitude}",
                        "custom"
                    )
                    mainViewModel.addPlace(place)
                    Toast.makeText(requireContext(), "Place added", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Place name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel", null)
        }.create()

        dialog.show()
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

    private fun showExistingPlaceDialog(place: Place) {
        val dialog = AlertDialog.Builder(requireContext()).apply {
            setTitle(place.name)
            setMessage("${place.address}\n\n ${place.type} at ${mainViewModel.prettifyDouble(place.lat)}, ${mainViewModel.prettifyDouble(place.lng)}")
            setPositiveButton("Remove") { _, _ ->
                mainViewModel.removePlace(place)
                Toast.makeText(requireContext(), "Place removed", Toast.LENGTH_SHORT).show()
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

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        categorySpinner = view.findViewById(R.id.category_spinner)
        radiusSpinner = view.findViewById(R.id.radius_spinner)

        locationService = LocationService(requireContext())

        if (locationService.isPermissionGranted()) {
            mapFragment?.getMapAsync { googleMap ->
                getUserLocation(googleMap)
                googleMap.isMyLocationEnabled = true
                googleMap.setOnMarkerClickListener { marker ->
                    val place = Place(
                        marker.title ?: "",
                        marker.position.latitude,
                        marker.position.longitude,
                        marker.snippet ?: "",
                        categorySpinner.selectedItem.toString()
                    )
                    if (mainViewModel.doesPlaceNameLocationExist(place)) {
                        mainViewModel.getPlaceByNameLocation(place)
                            ?.let { showExistingPlaceDialog(it) }
                    } else {
                        showAddPlaceDialog(place)
                    }
                    true
                }

                googleMap.setOnMapClickListener { latLng ->
                    showAddCustomPlaceDialog(latLng)
                }
                mainViewModel.places.observe(viewLifecycleOwner) { places ->
                    if (places.isNotEmpty()) {
                        mainViewModel.addCirclesToMap(googleMap)
                        mainViewModel.addExistingPlacesToMap(googleMap)
                    }
                }

                categorySpinner.post {
                    categorySpinner.dropDownVerticalOffset = -categorySpinner.height
                }

                radiusSpinner.post {
                    radiusSpinner.dropDownVerticalOffset = -radiusSpinner.height
                }

            }
        } else {
            locationService.requestPermission(requireActivity(), LOCATION_PERMISSION_REQUEST_CODE)
        }

        val searchButton = view.findViewById<Button>(R.id.search_button)

        searchButton.setOnClickListener {
            mapFragment?.getMapAsync { googleMap ->
                mainViewModel.fetchNearbyPlaces(
                    googleMap,
                    mainViewModel.latLngToLocation(googleMap.cameraPosition.target),
                    categorySpinner.selectedItem.toString(),
                    radiusSpinner.selectedItem.toString()
                )
            }
        }
    }
}


