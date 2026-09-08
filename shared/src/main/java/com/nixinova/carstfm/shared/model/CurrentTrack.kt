package com.nixinova.carstfm.shared.model

import java.time.Instant

data class CurrentTrack(
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val albumArtUrl: String,
    val time: Instant,
    val firstPlayed: Instant,
    val lastPlayed: Instant,
    val hasTags: Boolean
)
