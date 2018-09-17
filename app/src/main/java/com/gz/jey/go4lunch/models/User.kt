package com.gz.jey.go4lunch.models

import java.util.*


class User(
        var uid: String,
        var username: String,
        var mail: String,
        var urlPicture: String,
        var whereEatID: String,
        var whereEatName: String,
        var whereEatDate: String,
        var restLiked: ArrayList<String>)