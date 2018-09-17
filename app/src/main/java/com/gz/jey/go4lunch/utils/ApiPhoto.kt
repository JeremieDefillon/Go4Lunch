package com.gz.jey.go4lunch.utils

object ApiPhoto {
    /**
     * GET PHOTO URL
     * @param dimen Int
     * @param ref String
     * @param key String
     * @return photo's url : String
     */
    fun getPhotoURL(dimen :Int, ref :String, key : String ) : String{
        val max = dimen.toString()
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$max&maxheight=$max&photoreference=$ref&key=$key"
    }

}