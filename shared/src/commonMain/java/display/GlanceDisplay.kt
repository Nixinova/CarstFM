package com.nixinova.carstfm.shared.display

import com.nixinova.carstfm.shared.model.Result
import com.nixinova.carstfm.shared.repository.LastFmRepository
import com.nixinova.carstfm.shared.util.DateFormatting
import java.time.Duration
import java.time.Instant
import kotlin.collections.reduce

class GlanceDisplay(val carouselValue: Int, hasAlbumArt: Boolean) {
    val maxLineLen = if (hasAlbumArt) 32 else 55

    fun plays(playsResult: Result<LastFmRepository.TrackPlaysDto>?): String {
        if (playsResult == null) {
            return "Plays loading..."
        }

        if (playsResult.isError) {
            return "Error: " + playsResult.error
        }

        val playTimes = playsResult.value.playTimes
        val hasMore = playsResult.value.hasMore
        when (val nPlays = playTimes.size) {
            0 -> {
                return "Never played"
            }
            1 -> {
                val played = DateFormatting.relDate(playTimes[0])
                return if (hasMore) {
                    "Plays: >$nPlays ($played+)"
                }
                else {
                    "Plays: $nPlays ($played)"
                }
            }
            else -> {
                val firstPlayed = DateFormatting.relDate(playTimes[0])
                var lastPlayed = DateFormatting.relDate(playTimes[nPlays - 1])
                if (Duration.between(Instant.now(), playTimes[nPlays - 1]).abs() < Duration.ofMinutes(10)) {
                    lastPlayed = "just now"
                }
                val totalNPlays = (if (hasMore) ">" else "") + nPlays
                val text = "Plays: $totalNPlays ($firstPlayed - $lastPlayed)"
                if (text.length > maxLineLen && carouselValue % 5 > 2) {
                    return "…" + text.takeLast(maxLineLen)
                }
                return text
            }
        }
    }

    fun uniqueAlbums(playsResult: Result<LastFmRepository.TrackPlaysDto>?): String {
        if (playsResult == null) {
            return "Plays loading..."
        }

        if (playsResult.isError) {
            return "Error: " + playsResult.error
        }

        val albums = playsResult.value.albums

        val length = albums.size
        if (length == 0) return "From: (no albums)"

        val albumCounts = HashMap<String, Int>()
        for (album in albums) {
            albumCounts[album] = (albumCounts[album] ?: 0) + 1
        }

        val albumsTextList = albumCounts
            .toList()
            .sortedBy { it.first }
            .sortedByDescending { it.second }
            .map { "${it.first} (${it.second}x)" }

        val subList = cycleList(albumsTextList, carouselValue)
        return "From: " + subList.joinToString(", ")
    }

    fun tags(tagsResult: Result<List<String>>?): String {
        if (tagsResult == null) {
            return "Tags loading..."
        }

        if (tagsResult.isError) {
            return "Error: " + tagsResult.error
        }

        if (tagsResult.value.isEmpty()) {
            return "Untagged"
        }

        val sortedTags = tagsResult.value.sortedBy { it.lowercase() }
        val subList = cycleList(sortedTags, carouselValue)
        return "Tags: " + subList.joinToString(", ")
    }

    private fun cycleList(list: List<String>, carouselValue: Int): List<String> {
        val carouselIndex = carouselValue % list.size
        return list.slice(carouselIndex until list.size) +
            list.slice(0 until carouselIndex)
    }
}
