package com.nixinova.carstfm

import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.IconCompat
import com.nixinova.carstfm.model.CurrentTrack
import com.nixinova.carstfm.model.TrackDetails
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

class LastFmRepository {
    private val apiKeyUrl = "https://www.nixinova.com/.netlify/functions/lastfm-token"
    private val apiUrlBase = "https://ws.audioscrobbler.com/2.0/"

    private var apiKey: String? = null
    private var cachedCurrentTrack: CurrentTrack? = null

    init {
        Thread {
            getApiKey()
        }.start()
    }

    // call within a Thread
    fun getCurrentTrack(username: String): CurrentTrack? {
        if (apiKey == null) {
            return null
        }

        val (track, image) = getLatestTrackDetails(username)

        val cached = cachedCurrentTrack
        if (cached != null && cached.track == track) {
            return cachedCurrentTrack
        }

        val userTags = getTrackTags(username, track)
        val trackPlays = scrapePageForPlayTimes(username, track)

        val currentTrack = CurrentTrack(
            track = track,
            albumArt = image,
            plays = trackPlays?.sorted(),
            tags = userTags,
        )
        cachedCurrentTrack = currentTrack

        return currentTrack
    }

    private data class LatestTrackDetails(val track: TrackDetails, val albumImage: IconCompat)
    private fun getLatestTrackDetails(username: String): LatestTrackDetails {
        val latestTrackResponse = callLastFmApi("method=user.getRecentTracks&limit=1", username)
        val latestTrackData = JSONObject(latestTrackResponse)
            .getJSONObject("recenttracks")
            .getJSONArray("track")
            .getJSONObject(0)

        val artist = latestTrackData.getJSONObject("artist").getString("#text")
        val album = latestTrackData.getJSONObject("album").getString("#text")
        val song = latestTrackData.getString("name")
        val albumArtUrl = latestTrackData
            .getJSONArray("image")
            .getJSONObject(2)
            .getString("#text")
        val image = downloadImage(albumArtUrl)

        return LatestTrackDetails(TrackDetails(artist, album, song), image)
    }

    private fun getTrackTags(username: String, track: TrackDetails): List<String> {
        val userTagsResponse = callLastFmApi(
            "method=track.getTags&artist=${encode(track.artist)}&track=${encode(track.song)}",
            username
        )
        val userTagsObjects = JSONObject(userTagsResponse)
            .getJSONObject("tags")
            .optJSONArray("tag")
            ?: JSONArray()
        val userTags = List(userTagsObjects.length()) { i ->
            userTagsObjects.getJSONObject(i).getString("name")
        }
        return userTags
    }

    private fun scrapePageForPlayTimes(username: String, track: TrackDetails): Array<Instant>? {
        val url = "https://www.last.fm/user/${username}/library/music/" +
                "${encode(track.artist)}/_/${encode(track.song)}?_pjax=%23content"
        val response = fetchHtml(url)
            ?: return null

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
            .use { it.readText() }
    }

    private fun fetchHtml(url: String): String? {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "CarstFM/1.0 (https://github.com/Nixinova/CarstFM)")
        return try {
            connection.inputStream
                .bufferedReader()
                .use { it.readText() }
        } catch(_: IOException) {
            null
        }
    }

    private fun downloadImage(url: String): IconCompat {
        val bitmap = URL(url)
            .openStream()
            .use { BitmapFactory.decodeStream(it) }
        return IconCompat.createWithBitmap(bitmap)
    }
}
