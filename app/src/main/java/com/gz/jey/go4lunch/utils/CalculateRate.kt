package com.gz.jey.go4lunch.utils

object CalculateRate{

    fun getRateOn3(rate : Double) : Int{
        return Math.round((rate / 5.5) * 3).toInt()
    }
}