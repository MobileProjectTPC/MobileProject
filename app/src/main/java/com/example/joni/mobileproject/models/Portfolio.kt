package com.example.joni.mobileproject.models

import java.io.Serializable

class Portfolio(var date: String?, var images:ArrayList<Image>, var name: String?, var pdfs:ArrayList<PDF>?, var progresses: ArrayList<Progress>?, var summary: String?, var tool: String?, var uid: String, var user: String, var videos: ArrayList<Video>?, var workspace: String?): Serializable {

    constructor(): this("", ArrayList<Image>(), "", ArrayList<PDF>(), ArrayList<Progress>(), "", "", "", "", ArrayList<Video>(), "")
}