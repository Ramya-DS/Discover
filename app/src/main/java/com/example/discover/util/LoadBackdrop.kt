package com.example.discover.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import com.example.discover.DiscoverApplication
import java.io.BufferedInputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URL
import java.util.*
import kotlin.concurrent.withLock

class LoadBackdrop(
    private val cardView: WeakReference<ImageView>,
    private val activity: WeakReference<Activity>
) : AsyncTask<String, Unit, Bitmap>() {
    override fun doInBackground(vararg params: String?): Bitmap? {
        val fileName = params[0]
        val width = params[1]!!.toInt()
        val saveToCache = params[2]?.toBoolean()
        val height = (width / 1.77777).toInt()
        val inputStream: InputStream?
        val app = (activity.get()!!.application as DiscoverApplication)
        var bitmap: Bitmap? = null

        if (fileName != null) {
            val key = createKey(fileName)
            bitmap =
                if (saveToCache == null) fetchDiskImage(key, app) else fetchCacheImage(key, app)

            if (bitmap != null)
                return bitmap

            val url = "https://image.tmdb.org/t/p/w780/$fileName"
            try {
                inputStream = URL(url).openStream()
                bitmap =
                    inputStream?.let { createScaledBitmapFromStream(inputStream, width, height) }

                if (bitmap != null) {
                    writeInMemory(key, bitmap, app, saveToCache)
                }
            } catch (e: Exception) {
                Log.d("Discover_LoadCardBg", "Error ${e.message}")
                this.cancel(true)
            }
        }
        return bitmap
    }

    override fun onPostExecute(result: Bitmap?) {
        result?.let {
            cardView.get()?.scaleType = ImageView.ScaleType.CENTER_CROP
            cardView.get()?.setImageBitmap(it)
        }
    }

    private fun createScaledBitmapFromStream(
        s: InputStream,
        minimumDesiredBitmapWidth: Int,
        minimumDesiredBitmapHeight: Int
    ): Bitmap? {
        val stream = BufferedInputStream(s, 8 * 1024)
        val decodeBitmapOptions = BitmapFactory.Options()
        if (minimumDesiredBitmapWidth > 0 && minimumDesiredBitmapHeight > 0) {
            val decodeBoundsOptions = BitmapFactory.Options()
            decodeBoundsOptions.inJustDecodeBounds = true
            stream.mark(8 * 1024)
            BitmapFactory.decodeStream(stream, null, decodeBoundsOptions)
            stream.reset()
            val originalWidth: Int = decodeBoundsOptions.outWidth
            val originalHeight: Int = decodeBoundsOptions.outHeight

            val scale =
                (originalWidth / minimumDesiredBitmapWidth).coerceAtMost(originalHeight / minimumDesiredBitmapHeight)

            val bitmap: Bitmap?
            if (scale > 1) {
                decodeBitmapOptions.inSampleSize = 1.coerceAtLeast(scale)
                bitmap = BitmapFactory.decodeStream(stream, null, decodeBitmapOptions)

            } else
                bitmap = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeStream(stream),
                    minimumDesiredBitmapWidth,
                    minimumDesiredBitmapHeight, false
                )
            stream.close()
            return bitmap
        }
        return null
    }

    private fun createKey(path: String): String {
        return path.substringAfter('/').substringBefore('.').toLowerCase(Locale.ENGLISH)
    }

    private fun fetchDiskImage(key: String, application: DiscoverApplication): Bitmap? {
        if (application.containsKey(key))
            return application.getBitmap(key)
        return null
    }

    private fun fetchCacheImage(key: String, application: DiscoverApplication): Bitmap? {
        return application.memoryCache[key]
    }

    private fun writeInMemory(
        key: String,
        bitmap: Bitmap,
        application: DiscoverApplication,
        saveToCache: Boolean?
    ) {
        application.apply {
            memoryCache.put(key, bitmap)
            if (saveToCache == null)
                diskCacheLock.withLock {
                    if (!containsKey(key)) {
                        put(key, bitmap)
                    }
                }
        }
    }
}