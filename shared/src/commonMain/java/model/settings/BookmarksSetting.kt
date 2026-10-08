package com.nixinova.carstfm.shared.model.settings

import com.nixinova.carstfm.shared.model.TrackDetails
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class BookmarksSetting {
    companion object {
        private object Delimiter {
            const val BOOKMARK = ","
            const val STORED_TRACK = "/"
        }

        fun serialise(bookmarks: List<TrackDetails>): String {
            return bookmarks.joinToString(Delimiter.BOOKMARK) { serialiseTrack((it)) }
        }

        fun deserialise(value: String): List<TrackDetails> {
            return value.split(Delimiter.BOOKMARK)
                .filter { it.isNotEmpty() }
                .map { deserialiseTrack(it) }
                .distinct()
        }

        private fun serialiseTrack(track: TrackDetails): String {
            val delim = Delimiter.STORED_TRACK
            return listOf(track.artist, track.album, track.song)
                .joinToString(delim) { encode(it ?: "") }
        }

        private fun deserialiseTrack(str: String): TrackDetails {
            val parts = str.split(Delimiter.STORED_TRACK).map { decode(it) }
            return TrackDetails(
                artist = parts[0],
                album = parts[1].ifEmpty { null },
                song = parts[2],
            )
        }

        private fun encode(string: String): String {
            return URLEncoder.encode(string, StandardCharsets.UTF_8)
        }

        private fun decode(string: String): String {
            return URLDecoder.decode(string, StandardCharsets.UTF_8)
        }
    }
}
