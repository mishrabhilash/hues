package com.abhilashmishra.hues.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Created by abhilash.mishra on 11/11/17.
 */
class ListingResponse(
        @SerializedName("kind")
        var kind: String,

        @SerializedName("data")
        var data: ListingResponseData


) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}