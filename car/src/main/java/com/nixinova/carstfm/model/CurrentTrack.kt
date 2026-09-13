package com.nixinova.carstfm.model

import androidx.core.graphics.drawable.IconCompat
import java.time.Instant

data class CurrentTrack(
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val albumArt: IconCompat,
    val plays: List<Instant>,
    val tags: List<String>,
)
