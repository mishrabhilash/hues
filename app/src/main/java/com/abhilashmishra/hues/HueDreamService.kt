package com.abhilashmishra.hues

import android.service.dreams.DreamService
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.abhilashmishra.hues.model.ListingResponse
import com.abhilashmishra.hues.model.PostData
import com.abhilashmishra.hues.model.Subreddit
import com.abhilashmishra.hues.network.Network
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import java.util.concurrent.TimeUnit

/**
 * Created by abhilash.mishra on 11/4/17.
 */
class HueDreamService : DreamService() {

    val subredditList = arrayOf(Subreddit("earthporn.json"),
            Subreddit("MostBeautiful.json"),
            Subreddit("architecture.json"))

    val imageList = ArrayList<PostData>()
    val lastPageNumber = 0
    var lastShownImageIndex = -1
    var after: String? = null
    val subscriptions: CompositeDisposable = CompositeDisposable()

    //Views
    lateinit var imageView: ImageView
    lateinit var authorName: TextView

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // add initial options
        isInteractive = false
        isFullscreen = true
        isScreenBright = true

        // show the view on the screen
        setContentView(R.layout.hue_screen_layout)
        imageView = findViewById(R.id.image_view)
        authorName = findViewById(R.id.description)
        init()
    }

    private fun init() {
        fetchImages()
    }

    private fun fetchImages() {
        Flowable.merge(getAllFlowables())
                .subscribeOn(Schedulers.io())?.
                observeOn(AndroidSchedulers.mainThread())?.
                subscribeWith(object : DisposableSubscriber<ListingResponse>() {
                    override fun onNext(listingResponse: ListingResponse?) {
                        listingResponse?.data?.children?.forEach {
                            if (it.postData.postHint == "image") {
                                imageList.add(it.postData)
                            }
                            if (subscriptions.size() == 0) {
                                startImageView()
                            }
                        }
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable) {
                        Log.d("error", e.toString())
                    }

                })
    }

    private fun getAllFlowables(): List<Flowable<ListingResponse?>?> {

        return subredditList
                .map { subreddit ->
                    subreddit.name?.let {
                        Network.getRedditApi(this)?.
                                fetchPosts(subreddit.name, subreddit.after, 5)?.
                                map { listingResponse: ListingResponse? ->
                                    if (listingResponse != null) {
                                        subreddit.after = listingResponse.data.after
                                    }
                                    listingResponse
                                }
                    }
                }
                .toList()
    }

    private fun startImageView() {
        subscriptions.add(Flowable.interval(15000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSubscriber<Long>() {
                    override fun onComplete() {

                    }

                    override fun onNext(t: Long?) {
                        lastShownImageIndex++
                        if (lastShownImageIndex == imageList.size - 1) {
                            subscriptions.clear()
                            fetchImages()
                        }

                        Glide.with(applicationContext)
                                .load(imageList[lastShownImageIndex].url)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(imageView)

                        authorName.text = "author: ${imageList[lastShownImageIndex].author}"

                    }

                    override fun onError(t: Throwable?) {

                    }

                }))
    }
}