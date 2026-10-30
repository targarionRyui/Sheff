package com.example.sheff

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient


class SheffApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header("User-Agent", "SheffStudyApp/1.0 (educational project)")
                    .build()
                chain.proceed(request)
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(client)
            .build()
    }
}
