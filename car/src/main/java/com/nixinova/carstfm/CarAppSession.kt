package com.nixinova.carstfm

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.nixinova.carstfm.model.CurrentTrack
import com.nixinova.carstfm.model.Settings

class CarAppSession(private val settings: Settings) : Session() {
    private val repository = LastFmRepository()

    override fun onCreateScreen(intent: Intent): Screen {
        val username = settings.username
        return CarAppScreen(carContext, repository, username)
    }
}