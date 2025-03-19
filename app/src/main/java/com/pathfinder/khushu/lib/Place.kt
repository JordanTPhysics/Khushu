package com.pathfinder.khushu.lib

import com.pathfinder.khushu.utils.GeofenceService

data class Place(val name: String, val lat: Double, val lng: Double, val address: String, val type: String) {

    val geofenceService = GeofenceService()

    fun toJson(): String {
        return "$name,$lat,$lng,$address,$type"
    }

    fun fromJson(json: String): Place {
        val (name, lat, lng, address, type) = json.split(",")
        return Place(name, lat.toDouble(), lng.toDouble(), address, type)
    }

    fun hasGeofence(): Boolean {
        return geofenceService.getCurrentGeofences().any { it.requestId == name }
    }

}


