package com.nixinova.carstfm.model

import androidx.core.graphics.drawable.IconCompat
import java.time.Instant

data class CurrentTrack(
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val albumArt: IconCompat,
    val firstPlayed: Instant?,
    val lastPlayed: Instant?,
    val tags: List<String>,
)
