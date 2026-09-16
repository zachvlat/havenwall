package com.zachvlat.havenwall.data

import com.zachvlat.havenwall.network.ApiClient

class WallpaperRepository {

    private val api = ApiClient.api

    suspend fun search(query: String, page: Int): WallpaperResponse {
        return api.searchWallpapers(query = query, page = page)
    }

    suspend fun getWallpaper(id: String): Wallpaper {
        return api.getWallpaper(id).data
    }
}
