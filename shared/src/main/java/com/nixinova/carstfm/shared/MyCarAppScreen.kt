package com.nixinova.carstfm.shared

import android.os.Handler
import android.os.Looper
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import com.nixinova.carstfm.shared.model.CurrentTrack
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MyCarAppScreen(carContext: CarContext) : Screen(carContext) {
    // Time to dwell on each item when displaying a carousel of text data
    val dwellTime: Duration = Duration.ofSeconds(8)

    override fun onGetTemplate(): Template {
        val currentTrack = getCurrentTrack()

        return createTrackPane(currentTrack)
    }

    private fun getCurrentTrack(): CurrentTrack {
        val currentTrack = CurrentTrack(
            trackName = "Track Name",
            artistName = "Artist Name",
            albumName = "Album Name",
            albumArt = IconCompat.createWithResource(carContext, R.drawable.sample),
            firstPlayed = Instant.parse("2026-08-01T00:00:00Z"),
            lastPlayed = Instant.parse("2026-07-01T00:00:00Z"),
            tags = listOf("edm", "rock", "pop"),
        )

        return currentTrack
    }

    private fun createTrackPane(currentTrack: CurrentTrack): Template {
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
            .withZone(ZoneId.systemDefault())

        // A dummy number used in modulo operations below to denote which page of some data to view
        val carouselValue = (Instant.now().epochSecond.toInt() % 60) / dwellTime.seconds.toInt()

        val pane = Pane.Builder()
            .setImage(
                CarIcon.Builder(currentTrack.albumArt)
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle(currentTrack.trackName)
                    .addText(currentTrack.artistName)
                    .addText(currentTrack.albumName)
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("At a glance")
                    .addText(
                        if (carouselValue % 2 == 0)
                            "First played: " + formatter.format(currentTrack.firstPlayed)
                        else
                            "Last played: " + formatter.format(currentTrack.lastPlayed)
                    )
                    .addText("Tags: " + run {
                        val numTagsToShow = 3
                        val length = currentTrack.tags.size
                        val startIndex =  carouselValue % length
                        val endIndex = (startIndex + numTagsToShow - 1) % (length * 2)
                        val sortedTags = currentTrack.tags.sorted()
                        val subList = (sortedTags + sortedTags).slice(IntRange(startIndex, endIndex))
                        val totalOthers = length - subList.size
                        val tagsText = subList.joinToString(", ")
                        val othersText = if (totalOthers > 0) " ( and $totalOthers more)" else ""

                        tagsText + othersText
                    })
                    .build()
            )
            .build()

        return PaneTemplate.Builder(pane)
            .setHeaderAction(Action.APP_ICON)
            .setTitle("CarstFM")
            .build()
    }

    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        // Refresh UI every dwellTime seconds
        override fun run() {
            handler.postDelayed(this, dwellTime.toMillis())
            invalidate()
        }
    }

    init {
        handler.post(updateRunnable)
    }
}