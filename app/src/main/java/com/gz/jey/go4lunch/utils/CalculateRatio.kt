package com.gz.jey.go4lunch.utils


object CalculateRatio{

    /**
     * CONVERT GOOGLE RATING BY 3 AS MAX
     * @param rate Double
     * @return Int
     */
    fun getRateOn3(rate : Double) : Int{
        return Math.round((rate / 5.5) * 3).toInt()
    }

    /**
     * CONVERT PEOPLES LIKE FROM TOTAL PEEPS AS MAX
     * @param peeps Int
     * @param total  Int
     * @return Int
     */
    fun getLike(peeps : Int, total : Int) : Int{
        return Math.round((peeps.toDouble() / total.toDouble()) * 3).toInt()
    }
}