package com.nixinova.carstfm.shared.display

import com.nixinova.carstfm.shared.model.CurrentTrack
import com.nixinova.carstfm.shared.util.DateFormatting

class TrackDisplay(val carouselValue: Int) {
    fun trackSubtitle(currentTrack: CurrentTrack): String {
        return if (carouselValue % 2 == 0 || currentTrack.track.album == null)
            "by " + currentTrack.track.artist
        else
            "on " + currentTrack.track.album
    }

    fun trackTime(currentTrack: CurrentTrack): String {
        return if (currentTrack.time == null)
            "Now playing"
        else
            "Played at " + DateFormatting.relDateTime(currentTrack.time)
    }
}
