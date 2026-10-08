package com.nixinova.carstfm.shared.model

import android.content.Context
import androidx.core.content.edit
import com.nixinova.carstfm.shared.model.settings.BookmarksSetting

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
        val customPlaysEndpoint = "customEndpoint"
    }

    var username: String?
        get() = getPref(keys.username)
        set(value) = setPref(keys.username, value)

    var bookmarks: List<BookmarksSetting>
        get() = BookmarksSetting.deserialise(getPref(keys.bookmarks) ?: "")
        set(value) = setPref(keys.bookmarks, BookmarksSetting.serialise(value))

    var customPlaysEndpoint: String?
        get() = getPref(keys.customPlaysEndpoint)
        set(value) = setPref(keys.customPlaysEndpoint, value)
}
