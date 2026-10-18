package com.nixinova.carstfm.shared.display

import com.nixinova.carstfm.shared.model.Result
import com.nixinova.carstfm.shared.repository.LastFmRepository
import com.nixinova.carstfm.shared.util.DateFormatting
import java.time.Duration
import java.time.Instant

class GlanceDisplay(val carouselValue: Int, hasAlbumArt: Boolean) {
    val maxLineLen = if (hasAlbumArt) 37 else 70

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

        val albumsSet = playsResult.value.albums.distinct()

        val length = albumsSet.size
        if (length == 0) return "On: (no albums)"

        val startIndex =  carouselValue % length
        val subList = if (length < 3)
            albumsSet
        else
            (albumsSet + albumsSet).slice(IntRange(startIndex, length * 2 - 1))
        return "On: " + subList.joinToString(", ")
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

        val length = tagsResult.value.size
        val startIndex =  carouselValue % length
        val sortedTags = tagsResult.value.sortedBy { it.lowercase() }
        val subList = if (length < 3)
            sortedTags
        else
            (sortedTags + sortedTags).slice(IntRange(startIndex, length * 2 - 1))
        return "Tags: " + subList.joinToString(", ")
    }
}
