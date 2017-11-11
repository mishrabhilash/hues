package com.abhilashmishra.hues.model

import com.google.gson.annotations.SerializedName

/**
 * Created by abhilash.mishra on 11/11/17.
 */

class ListingResponseData(
        @SerializedName("modhash")
        var modHash: String,

        @SerializedName("whitelist_status")
        var whitelistStatus: String,

        @SerializedName("after")
        var after: String,

        @SerializedName("before")
        var before: String,

        @SerializedName("children")
        var children: List<Post>
)

