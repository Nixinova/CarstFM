package com.nixinova.carstfm

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
import com.nixinova.carstfm.model.CurrentTrack
import com.nixinova.carstfm.model.Settings
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CarAppScreen(
    carContext: CarContext,
    private val repository: LastFmRepository,
    private val settings: Settings
) : Screen(carContext) {
    // Time to dwell on each item when displaying a carousel of text data
    val dwellTime: Duration = Duration.ofSeconds(8)
    // Interval to refetch current track info
    val refetchTime: Duration = Duration.ofSeconds(10)

    private var username: String? = null
    private var currentTrack: CurrentTrack? = null

    override fun onGetTemplate(): Template {
        username = settings.username
        return createTemplate(currentTrack)
    }

    private fun createTemplate(currentTrack: CurrentTrack?): Template {
        val pane =
            if (username == null) {
                createNoUsernamePane()
            }
            else if (currentTrack == null) {
                Pane.Builder().setLoading(true).build()
            }
            else {
                createTrackPane(currentTrack)
            }

        return PaneTemplate.Builder(pane)
            .setHeaderAction(Action.APP_ICON)
            .setTitle("CarstFM")
            .build()
    }

    private fun createNoUsernamePane(): Pane {
        return Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle("Username not specified")
                    .addText("Please enter your Last.fm username in the mobile app")
                    .build()
            )
            .build()
    }

    private fun createTrackPane(currentTrack: CurrentTrack): Pane {
        // A dummy number used in modulo operations below to denote which page of some data to view
        val carouselValue = (Instant.now().epochSecond.toInt() % 60) / dwellTime.seconds.toInt()

        return Pane.Builder()
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
                    .addText(run {
                        val nPlays = currentTrack.plays.size
                        if (carouselValue % 3 == 0)
                            "First played: " + (
                                if (nPlays == 0) "N/A"
                                else formatDate(currentTrack.plays[0])
                            )
                        else if (carouselValue % 3 == 1)
                            "Last played: " + (
                                if (nPlays == 0) "N/A"
                                else formatDate(currentTrack.plays[nPlays - 1])
                            )
                        else
                            "Times played: " + currentTrack.plays.size
                    })
                    .addText("Tags: " + (if (currentTrack.tags.isEmpty()) "None" else run {
                        val length = currentTrack.tags.size
                        val startIndex =  carouselValue % length
                        val sortedTags = currentTrack.tags.sorted()
                        val subList = (sortedTags + sortedTags).slice(IntRange(startIndex, length * 2 - 1))
                        subList.joinToString(", ")
                    }))
                    .build()
            )
            .build()
    }

    private fun formatDate(date: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
            .withZone(ZoneId.systemDefault())
        return formatter.format(date)
    }

    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        // Refresh UI every dwellTime seconds
        override fun run() {
            handler.postDelayed(this, dwellTime.toMillis())
            invalidate()
        }
    }

    private val fetchTrackRunnable = object : Runnable {
        // Fetch current track
        override fun run() {
            Thread {
                if (username != null) {
                    currentTrack = repository.getCurrentTrack(username ?: "")
                }
            }.start()
            handler.postDelayed(this, refetchTime.toMillis())
        }
    }

    init {
        handler.post(updateRunnable)
        handler.post(fetchTrackRunnable)
    }
}
