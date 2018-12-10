package com.example.joni.mobileproject.models

import java.io.Serializable

class PDF(val PDFId: String, val PDFUrl: String, val title: String) : Serializable{
    constructor(): this("", "", "")
}