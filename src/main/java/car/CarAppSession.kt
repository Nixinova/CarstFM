package com.nixinova.carstfm.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.nixinova.carstfm.repository.LastFmRepository
import com.nixinova.carstfm.model.Settings

class CarAppSession(private val settings: Settings) : Session() {
    private val repository = LastFmRepository(settings)

    override fun onCreateScreen(intent: Intent): Screen {
        return CarAppScreen(carContext, repository, settings)
    }
}
