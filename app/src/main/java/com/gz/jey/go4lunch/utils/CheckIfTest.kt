package com.gz.jey.go4lunch.utils

object CheckIfTest {

    private var isTheRunningTest: Boolean? = null

        fun isRunningTest(test: String) : Boolean {
            if (null == isTheRunningTest) {
                val isTest : Boolean = try {
                    Class.forName("com.gz.jey.go4lunch.$test")
                    true
                } catch (e: ClassNotFoundException) {
                    false
                }

                isTheRunningTest = isTest
            }

            return isTheRunningTest as Boolean
        }
}
