package com.nixinova.carstfm.model

import android.content.Context
import androidx.core.content.edit

class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("carstfm", Context.MODE_PRIVATE)

    private fun getPref(pref: String) = prefs.getString(pref, null)
    private fun setPref(pref: String, value: String?) {
        val newValue = if (value.isNullOrEmpty()) null else value.trim()
        prefs.edit { putString(pref, newValue) }
    }

    private val keys = object {
        val username = "username"
        val bookmarks = "bookmarks"
    }

    var username: String?
        get() = getPref(keys.username)
        set(value) = setPref(keys.username, value)

    var bookmarks: List<TrackDetails>
        get() = (getPref(keys.bookmarks) ?: "")
            .split(bookmarksDelimiter)
            .filter { it.isNotEmpty() }
            .map { deserialiseTrack(it) }
        set(value) {
            val newValue = value.joinToString(bookmarksDelimiter) { serialiseTrack((it)) }
            setPref(keys.bookmarks, newValue)
        }
    private val bookmarksDelimiter = ","
    private val storedTrackDelimiter = "/"
    private fun deserialiseTrack(str: String): TrackDetails {
        val parts = str.split(storedTrackDelimiter)
        return TrackDetails(parts[0], parts[1], parts[2])
    }
    private fun serialiseTrack(track: TrackDetails): String {
        return "${track.artist}$storedTrackDelimiter${track.album}$storedTrackDelimiter${track.song}"
    }
}
