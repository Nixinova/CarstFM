package com.nixinova.carstfm.repository

import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.IconCompat
import com.nixinova.carstfm.model.CurrentTrack
import com.nixinova.carstfm.model.TrackDetails
import com.nixinova.carstfm.model.Result
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant

class LastFmRepository {
    private val apiKey = "0ff9dba2068dd35752b321c6412f86c5"
    private val apiUrlBase = "https://ws.audioscrobbler.com/2.0/"

    fun getCurrentTrack(username: String): Result<CurrentTrack> {
        val params = toParams(
            "username" to username,
            "method" to "user.getRecentTracks",
            "limit" to "1",
        )
        val latestTrackResponse = callLastFmApi(params).valueOrReturn {
            return Result.Fail<CurrentTrack>(it)
        }

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

        return Result.Ok(CurrentTrack(
            TrackDetails(artist, album, song),
            time,
            image,
        ))
    }

    fun getTrackTags(username: String, track: TrackDetails): Result<List<String>> {
        val params = toParams(
            "username" to username,
            "method" to "track.getTags",
            "artist" to encode(track.artist),
            "track" to encode(track.song),
        )
        val userTagsResponse = callLastFmApi(params).valueOrReturn {
            return Result.Fail<List<String>>("Failed to fetch user tags")
        }

        val userTagsObjects = JSONObject(userTagsResponse)
            .getJSONObject("tags")
            .optJSONArray("tag")
            ?: JSONArray()
        val userTags = List(userTagsObjects.length()) { i ->
            userTagsObjects.getJSONObject(i).getString("name")
        }
        return Result.Ok(userTags)
    }

    fun getTrackPlays(username: String, track: TrackDetails, page: Int = 1): Result<Array<Instant>> {
        val maxLimit = 200
        val params = toParams(
            "username" to username,
            "method" to "user.getTrackScrobbles",
            "artist" to encode(track.artist),
            "track" to encode(track.song),
            "limit" to maxLimit.toString(),
            "page" to page.toString(),
        )
        val scrobblesResponse = callLastFmApi(params).valueOrReturn {
            return Result.Fail<Array<Instant>>("Failed to fetch track plays")
        }

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
            val recursedList = getTrackPlays(username, track, page + 1)
            if (recursedList.isOk) {
                dates.addAll(recursedList.value.toList())
            }
        }

        return Result.Ok(
            dates.sorted().toTypedArray()
        )
    }

    private fun callLastFmApi(params: Map<String, String>): Result<String> {
        val url = apiUrlBase + "?" + params.map { "${it.key}=${it.value}" }.joinToString("&")
        val response = fetch(url)
        try {
            val error = JSONObject(response).optString("error")
            if (error.isNotEmpty()) {
                return Result.Fail<String>("Last.fm fetch error: $error")
            }
            return Result.Ok(response)
        } catch (ex: Exception) {
            return Result.Fail<String>("Unknown error: ${ex.message}")
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
