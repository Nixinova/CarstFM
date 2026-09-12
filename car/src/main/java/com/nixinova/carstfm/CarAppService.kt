package com.nixinova.carstfm

import androidx.car.app.CarAppService as AppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.nixinova.carstfm.model.Settings

class CarAppService : AppService() {
    private lateinit var settings: Settings

    override fun onCreate() {
        super.onCreate()

        settings = Settings(this)
    }

    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return CarAppSession(settings)
    }
}