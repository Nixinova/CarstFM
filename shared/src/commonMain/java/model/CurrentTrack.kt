package com.nixinova.carstfm.shared.model

import androidx.core.graphics.drawable.IconCompat
import java.time.Instant

data class CurrentTrack(
    val track: TrackDetails,
    // Null if currently playing
    val time: Instant?,
    // Null if no album present
    val albumArt: IconCompat?,
)
