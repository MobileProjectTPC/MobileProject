package com.example.joni.mobileproject.models

class Image(val imageId: String, val imageUrl: String, val title: String){
    constructor(): this("", "", "")
}