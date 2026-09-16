package com.zachvlat.havenwall

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zachvlat.havenwall.data.FavoritesManager
import com.zachvlat.havenwall.data.Wallpaper
import com.zachvlat.havenwall.data.WallpaperRepository
import kotlinx.coroutines.launch

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WallpaperRepository()
    private val favoritesManager = FavoritesManager(application)

    var wallpapers by mutableStateOf<List<Wallpaper>>(emptyList())
        private set

    var favorites by mutableStateOf<List<Wallpaper>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isSearching by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var wallpaperDetail by mutableStateOf<Wallpaper?>(null)
        private set

    private var currentPage = 1
    private var lastPage = Int.MAX_VALUE
    private var currentQuery = ""

    init {
        favorites = favoritesManager.getFavorites()
    }

    fun search(query: String) {
        currentQuery = query.trim()
        if (currentQuery.isEmpty()) return

        currentPage = 1
        lastPage = Int.MAX_VALUE
        wallpapers = emptyList()
        isSearching = true
        errorMessage = null
        loadPage()
    }

    fun loadNextPage() {
        if (isLoading || currentPage > lastPage) return
        loadPage()
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        favoritesManager.toggleFavorite(wallpaper)
        favorites = favoritesManager.getFavorites()
    }

    fun isFavorite(id: String): Boolean = favoritesManager.isFavorite(id)

    fun fetchWallpaperDetail(id: String) {
        viewModelScope.launch {
            try {
                wallpaperDetail = repository.getWallpaper(id)
            } catch (_: Exception) {
                wallpaperDetail = null
            }
        }
    }

    private fun loadPage() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.search(currentQuery, currentPage)
                wallpapers = if (currentPage == 1) {
                    response.data
                } else {
                    wallpapers + response.data
                }
                lastPage = response.meta.lastPage
                currentPage++
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error occurred"
            } finally {
                isLoading = false
            }
        }
    }
}