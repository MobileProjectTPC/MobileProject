package com.example.joni.mobileproject.models

class Video(val videoId: String, val videoUrl: String, val title: String){
    constructor(): this("", "", "")
}