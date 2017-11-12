package com.abhilashmishra.hues

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.service.dreams.DreamService
import android.support.v7.graphics.Palette
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.abhilashmishra.hues.model.ListingResponse
import com.abhilashmishra.hues.model.PostData
import com.abhilashmishra.hues.model.Subreddit
import com.abhilashmishra.hues.network.Network
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
    var lastBackgroundColor = Color.TRANSPARENT

    //Views
    lateinit var imageView: ImageView
    lateinit var authorName: TextView
    lateinit var totalBackground: View

    val paletteListener: Palette.PaletteAsyncListener = Palette.PaletteAsyncListener { palette ->
        palette?.let {
            val colorFrom: Int = lastBackgroundColor
            val colorTo: Int = palette.getVibrantColor(Color.TRANSPARENT)
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.duration = 250; // milliseconds
            colorAnimation.addUpdateListener { animator -> totalBackground.setBackgroundColor(animator.animatedValue as Int) }
            colorAnimation.start();
            lastBackgroundColor = colorTo
        }
    }

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
        totalBackground = findViewById(R.id.totalBackground)
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
                                fetchPosts(subreddit.name, subreddit.after, null)?.
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
        subscriptions.add(Flowable.interval(20000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSubscriber<Long>() {
                    override fun onComplete() {

                    }

                    override fun onNext(t: Long?) {
                        lastShownImageIndex++
                        if (lastShownImageIndex == imageList.size - 1) {
                            subscriptions.clear()
                            lastShownImageIndex = 0
                            fetchImages()
                        }

                        Glide.with(applicationContext)
                                .load(imageList[lastShownImageIndex].url)
                                .transition(DrawableTransitionOptions.withCrossFade(800))
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                        return false
                                    }

                                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                        if (resource is BitmapDrawable) {
                                            Palette.from(resource.bitmap).generate(paletteListener)
                                        }
                                        return false
                                    }

                                })
                                .into(imageView)

                        authorName.text = "author: ${imageList[lastShownImageIndex].author}"

                    }

                    override fun onError(t: Throwable?) {

                    }

                }))
    }
}