package com.nixinova.carstfm.shared.display

import com.nixinova.carstfm.shared.model.CurrentTrack
import com.nixinova.carstfm.shared.util.DateFormatting
import java.time.Duration
import java.time.Instant

class TrackDisplay() {
    fun trackSubtitle(currentTrack: CurrentTrack): String {
        val byArtist = "by " + currentTrack.track.artist
        val fromAlbum = "from " + currentTrack.track.album
        return if (currentTrack.track.album == null)
            byArtist
        else
            "$byArtist $fromAlbum"
    }

    fun trackTime(currentTrack: CurrentTrack, curTrackStartTime: Instant?): String {
        return if (currentTrack.time == null)
            if (curTrackStartTime == null)
                "Now playing"
            else
                "Now playing (${elapsedTime(curTrackStartTime)} ago)"
        else
            "Played at " + DateFormatting.relDateTime(currentTrack.time)
    }

    private fun elapsedTime(startTime: Instant?): String {
        val elapsedSecs = Duration.between(startTime ?: Instant.now(), Instant.now()).abs().toSeconds()
        val elapsedFmtd = "%.1f".format(elapsedSecs / 60f)
        return elapsedFmtd + "m"
    }
}
