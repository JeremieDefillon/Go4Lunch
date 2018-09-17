package com.gz.jey.go4lunch.models

data class DetailsResult(
        val formatted_phone_number: String,
        val international_phone_number: String,
        val place_id: String,
        val name: String,
        val vicinity: String,
        val opening_hours: DetailsOpeningHours,
        val website: String,
        val photos: List<Photo>,
        val rating: Double,
        val types: List<String>,
        var liked: Int
)

