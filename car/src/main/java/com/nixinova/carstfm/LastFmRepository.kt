package com.nixinova.carstfm

import androidx.car.app.CarContext
import androidx.core.graphics.drawable.IconCompat
import com.nixinova.carstfm.model.CurrentTrack
import org.json.JSONArray
import java.io.BufferedReader
import java.net.URL
import java.time.Instant

class LastFmRepository {
    private var apiUrl = "https://get.geojs.io/v1/ip/country.json?ip=8.8.8.8" // authless dummy endpoint for testing

    // call within a Thread
    fun getCurrentTrack(carContext: CarContext, username: String): CurrentTrack {
        val response = URL(apiUrl + "#" + username)
            .openStream()
            .bufferedReader()
            .use(BufferedReader::readText)

        val json = JSONArray(response).getJSONObject(0)

        val currentTrack = CurrentTrack(
            trackName = json.getString("country"),
            artistName = json.getString("country_3"),
            albumName = json.getString("name"),
            albumArt = IconCompat.createWithResource(carContext, R.drawable.sample),
            firstPlayed = Instant.parse("2026-08-01T00:00:00Z"),
            lastPlayed = Instant.parse("2026-07-01T00:00:00Z"),
            tags = listOf("edm", "rock", "pop"),
        )

        return currentTrack
    }
}
