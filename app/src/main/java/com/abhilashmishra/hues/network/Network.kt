package com.abhilashmishra.hues.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by abhilash.mishra on 11/11/17.
 */
class Network private constructor() {

    companion object {
        private val baseUrl = "https://www.reddit.com"
        private var instance: RedditApi? = null

        fun getRedditApi(context: Context): RedditApi? {
            if (instance == null) {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                val client = OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .build()

                val retrofit2 = Retrofit.Builder()
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(baseUrl)
                        .client(client)
                        .build()
                instance = retrofit2.create(RedditApi::class.java)
            }
            return instance;
        }
    }
}