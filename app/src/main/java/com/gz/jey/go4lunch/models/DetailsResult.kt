package com.gz.jey.go4lunch.models

data class DetailsResult(
        val formatted_phone_number: String? = null,
        val international_phone_number: String? = null,
        val place_id: String? = null,
        val name: String? = null,
        val vicinity: String? = null,
        val opening_hours: DetailsOpeningHours? = null,
        val website: String? = null,
        val photos: List<Photo>? = null,
        val rating: Double? = null,
        val types: List<String>? = null,
        var liked: Int = 0
)

