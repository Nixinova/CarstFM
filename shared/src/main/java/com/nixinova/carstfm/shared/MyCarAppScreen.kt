package com.nixinova.carstfm.shared

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.nixinova.carstfm.shared.model.CurrentTrack
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter



class MyCarAppScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val currentTrack = CurrentTrack(
            trackName = "Track Name",
            artistName = "Artist Name",
            albumName = "Album Name",
            albumArtUrl = "https://example.com",
            time = Instant.parse("2026-09-01T00:00:00Z"),
            firstPlayed = Instant.parse("2026-08-01T00:00:00Z"),
            lastPlayed = Instant.parse("2026-07-01T00:00:00Z"),
            hasTags = true,
        )
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())

        val pane = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle(currentTrack.trackName)
                    .addText(currentTrack.artistName)
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle(currentTrack.albumName)
                    .addText(currentTrack.albumArtUrl)
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle(formatter.format(currentTrack.time))
                    .addText("(now)")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Info")
                    .addText("First played: " + formatter.format(currentTrack.firstPlayed))
                    .addText("Last played: " + formatter.format(currentTrack.lastPlayed))
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Tags")
                    .addText(if (currentTrack.hasTags) "Yes" else "No")
                    .build()
            )
            .build()

        return PaneTemplate.Builder(pane)
            .setHeaderAction(Action.APP_ICON)
            .setTitle("CarstFM")
            .build()
    }
}