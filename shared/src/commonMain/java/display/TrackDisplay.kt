package com.nixinova.carstfm.shared.display

import com.nixinova.carstfm.shared.model.CurrentTrack
import com.nixinova.carstfm.shared.util.DateFormatting
import java.time.Duration
import java.time.Instant

class TrackDisplay(val carouselValue: Int) {
    fun trackSubtitle(currentTrack: CurrentTrack): String {
        return if (carouselValue % 2 == 0 || currentTrack.track.album == null)
            "by " + currentTrack.track.artist
        else
            "on " + currentTrack.track.album
    }

    fun trackTime(currentTrack: CurrentTrack, curTrackStartTime: Instant?): String {
        return if (currentTrack.time == null)
            if (curTrackStartTime == null)
                "Now playing"
            else
                "Now playing (${elapsedTime(curTrackStartTime)})"
        else
            "Played at " + DateFormatting.relDateTime(currentTrack.time)
    }

    private fun elapsedTime(startTime: Instant?): String {
        val elapsedSecs = Duration.between(startTime ?: Instant.now(), Instant.now()).abs().toSeconds()
        val elapsedFmtd = "%.1f".format(elapsedSecs / 60f)
        return elapsedFmtd + "min"
    }
}
