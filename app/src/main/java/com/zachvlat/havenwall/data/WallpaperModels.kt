package com.zachvlat.havenwall.data

import com.google.gson.annotations.SerializedName

data class WallpaperResponse(
    val data: List<Wallpaper>,
    val meta: Meta
)

data class WallpaperDetailResponse(
    val data: Wallpaper
)

data class Wallpaper(
    val id: String,
    val url: String,
    val views: Int,
    val favorites: Int,
    val source: String,
    val purity: String,
    val category: String,
    @SerializedName("dimension_x") val dimensionX: Int,
    @SerializedName("dimension_y") val dimensionY: Int,
    val resolution: String,
    val ratio: String,
    @SerializedName("file_size") val fileSize: Long,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("created_at") val createdAt: String,
    val colors: List<String>,
    val path: String,
    val thumbs: Thumbs,
    val tags: List<Tag>? = null
)

data class Tag(
    val id: Int,
    val name: String,
    val alias: String? = null,
    @SerializedName("category_id") val categoryId: Int? = null,
    val category: String? = null,
    val purity: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class Thumbs(
    val large: String,
    val original: String,
    val small: String
)

data class Meta(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    val total: Int,
    val query: String?
)
