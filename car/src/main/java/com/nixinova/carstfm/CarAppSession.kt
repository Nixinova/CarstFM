package com.nixinova.carstfm

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.nixinova.carstfm.model.CurrentTrack

class CarAppSession(private val username: String) : Session() {
    private val repository = LastFmRepository()

    override fun onCreateScreen(intent: Intent): Screen {
        return CarAppScreen(carContext, repository, username)
    }
}