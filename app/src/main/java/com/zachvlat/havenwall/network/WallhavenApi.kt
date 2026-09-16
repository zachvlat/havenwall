package com.zachvlat.havenwall.network

import com.zachvlat.havenwall.data.WallpaperDetailResponse
import com.zachvlat.havenwall.data.WallpaperResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WallhavenApi {

    @GET("api/v1/search")
    suspend fun searchWallpapers(
        @Query("q") query: String,
        @Query("purity") purity: String = "100",
        @Query("ratios") ratios: String = "9x16",
        @Query("page") page: Int = 1
    ): WallpaperResponse

    @GET("api/v1/w/{id}")
    suspend fun getWallpaper(
        @Path("id") id: String
    ): WallpaperDetailResponse
}
