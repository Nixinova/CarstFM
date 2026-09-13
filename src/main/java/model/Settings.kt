package com.nixinova.carstfm.model

import android.content.Context

class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("carstfm", Context.MODE_PRIVATE)

    var username: String?
        get() = prefs.getString("username", null)
        set(value) {
            prefs.edit().putString("username", value).apply()
        }

    private val bookmarksDelimiter = ","
    private val storedTrackDelimiter = "/"
    var bookmarks: List<TrackDetails>
        get() = (prefs.getString("bookmarks", "") ?: "")
            .split(bookmarksDelimiter)
            .filter { it.isNotEmpty() }
            .map { deserialiseTrack(it) }
        set(value) = prefs.edit().putString(
            "bookmarks",
            value.joinToString(bookmarksDelimiter) { serialiseTrack((it)) }
        ).apply()
    private fun deserialiseTrack(str: String): TrackDetails {
        val parts = str.split(storedTrackDelimiter)
        return TrackDetails(parts[0], parts[1], parts[2])
    }
    private fun serialiseTrack(track: TrackDetails): String {
        return "${track.artist}$storedTrackDelimiter${track.album}$storedTrackDelimiter${track.song}"
    }
}
