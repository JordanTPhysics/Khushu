package com.example.khushu.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.khushu.R
import com.example.khushu.lib.Place

class PlacesRecyclerAdapter(
    private var places: List<Place>,
    private val onDeleteClick: (Place) -> Unit
) : RecyclerView.Adapter<PlacesRecyclerAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val removeButton: Button = itemView.findViewById(R.id.removeButton)
        val placeName: TextView = itemView.findViewById(R.id.place_name)
        val address: TextView = itemView.findViewById(R.id.address)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.placeName.text = place.name
        holder.address.text = place.address
//        holder.itemView.background

        holder.removeButton.setOnClickListener {
            onDeleteClick(place)
        }
    }

    override fun getItemCount(): Int = places.size

    fun updatePlaces(newPlaces: List<Place>) {
        this.places = newPlaces
        notifyDataSetChanged()
    }
}