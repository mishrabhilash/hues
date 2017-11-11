package com.abhilashmishra.hues.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abhilash.mishra on 11/12/17.
 */
class Subreddit(
        @SerializedName("name")
        var name:String?,

        @SerializedName("after")
        var after: String? = null
)