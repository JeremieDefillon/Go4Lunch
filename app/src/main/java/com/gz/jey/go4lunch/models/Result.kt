package com.gz.jey.go4lunch.models

data class Result(
        val formatted_address: String? = null,
        val geometry: Geometry? = null,
        val icon: String? = null,
        val id: String? = null,
        val name: String? = null,
        val opening_hours: OpeningHours? = null,
        val photos: List<Photo>? = null,
        val place_id: String? = null,
        val rating: Double? = null,
        val reference: String? = null,
        var distance: Double? = null,
        val types: List<String>? = null,
        var liked: Int = 0,
        val workmates: ArrayList<Contact> = ArrayList()
)
