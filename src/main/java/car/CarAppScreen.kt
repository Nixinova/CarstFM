package com.nixinova.carstfm.car

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
import com.nixinova.carstfm.repository.LastFmRepository
import com.nixinova.carstfm.R
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
                    .setTitle(currentTrack.track.song)
                    .addText(
                        if (carouselValue % 2 == 0)
                            "by " + currentTrack.track.artist
                        else
                            "on " + currentTrack.track.album
                    )
                    .addText(
                        if (currentTrack.time == null)
                            "Now playing"
                        else
                            "Played at " + formatDateTime(currentTrack.time, "h:mma, d MMM")
                    )
                    .addAction(
                        Action.Builder()
                            .setTitle("Bookmark")
                            .setIcon(
                                CarIcon.Builder(
                                    IconCompat.createWithResource(
                                        carContext,
                                        if (settings.bookmarks.contains(currentTrack.track))
                                            R.drawable.unbookmark_icon
                                        else
                                            R.drawable.bookmark_icon
                                    )
                                ).build()
                            )
                            .setOnClickListener {
                                if (settings.bookmarks.contains(currentTrack.track))
                                    settings.bookmarks -= currentTrack.track
                                else
                                    settings.bookmarks += currentTrack.track
                                invalidate() // refresh UI
                            }
                            .build()
                    )
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("At a glance")
                    .addText(run {
                        when (val nPlays = currentTrack.plays?.size) {
                            null -> {
                                "Plays failed to load"
                            }
                            0 -> {
                                "Never played"
                            }
                            else -> {
                                val format = "d MMM yyyy"
                                val firstPlayed = formatDateTime(currentTrack.plays[0], format)
                                val lastPlayed = formatDateTime(currentTrack.plays[nPlays - 1], format)
                                "Plays: $nPlays ($firstPlayed - $lastPlayed)"
                            }
                        }
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

    private fun formatDateTime(date: Instant, pattern: String): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
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
