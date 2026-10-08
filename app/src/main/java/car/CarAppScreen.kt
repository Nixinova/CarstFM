package com.nixinova.carstfm.app.car

import android.os.Handler
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import com.nixinova.carstfm.shared.repository.LastFmRepository
import com.nixinova.carstfm.shared.model.CurrentTrack
import com.nixinova.carstfm.shared.model.Result
import com.nixinova.carstfm.shared.model.Settings
import com.nixinova.carstfm.shared.util.DateFormatting
import com.nixinova.carstfm.R
import com.nixinova.carstfm.shared.model.settings.BookmarkType
import com.nixinova.carstfm.shared.model.settings.BookmarksSetting
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
    private var playsResult: Result<LastFmRepository.TrackPlaysDto>? = null
    private var tagsResult: Result<List<String>>? = null

    override fun onGetTemplate(): Template {
        username = settings.username
        return createTemplate(currentTrackResult, playsResult, tagsResult)
    }

    private fun createTemplate(
        currentTrackResult: Result<CurrentTrack>?,
        playsResult: Result<LastFmRepository.TrackPlaysDto>?,
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
        playsResult: Result<LastFmRepository.TrackPlaysDto>?,
        tagsResult: Result<List<String>>?,
    ): Pane {
        // A dummy number used in modulo operations below to denote which page of some data to view
        val carouselValue = (Instant.now().epochSecond.toInt() % 60) / dwellTime.seconds.toInt()
        val albumArt = currentTrack.albumArt
        val time = currentTrack.time

        return Pane.Builder()
            .apply {
                if (albumArt != null)
                    setImage(
                        CarIcon.Builder(albumArt)
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
                        if (time == null)
                            "Now playing"
                        else
                            "Played at " + DateFormatting.relDateTime(time)
                    )
                    .addAction(
                        getBookmarkAction(
                            BookmarksSetting(currentTrack.track, BookmarkType.TRACK_INFO),
                            R.drawable.bookmark_icon to R.drawable.unbookmark_icon,
                        )
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
                        else {
                            val playTimes = playsResult.value.playTimes
                            val hasMore = playsResult.value.hasMore
                            when (val nPlays = playTimes.size) {
                                0 -> {
                                    "Never played"
                                }
                                1 -> {
                                    val played = DateFormatting.relDate(playTimes[0])
                                    if (hasMore) {
                                        "Plays: >$nPlays ($played+)"
                                    }
                                    else {
                                        "Plays: $nPlays ($played)"
                                    }
                                }
                                else -> {
                                    val firstPlayed = DateFormatting.relDate(playTimes[0])
                                    val lastPlayed = DateFormatting.relDate(playTimes[nPlays - 1])
                                    val totalNPlays = (if (hasMore) ">" else "") + nPlays
                                    "Plays: $totalNPlays ($firstPlayed - $lastPlayed)"
                                }
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
                    .addAction(
                        getBookmarkAction(
                            BookmarksSetting(currentTrack.track, BookmarkType.PLAYS_OR_TAGS),
                            R.drawable.tag_icon to R.drawable.untag_icon,
                        )
                    )
                    .build()
            )
            .build()
    }

    private fun getBookmarkAction(setting: BookmarksSetting, icons: Pair<Int, Int>): Action {
        return Action.Builder()
            .setTitle("Bookmark ${setting.type.name}")
            .setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(
                        carContext,
                        if (settings.bookmarks.contains(setting))
                            icons.second
                        else
                            icons.first
                    )
                ).build()
            )
            .setOnClickListener {
                if (settings.bookmarks.contains(setting))
                    settings.bookmarks -= setting
                else
                    settings.bookmarks += setting
                invalidate() // refresh UI
            }
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
