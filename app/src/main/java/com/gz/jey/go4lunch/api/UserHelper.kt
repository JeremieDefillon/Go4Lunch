package com.gz.jey.go4lunch.api

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.gz.jey.go4lunch.models.User
import java.util.*


object UserHelper {

    private const val COLLECTION_NAME : String = "users"

    /**
     * GET USER'S COLLECTION
     * @return CollectionReference
     */
    fun getUsersCollection() : CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    /**
     * CREATE USER
     * @param uid String
     * @param username String
     * @param email String
     * @param urlPicture String
     * @param whereEatID String
     * @param whereEatName String
     * @param whereEatDate String
     * @param restLiked ArrayList<String>
     * @return Task<Void>
     */
    fun createUser(uid : String, username : String, email: String, urlPicture : String, whereEatID : String, whereEatName : String, whereEatDate: String, restLiked : ArrayList<String>) : Task<Void> {
        val userToCreate = User(uid, username, email, urlPicture, whereEatID, whereEatName, whereEatDate, restLiked)
        return UserHelper.getUsersCollection().document(uid).set(userToCreate)
    }

    /**
     * UPDATE USER
     * @param uid String
     * @param user User
     * @return Task<Void>
     *     */
    fun updateUser(uid : String, user : User) : Task<Void> {
        return UserHelper.getUsersCollection().document(uid).set(user)
    }

    /*
    /**
     * UPDATE CONTACT
     * @param uid String
     * @param contact Contact
     * @return Task<Void>
     *     */
    fun updateContact(uid : String, contact : Contact) : Task<Void> {
        return UserHelper.getUsersCollection().document(uid).set(contact)
    }*/


    /**
     * GET USER
     * @param uid String
     * @return Task<DocumentSnapshot>
     *     */
    fun getUser(uid : String) : Task<DocumentSnapshot>{
        return UserHelper.getUsersCollection().document(uid).get()
    }

}