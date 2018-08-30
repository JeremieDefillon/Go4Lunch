package com.gz.jey.go4lunch.models

import kotlin.collections.ArrayList

class User(uid: String, username: String, mail: String, urlPicture: String, whereEat : String, restLiked : ArrayList<String>) {

    var uid: String = uid
    var mail: String = mail
    var username: String = username
    var urlPicture: String = urlPicture
    var whereEat: String = whereEat
    var restLiked : ArrayList<String> = restLiked
}