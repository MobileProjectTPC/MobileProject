package com.example.joni.mobileproject

import java.io.Serializable

class Image(val imageId: String, val imageUrl: String, val title: String): Serializable {
    constructor(): this("", "", "")
}