package com.nixinova.carstfm.shared.model

import java.time.Instant

data class CurrentTrack(
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val albumArtUrl: String,
    val firstPlayed: Instant,
    val lastPlayed: Instant,
    val tags: List<String>,
)
