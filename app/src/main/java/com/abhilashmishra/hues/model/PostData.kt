package com.abhilashmishra.hues.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Created by abhilash.mishra on 11/11/17.
 */
class PostData(
        @SerializedName("thumbnail")
        var thumbnail: String,

        @SerializedName("post_hint")
        var postHint: String,

        @SerializedName("author")
        var author: String,

        @SerializedName("url")
        var url: String) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}