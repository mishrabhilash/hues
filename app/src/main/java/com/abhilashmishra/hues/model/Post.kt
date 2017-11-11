package com.abhilashmishra.hues.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Created by abhilash.mishra on 11/11/17.
 */
class Post(
        @SerializedName("kind")
        var kind:String,

        @SerializedName("data")
        var postData: PostData
){
        override fun toString(): String {
                return Gson().toJson(this)
        }
}