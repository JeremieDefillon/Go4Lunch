package com.gz.jey.go4lunch.models

data class Place (
        val htmlAttributions: List<Any>? = null,
        val nextPageToken: String? = null,
        var results: List<Result>? = null,
        val status: String? = null
)