package com.example.discover.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.example.discover.DiscoverApplication
import java.io.BufferedInputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URL
import java.util.*
import kotlin.concurrent.withLock

//class LoadPoster(
//    private val imageView: WeakReference<ImageView>,
//    private val activity: WeakReference<Activity>
//) : AsyncTask<String, Unit, Bitmap>() {
//
//    override fun doInBackground(vararg params: String?): Bitmap? {
//        val fileName = params[0]
//        val inputStream: InputStream?
//
//        var bitmap: Bitmap? = null
//
//        if (fileName != null) {
//            try {
//                val app = (activity.get()?.application as DiscoverApplication)
//                val key = createKey(fileName)
//                bitmap = checkInMemory(key, app)
//                if (bitmap != null)
//                    return bitmap
//
//                val url = "https://image.tmdb.org/t/p/w154/$fileName"
//
//                inputStream = URL(url).openStream()
//                bitmap = inputStream?.let { createScaledBitmapFromStream(inputStream) }
//
//                if (bitmap != null) {
//                    writeInMemory(key, bitmap, app)
//
//                }
//            } catch (e: Exception) {
//                Log.d("LoadImage", "Error occurred. ${e.message}")
//                this.cancel(true)
//            }
//        }
//        return bitmap
//    }
//
//    override fun onPostExecute(result: Bitmap?) {
//        if (result != null) {
//            imageView.get()?.scaleType = ImageView.ScaleType.CENTER_CROP
//            imageView.get()?.setImageBitmap(result)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                imageView.get()?.viewTreeObserver?.addOnPreDrawListener(object :
//                    ViewTreeObserver.OnPreDrawListener {
//                    override fun onPreDraw(): Boolean {
//                        imageView.get()?.viewTreeObserver?.removeOnPreDrawListener(this)
//                        activity.get()?.startPostponedEnterTransition()
//                        return true
//                    }
//                })
//            }
//        }
//    }
//
//    private fun createScaledBitmapFromStream(
//        s: InputStream,
//        minimumDesiredBitmapWidth: Int = 360,
//        minimumDesiredBitmapHeight: Int = 540
//    ): Bitmap? {
//        val stream = BufferedInputStream(s, 8 * 1024)
//        val decodeBitmapOptions = BitmapFactory.Options()
//        if (minimumDesiredBitmapWidth > 0 && minimumDesiredBitmapHeight > 0) {
//            val decodeBoundsOptions = BitmapFactory.Options()
//            decodeBoundsOptions.inJustDecodeBounds = true
//            stream.mark(8 * 1024)
//            BitmapFactory.decodeStream(stream, null, decodeBoundsOptions)
//            stream.reset()
//            val originalWidth: Int = decodeBoundsOptions.outWidth
//            val originalHeight: Int = decodeBoundsOptions.outHeight
//
//            val scale =
//                (originalWidth / minimumDesiredBitmapWidth).coerceAtMost(originalHeight / minimumDesiredBitmapHeight)
//
//            val bitmap: Bitmap?
//            if (scale > 1) {
//                decodeBitmapOptions.inSampleSize = 1.coerceAtLeast(scale)
//                bitmap = BitmapFactory.decodeStream(stream, null, decodeBitmapOptions)
//
//            } else
//                bitmap = BitmapFactory.decodeStream(stream)
//            stream.close()
//            return bitmap
//        }
//        return null
//    }
//
//    private fun createKey(path: String): String {
//        return path.substringAfter('/').substringBefore('.').toLowerCase(Locale.ENGLISH)
//    }
//
//    private fun getBitmapFromDiskCache(key: String, application: DiscoverApplication): Bitmap? {
//        if (application.containsKey(key)) {
//            val bitmap = application.getBitmap(key)
//            Log.d("disk", "$bitmap")
//            application.memoryCache.put(key, bitmap!!)
//            Log.d("memory", "written")
//            return bitmap
//        }
//
//        return null
//    }
//
//    private fun checkInMemory(key: String, application: DiscoverApplication): Bitmap? {
//        val image = fetchMemoryCacheImage(key, application)
//        if (image != null) {
//            Log.d("memory", "read")
//            return image
//        }
//        return getBitmapFromDiskCache(key, application)
//    }
//
//    private fun fetchMemoryCacheImage(key: String, application: DiscoverApplication): Bitmap? {
//        val memoryCache = (application).memoryCache
//        return memoryCache[key]
//    }
//
//    private fun writeInMemory(key: String, bitmap: Bitmap, application: DiscoverApplication) {
//        application.apply {
//            memoryCache.put(key, bitmap)
//            diskCacheLock.withLock {
//                if (!containsKey(key)) {
//                    put(key, bitmap)
//                }
//            }
//        }
//    }
//}