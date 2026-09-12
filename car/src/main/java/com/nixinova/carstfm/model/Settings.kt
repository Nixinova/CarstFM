package com.nixinova.carstfm.model

import android.content.Context

class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("carstfm", Context.MODE_PRIVATE)

    var username: String?
        get() = prefs.getString("username", null)
        set(value) {
            prefs.edit().putString("username", value).apply()
        }
}
