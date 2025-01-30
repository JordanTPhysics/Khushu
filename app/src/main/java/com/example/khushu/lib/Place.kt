package com.example.khushu.lib

data class Place(val name: String, val lat: Double, val lng: Double, val address: String, val type: String) {

    fun toJson(): String {
        return "$name,$lat,$lng,$address,$type"
    }

    fun fromJson(json: String): Place {
        val (name, lat, lng, address, type) = json.split(",")
        return Place(name, lat.toDouble(), lng.toDouble(), address, type)
    }

}


