package com.nixinova.carstfm.shared.display

import com.nixinova.carstfm.shared.model.Result
import com.nixinova.carstfm.shared.repository.LastFmRepository
import com.nixinova.carstfm.shared.util.DateFormatting

object GlanceDisplay {
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
                val lastPlayed = DateFormatting.relDate(playTimes[nPlays - 1])
                val totalNPlays = (if (hasMore) ">" else "") + nPlays
                return "Plays: $totalNPlays ($firstPlayed - $lastPlayed)"
            }
        }
    }

    fun tags(tagsResult: Result<List<String>>?, carouselValue: Int): String {
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
        val subList = (sortedTags + sortedTags).slice(IntRange(startIndex, length * 2 - 1))
        return "Tags: " + subList.joinToString(", ")
    }
}
