package com.nixinova.carstfm

import androidx.car.app.CarContext
import androidx.core.graphics.drawable.IconCompat
import com.nixinova.carstfm.model.CurrentTrack
import org.json.JSONObject
import org.json.JSONStringer
import org.json.JSONTokener
import java.io.BufferedReader
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.util.Date
import java.util.Locale

class LastFmRepository {
    private val apiKeyUrl = "https://www.nixinova.com/.netlify/functions/lastfm-token"
    private val apiUrlBase = "https://ws.audioscrobbler.com/2.0/"

    private var apiKey: String? = null

    init {
        Thread {
            getApiKey()
        }.start()
    }

    // call within a Thread
    fun getCurrentTrack(carContext: CarContext, username: String): CurrentTrack? {
        if (apiKey == null) {
            return null
        }

        val latestTrackResponse = callLastFmApi("method=user.getRecentTracks&limit=1", username)
        val latestTrackData = JSONObject(latestTrackResponse)
            .getJSONObject("recenttracks")
            .getJSONArray("track")
            .getJSONObject(0)

        val track = latestTrackData.getString("name")
        val artist = latestTrackData.getJSONObject("artist").getString("#text")
        val album = latestTrackData.getJSONObject("album").getString("#text")

        val trackPlays = scrapePageForPlayTimes(username, artist, track)

        val userTagsResponse = callLastFmApi(
            "method=track.getTags&artist=${encode(artist)}&track=${encode(track)}",
            username
        )
        val userTagsObjects = JSONObject(userTagsResponse)
            .getJSONObject("tags")
            .getJSONArray("tag")
        val userTags = List(userTagsObjects.length()) { i ->
            userTagsObjects.getJSONObject(i).getString("name")
        }

        val currentTrack = CurrentTrack(
            trackName = track,
            artistName = artist,
            albumName = album,
            albumArt = IconCompat.createWithResource(carContext, R.drawable.sample), // todo
            firstPlayed = trackPlays[0],
            lastPlayed = trackPlays[trackPlays.size - 1],
            tags = userTags,
        )

        return currentTrack
    }

    private fun scrapePageForPlayTimes(username: String, artist: String, track: String): Array<Instant> {
        return arrayOf(
            Instant.parse("2000-01-01T00:00:00Z"),
            Instant.parse("2000-12-31T00:00:00Z"),
        )
        // TODO: Fetch fragment correctly
        val url = "https://www.last.fm/user/${username}/library/music/" +
                "${encode(artist)}/_/${encode(track)}?_pjax=%23content"
        val response = fetch(url)
        val dateStrs = Regex("""<span title="([^">]+?)">""")
            .findAll(response)
            .map { it.groupValues[1] }
            .toList()
        val timeFormat = "EEEE d MMM yyyy, h:mma"
        val formatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern(timeFormat)
            .toFormatter(Locale.ENGLISH)
        val dates = dateStrs
            .map { LocalDateTime.parse(it, formatter).atZone(ZoneId.systemDefault()).toInstant() }
            .sorted()
            .toTypedArray()
        return dates
    }

    private fun callLastFmApi(params: String, username: String): String {
        return fetch("$apiUrlBase?$params&api_key=$apiKey&user=$username&format=json")
    }

    private fun getApiKey() {
        val response = fetch(apiKeyUrl)
        apiKey = JSONTokener(response).nextValue() as String
    }

    private fun encode(string: String): String {
        return URLEncoder.encode(string, StandardCharsets.UTF_8)
    }

    private fun fetch(url: String): String {
        return URL(url)
            .openStream()
            .bufferedReader()
            .use(BufferedReader::readText)
    }
}
