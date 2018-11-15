package com.example.joni.mobileproject.slidingImages

class ImageModel {

    private var imageDrawable: Int = 0

    fun getImageDrawables(): Int {
        return imageDrawable
    }

    fun setImageDrawables(image_drawable: Int) {
        this.imageDrawable = image_drawable
    }
}