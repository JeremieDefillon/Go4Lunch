package com.gz.jey.go4lunch.models

data class DetailsOpeningHours(
        val openNow: Boolean? = null,
        val periods: List<Period>? = null,
        val weekdayText: List<String>? = null)