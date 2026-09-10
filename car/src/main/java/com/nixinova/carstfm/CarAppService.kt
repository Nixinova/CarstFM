package com.nixinova.carstfm

import androidx.car.app.CarAppService as AppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class CarAppService : AppService() {
    private val username = "Nixinova"

    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return CarAppSession(username)
    }
}