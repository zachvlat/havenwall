package com.zachvlat.havenwall

import com.google.gson.Gson
import com.zachvlat.havenwall.data.WallpaperResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class WallpaperModelsTest {
    @Test
    fun exactTagQueryObjectDoesNotBreakParsing() {
        val json = """{"data":[],"meta":{"current_page":1,"last_page":1,"per_page":24,"total":0,"query":{"id":1,"tag":"anime"}}}"""

        val response = Gson().fromJson(json, WallpaperResponse::class.java)

        assertEquals(1, response.meta.currentPage)
        assertEquals(0, response.data.size)
    }
}
