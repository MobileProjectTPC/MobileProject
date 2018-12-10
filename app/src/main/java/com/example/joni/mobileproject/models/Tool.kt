package com.example.joni.mobileproject.models

import java.io.Serializable

class Tool(val image: String, val name: String, val uid: String): Serializable{
    constructor(): this("", "", "")
}