package com.abhilashmishra.hues.network

import com.abhilashmishra.hues.model.ListingResponse
import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


/**
 * Created by abhilash.mishra on 11/11/17.
 */
interface RedditApi {
    @GET("r/{subredditName}")
    fun fetchPosts(@Path("subredditName") subredditName: String?,
                   @Query("after") after: String?,
                   @Query("limit") limit: Int?): Flowable<ListingResponse?>
}