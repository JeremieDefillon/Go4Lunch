package com.gz.jey.go4lunch.api

import com.gz.jey.go4lunch.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


object UserHelper {

    private val COLLECTION_NAME : String = "users"

    // --- COLLECTION REFERENCE ---

    fun getUsersCollection() : CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    // --- CREATE ---

    fun createUser(uid : String, username : String, email: String, urlPicture : String, whereEat : String, restLiked : ArrayList<String>) : Task<Void> {
        val userToCreate = User(uid, username, email, urlPicture, whereEat, restLiked)
        return UserHelper.getUsersCollection().document(uid).set(userToCreate)
    }

    // --- UPDATE ---

    fun updateWhereEat(uid : String, whereEat : String) : Task<Void> {
        return UserHelper.getUsersCollection().document(uid).update("whereEat", whereEat)
    }

    fun updateRestLiked(uid : String, restLiked: ArrayList<String>) : Task<Void> {
        return UserHelper.getUsersCollection().document(uid).update("restLiked", restLiked)
    }

    // --- GET ---

    fun getUser(uid : String) : Task<DocumentSnapshot>{
        return UserHelper.getUsersCollection().document(uid).get()
    }

}