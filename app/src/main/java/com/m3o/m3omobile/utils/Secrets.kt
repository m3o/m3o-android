package com.m3o.m3omobile.utils

object Secrets {

    // Method calls will be added by gradle task hideSecret
    // Example : external fun getWellHiddenSecret(packageName: String): String

    init {
        System.loadLibrary("secrets")
    }

    external fun getCipherPassword(packageName: String = "com.m3o.m3omobile"): String
}