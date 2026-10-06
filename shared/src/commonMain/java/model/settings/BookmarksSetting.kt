package com.nixinova.carstfm.shared.model.settings

import com.nixinova.carstfm.shared.model.TrackDetails

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
        }

        private fun serialiseTrack(track: TrackDetails): String {
            val delim = Delimiter.STORED_TRACK
            return listOf(track.artist, track.album, track.song)
                .joinToString(delim)
        }

        private fun deserialiseTrack(str: String): TrackDetails {
            val parts = str.split(Delimiter.STORED_TRACK)
            return TrackDetails(parts[0], parts[1], parts[2])
        }
    }
}
