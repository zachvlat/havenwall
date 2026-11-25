package com.zachvlat.havenwall

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zachvlat.havenwall.data.FavoritesManager
import com.zachvlat.havenwall.data.Thumbs
import com.zachvlat.havenwall.data.Wallpaper
import com.zachvlat.havenwall.ui.screens.centerCrop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesManagerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val prefs = context.getSharedPreferences("havenwall_favorites", Context.MODE_PRIVATE)

    @Before
    fun setUp() {
        prefs.edit().clear().commit()
    }

    @Test
    fun toggleAddsRemovesAndKeepsOtherFavorites() {
        val manager = FavoritesManager(context)
        val first = wallpaper("a")
        val second = wallpaper("b")

        assertTrue(manager.toggleFavorite(first))
        assertTrue(manager.toggleFavorite(second))
        assertEquals(listOf("a", "b"), manager.getFavorites().map { it.id })

        manager.toggleFavorite(first)
        assertEquals(listOf("b"), manager.getFavorites().map { it.id })
        assertFalse(manager.isFavorite("a"))
        assertTrue(manager.isFavorite("b"))
    }

    @Test
    fun brokenSavedJsonIsNotOverwritten() {
        prefs.edit().putString("favorites", "not json").commit()
        val manager = FavoritesManager(context)

        assertTrue(manager.getFavorites().isEmpty())
        assertFalse(manager.toggleFavorite(wallpaper("a")))
        assertEquals("not json", prefs.getString("favorites", null))
    }

    @Test
    fun centerCropFillsTheTarget() {
        val cropped = centerCrop(android.graphics.Bitmap.createBitmap(200, 100, android.graphics.Bitmap.Config.ARGB_8888), 100, 100)

        assertEquals(100, cropped.width)
        assertEquals(100, cropped.height)
    }

    private fun wallpaper(id: String) = Wallpaper(
        id = id,
        url = "https://wallhaven.cc/w/$id",
        views = 0,
        favorites = 0,
        source = "",
        purity = "sfw",
        category = "general",
        dimensionX = 1080,
        dimensionY = 1920,
        resolution = "1080x1920",
        ratio = "9x16",
        fileSize = 1000L,
        fileType = "image/jpeg",
        createdAt = "2026-01-01 00:00:00",
        colors = emptyList(),
        path = "https://w.wallhaven.cc/full/x/wallhaven-$id.jpg",
        thumbs = Thumbs("large", "original", "small")
    )
}
