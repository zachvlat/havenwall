package com.zachvlat.havenwall

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zachvlat.havenwall.R
import com.zachvlat.havenwall.data.FavoritesManager
import com.zachvlat.havenwall.data.Wallpaper
import com.zachvlat.havenwall.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val api = ApiClient.api
    private val favoritesManager = FavoritesManager(application)

    val wallpapers = mutableStateListOf<Wallpaper>()

    var favorites by mutableStateOf<List<Wallpaper>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isSearching by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var detailError by mutableStateOf<String?>(null)
        private set

    var wallpaperDetail by mutableStateOf<Wallpaper?>(null)
        private set

    private var currentPage = 1
    private var lastPage = Int.MAX_VALUE
    private var currentQuery = ""
    private var currentSorting = "date_added"
    private var currentTopRange: String? = null
    private var requestId = 0
    private var detailId: String? = null
    private var loadJob: Job? = null

    init {
        favorites = favoritesManager.getFavorites()
        loadHome("hot")
    }

    fun search(query: String) {
        currentQuery = query.trim()
        if (currentQuery.isEmpty()) {
            errorMessage = getApplication<Application>().getString(R.string.empty_search)
            return
        }
        currentSorting = "date_added"
        currentTopRange = null
        startLoad()
    }

    fun loadHome(sorting: String, topRange: String? = null) {
        currentQuery = ""
        currentSorting = sorting
        currentTopRange = topRange
        startLoad()
    }

    private fun startLoad() {
        val id = ++requestId
        loadJob?.cancel()
        currentPage = 1
        lastPage = Int.MAX_VALUE
        wallpapers.clear()
        isLoading = true
        isSearching = true
        errorMessage = null
        detailError = null
        loadPage(id)
    }

    fun loadNextPage() {
        if (isLoading || currentPage > lastPage) return
        loadPage(requestId)
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        if (!favoritesManager.toggleFavorite(wallpaper)) {
            errorMessage = getApplication<Application>().getString(R.string.favorites_error)
            return
        }
        favorites = favoritesManager.getFavorites()
    }

    fun isFavorite(id: String): Boolean = favorites.any { it.id == id }

    fun fetchWallpaperDetail(id: String) {
        detailId = id
        viewModelScope.launch {
            detailError = null
            wallpaperDetail = null
            try {
                val result = api.getWallpaper(id).data
                if (detailId == id) wallpaperDetail = result
            } catch (_: Exception) {
                if (detailId == id) {
                    detailError = getApplication<Application>().getString(R.string.tags_error)
                }
            }
        }
    }

    private fun loadPage(id: Int) {
        loadJob = viewModelScope.launch {
            if (id != requestId) return@launch
            isLoading = true
            try {
                val response = api.searchWallpapers(
                    query = currentQuery,
                    page = currentPage,
                    sorting = currentSorting,
                    topRange = currentTopRange
                )
                if (id != requestId) return@launch
                if (currentPage == 1) wallpapers.clear()
                wallpapers.addAll(response.data)
                lastPage = response.meta.lastPage
                currentPage++
            } catch (e: Exception) {
                if (id == requestId) {
                    errorMessage = e.message
                        ?: getApplication<Application>().getString(R.string.unknown_error)
                }
            } finally {
                if (id == requestId) isLoading = false
            }
        }
    }
}
