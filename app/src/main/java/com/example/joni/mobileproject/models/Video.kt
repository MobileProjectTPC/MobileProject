package com.example.joni.mobileproject.models

import java.io.Serializable

class Video(val videoId: String, val videoUrl: String, val title: String): Serializable{
    constructor(): this("", "", "")
}