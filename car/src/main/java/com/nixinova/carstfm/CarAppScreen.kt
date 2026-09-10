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
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CarAppScreen(
    carContext: CarContext,
    private val repository: LastFmRepository,
    private val username: String
) : Screen(carContext) {
    // Time to dwell on each item when displaying a carousel of text data
    val dwellTime: Duration = Duration.ofSeconds(8)

    private var currentTrack: CurrentTrack? = null

    override fun onGetTemplate(): Template {
        return createTemplate(currentTrack)
    }

    private fun createTemplate(currentTrack: CurrentTrack?): Template {
        val pane = if (currentTrack == null) {
            Pane.Builder().setLoading(true).build()
        } else {
            createPane(currentTrack)
        }

        return PaneTemplate.Builder(pane)
            .setHeaderAction(Action.APP_ICON)
            .setTitle("CarstFM")
            .build()
    }

    private fun createPane(currentTrack: CurrentTrack): Pane {
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
            .withZone(ZoneId.systemDefault())

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
    }

    private fun loadCurrentTrack() {
        Thread {
            currentTrack = repository.getCurrentTrack(carContext, username)
        }.start()
    }

    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        // Refresh UI every dwellTime seconds
        override fun run() {
            handler.postDelayed(this, dwellTime.toMillis())
            loadCurrentTrack()
            invalidate()
        }
    }

    init {
        handler.post(updateRunnable)
    }
}