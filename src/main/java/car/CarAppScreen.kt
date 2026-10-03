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
import com.nixinova.carstfm.model.Result
import com.nixinova.carstfm.model.Settings
import com.nixinova.carstfm.util.DateFormatting
import java.time.Duration
import java.time.Instant

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
    private var currentTrackResult: Result<CurrentTrack>? = null
    private var playsResult: Result<Array<Instant>>? = null
    private var tagsResult: Result<List<String>>? = null

    override fun onGetTemplate(): Template {
        username = settings.username
        return createTemplate(currentTrackResult, playsResult, tagsResult)
    }

    private fun createTemplate(
        currentTrackResult: Result<CurrentTrack>?,
        playsResult: Result<Array<Instant>>?,
        tagsResult: Result<List<String>>?,
    ): Template {
        val pane =
            if (username == null) {
                createNoUsernamePane()
            }
            else if (currentTrackResult == null) {
                Pane.Builder().setLoading(true).build()
            }
            else if (currentTrackResult.isError) {
                createErrorPane(currentTrackResult.error)
            }
            else {
                createTrackPane(currentTrackResult.value, playsResult, tagsResult)
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

    private fun createErrorPane(error: String): Pane {
        return Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle("An error has occurred")
                    .addText(error)
                    .build()
            )
            .build()
    }

    private fun createTrackPane(
        currentTrack: CurrentTrack,
        playsResult: Result<Array<Instant>>?,
        tagsResult: Result<List<String>>?,
    ): Pane {
        // A dummy number used in modulo operations below to denote which page of some data to view
        val carouselValue = (Instant.now().epochSecond.toInt() % 60) / dwellTime.seconds.toInt()

        return Pane.Builder()
            .apply {
                if (currentTrack.albumArt != null)
                    setImage(
                        CarIcon.Builder(currentTrack.albumArt)
                            .build()
                    )
            }
            .addRow(
                Row.Builder()
                    .setTitle(currentTrack.track.song)
                    .addText(
                        if (carouselValue % 2 == 0 || currentTrack.track.album == null)
                            "by " + currentTrack.track.artist
                        else
                            "on " + currentTrack.track.album
                    )
                    .addText(
                        if (currentTrack.time == null)
                            "Now playing"
                        else
                            "Played at " + DateFormatting.relDateTime(currentTrack.time)
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
                        if (playsResult == null) {
                            "Plays loading..."
                        }
                        else if (playsResult.isError) {
                            "Error: " + playsResult.error
                        }
                        else when (val nPlays = playsResult.value.size) {
                            0 -> {
                                "Never played"
                            }
                            1 -> {
                                val played = DateFormatting.relDate(playsResult.value[0])
                                "Plays: 1 ($played)"
                            }
                            else -> {
                                val firstPlayed = DateFormatting.relDate(playsResult.value[0])
                                val lastPlayed = DateFormatting.relDate(playsResult.value[nPlays - 1])
                                "Plays: $nPlays ($firstPlayed - $lastPlayed)"
                            }
                        }
                    })
                    .addText(
                        if (tagsResult == null) {
                            "Tags loading..."
                        }
                        else if (tagsResult.isError) {
                            "Error: " + tagsResult.error
                        }
                        else if (tagsResult.value.isEmpty()){
                            "Untagged"
                        }
                        else {
                            val length = tagsResult.value.size
                            val startIndex =  carouselValue % length
                            val sortedTags = tagsResult.value.sortedBy { it.lowercase() }
                            val subList = (sortedTags + sortedTags).slice(IntRange(startIndex, length * 2 - 1))
                            "Tags: " + subList.joinToString(", ")
                        }
                    )
                    .build()
            )
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

    private val fetchTrackRunnable = object : Runnable {
        // Fetch current track
        override fun run() {
            Thread {
                var lastCurrentTrack = currentTrackResult?.valueOrNull?.track
                var usernameVal = username
                if (usernameVal != null) {
                    currentTrackResult = repository.getCurrentTrack(usernameVal)
                    var newCurrentTrack = currentTrackResult?.valueOrNull?.track
                    if (newCurrentTrack != null && newCurrentTrack != lastCurrentTrack) {
                        // Refetch these
                        playsResult = null
                        Thread {
                            playsResult = repository.getTrackPlays(usernameVal, newCurrentTrack)
                        }.start()
                        tagsResult = null
                        Thread {
                            tagsResult = repository.getTrackTags(usernameVal, newCurrentTrack)
                        }.start()
                    }
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
