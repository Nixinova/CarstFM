package com.nixinova.carstfm

import androidx.car.app.CarContext
import androidx.core.graphics.drawable.IconCompat
import com.nixinova.carstfm.model.CurrentTrack
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
    fun getCurrentTrack(carContext: CarContext, username: String): CurrentTrack? {
        if (apiKey == null) {
            return null
        }

        val latestTrackDetails = getLatestTrackDetails(username)
        val track = latestTrackDetails.track
        val artist = latestTrackDetails.artist
        val album = latestTrackDetails.album

        val cached = cachedCurrentTrack
        if (
            cached != null
            && cached.trackName == track
            && cached.artistName == artist
            && cached.albumName == album
        ) {
            return cachedCurrentTrack
        }

        val userTags = getTrackTags(username, artist, track)
        val trackPlays = getTrackPlays(username, artist, track)

        val currentTrack = CurrentTrack(
            trackName = track,
            artistName = artist,
            albumName = album,
            albumArt = IconCompat.createWithResource(carContext, R.drawable.sample), // todo
            firstPlayed = trackPlays.earliest,
            lastPlayed = trackPlays.latest,
            tags = userTags,
        )
        cachedCurrentTrack = currentTrack

        return currentTrack
    }

    private data class LatestTrackDetails(val artist: String, val album: String, val track: String)
    private fun getLatestTrackDetails(username: String): LatestTrackDetails {
        val latestTrackResponse = callLastFmApi("method=user.getRecentTracks&limit=1", username)
        val latestTrackData = JSONObject(latestTrackResponse)
            .getJSONObject("recenttracks")
            .getJSONArray("track")
            .getJSONObject(0)

        val artist = latestTrackData.getJSONObject("artist").getString("#text")
        val album = latestTrackData.getJSONObject("album").getString("#text")
        val track = latestTrackData.getString("name")

        return LatestTrackDetails(artist, album, track)
    }

    private fun getTrackTags(username: String, artist: String, track: String): List<String> {
        val userTagsResponse = callLastFmApi(
            "method=track.getTags&artist=${encode(artist)}&track=${encode(track)}",
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

    private data class TrackPlays(val earliest: Instant?, val latest: Instant?)
    private fun getTrackPlays(username: String, artist: String, track: String): TrackPlays {
        val trackPlays = scrapePageForPlayTimes(username, artist, track)
        if (trackPlays.isEmpty()) {
            return TrackPlays(null, null)
        }
        return TrackPlays(trackPlays[0], trackPlays[trackPlays.size - 1])
    }
    private fun scrapePageForPlayTimes(username: String, artist: String, track: String): Array<Instant> {
        val url = "https://www.last.fm/user/${username}/library/music/" +
                "${encode(artist)}/_/${encode(track)}?_pjax=%23content"
        val response = fetchHtml(url)
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

    private fun fetchHtml(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "CarstFM/1.0 (https://github.com/Nixinova/CarstFM)")
        return try {
            connection.inputStream
                .bufferedReader()
                .use { it.readText() }
        } catch(_: IOException) {
            ""
        }
    }
}
