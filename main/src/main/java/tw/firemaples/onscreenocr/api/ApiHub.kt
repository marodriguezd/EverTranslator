package tw.firemaples.onscreenocr.api

import retrofit2.Retrofit

object ApiHub {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl("http://localhost/").build()
    }

    val tessDataDownloader: TessDataDownloader by lazy {
        retrofit.create(TessDataDownloader::class.java)
    }
}
