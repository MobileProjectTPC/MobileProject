package com.example.joni.mobileproject.models

import java.io.Serializable

class Image(val imageId: String, val imageUrl: String, val title: String) : Serializable {
    constructor(): this("", "", "")
}