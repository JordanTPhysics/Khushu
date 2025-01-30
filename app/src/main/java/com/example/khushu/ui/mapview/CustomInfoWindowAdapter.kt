package com.example.khushu.ui.mapview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.khushu.R
import com.example.khushu.lib.Place
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.info_window_layout, null)
        val title = view.findViewById<TextView>(R.id.title)
        val address = view.findViewById<TextView>(R.id.address)
        val addButton = view.findViewById<Button>(R.id.add_button)

        title.text = marker.title
        address.text = marker.snippet

        addButton.setOnClickListener {
            // Handle adding the place to local storage
            val place = marker.tag as Place
            // Add place to local storage
        }

        return view
    }
}