package com.gz.jey.go4lunch.utils


object CalculateRatio{

    fun getRateOn3(rate : Double) : Int{
        return Math.round((rate / 5.5) * 3).toInt()
    }

    fun getLike(peeps : Int, total : Int) : Int{
        return Math.round((peeps.toDouble() / total.toDouble()) * 3).toInt()
    }
}