package com.pathfinder.khushu.ui.mapview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.pathfinder.khushu.R
import com.pathfinder.khushu.lib.Place
import com.pathfinder.khushu.utils.PreferencesRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context, private val preferencesRepository: PreferencesRepository) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.info_window_layout, null)
        val title = view.findViewById<TextView>(R.id.title)
        val address = view.findViewById<TextView>(R.id.address)
        val addButton = view.findViewById<Button>(R.id.add_button)
        val place = Place(
            marker.title?: "",
            marker.position.latitude,
            marker.position.longitude,
            marker.snippet?: "",
            ""
        )
        title.text = marker.title
        address.text = marker.snippet

        addButton.setOnClickListener {
            preferencesRepository.addPlace(place)
        }


        return view
    }
}