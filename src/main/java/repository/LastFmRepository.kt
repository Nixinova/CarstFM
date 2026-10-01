package com.nixinova.carstfm.repository

import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.IconCompat
import com.nixinova.carstfm.model.CurrentTrack
import com.nixinova.carstfm.model.TrackDetails
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant

class LastFmRepository {
    private val apiKey = "0ff9dba2068dd35752b321c6412f86c5"
    private val apiUrlBase = "https://ws.audioscrobbler.com/2.0/"

    private var cachedCurrentTrack: CurrentTrack? = null

    // call within a Thread
    fun getCurrentTrack(username: String): CurrentTrack? {
        val (track, time, image) = getLatestTrackDetails(username)
            ?: return null

        val cached = cachedCurrentTrack
        if (cached != null && cached.track == track) {
            return cachedCurrentTrack
        }

        val trackPlays = getTrackPlays(username, track)
        val userTags = getTrackTags(username, track)

        val currentTrack = CurrentTrack(
            track = track,
            time = time,
            albumArt = image,
            plays = trackPlays?.sorted(),
            tags = userTags,
        )
        cachedCurrentTrack = currentTrack

        return currentTrack
    }

    private data class LatestTrackDetails(
        val track: TrackDetails,
        val time: Instant?,
        val albumImage: IconCompat?,
    )
    private fun getLatestTrackDetails(username: String): LatestTrackDetails? {
        val params = toParams(
            "username" to username,
            "method" to "user.getRecentTracks",
            "limit" to "1",
        )
        val latestTrackResponse = callLastFmApi(params)
            ?: return null

        val latestTrackData = JSONObject(latestTrackResponse)
            .getJSONObject("recenttracks")
            .getJSONArray("track")
            .getJSONObject(0)

        val artist = latestTrackData.getJSONObject("artist").getString("#text")
        val album = latestTrackData.getJSONObject("album").getString("#text").ifEmpty { null }
        val song = latestTrackData.getString("name")
        val timeUtcSecs = latestTrackData.optJSONObject("date")?.getInt("uts")?.toLong()
        val time = if (timeUtcSecs == null) null else Instant.ofEpochSecond(timeUtcSecs)
        val albumArtUrl = latestTrackData
            .getJSONArray("image")
            .getJSONObject(2)
            .getString("#text")
            .ifEmpty { null }
        val image = if (albumArtUrl == null) null else downloadImage(albumArtUrl)

        return LatestTrackDetails(
            TrackDetails(artist, album, song),
            time,
            image,
        )
    }

    private fun getTrackTags(username: String, track: TrackDetails): List<String>? {
        val params = toParams(
            "username" to username,
            "method" to "track.getTags",
            "artist" to encode(track.artist),
            "track" to encode(track.song),
        )
        val userTagsResponse = callLastFmApi(params)
            ?: return null

        val userTagsObjects = JSONObject(userTagsResponse)
            .getJSONObject("tags")
            .optJSONArray("tag")
            ?: JSONArray()
        val userTags = List(userTagsObjects.length()) { i ->
            userTagsObjects.getJSONObject(i).getString("name")
        }
        return userTags
    }

    private fun getTrackPlays(username: String, track: TrackDetails, page: Int = 1): Array<Instant>? {
        val maxLimit = 200;
        val params = toParams(
            "username" to username,
            "method" to "user.getTrackScrobbles",
            "artist" to encode(track.artist),
            "track" to encode(track.song),
            "limit" to maxLimit.toString(),
            "page" to page.toString(),
        )
        val scrobblesResponse = callLastFmApi(params)
            ?: return null

        val trackScrobbles = JSONObject(scrobblesResponse)
            .getJSONObject("trackscrobbles")
            .getJSONArray("track")

        val dates = mutableListOf<Instant>()
        for (i in 0..<trackScrobbles.length()) {
            val uts = trackScrobbles
                .getJSONObject(i)
                .getJSONObject("date")
                .getInt("uts")
                .toLong()
            dates += Instant.ofEpochSecond(uts)
        }
        if (trackScrobbles.length() == maxLimit) {
            val recursedList = getTrackPlays(username, track, page + 1)?.toList()
            if (recursedList != null) {
                dates.addAll(recursedList)
            }
        }

        return dates
            .sorted()
            .toTypedArray()
    }

    private fun callLastFmApi(params: Map<String, String>): String? {
        val url = apiUrlBase + "?" + params.map { "${it.key}=${it.value}" }.joinToString("&")
        val response = fetch(url)
        try {
            val hasError = JSONObject(response).has("error")
            if (hasError) {
                return null
            }
            return response
        } catch (_: Exception) {
            return null
        }
    }

    private fun toParams(vararg args: Pair<String, String>): Map<String, String> {
        val params = mutableMapOf<String, String>()
        for (arg in args) {
            params[arg.first] = arg.second
        }
        params["api_key"] = apiKey
        params["format"] = "json"
        return params
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

    private fun downloadImage(url: String): IconCompat {
        val bitmap = URL(url)
            .openStream()
            .use { BitmapFactory.decodeStream(it) }
        return IconCompat.createWithBitmap(bitmap)
    }
}
