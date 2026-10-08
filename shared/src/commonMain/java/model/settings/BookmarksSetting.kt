package com.nixinova.carstfm.shared.model.settings

import com.nixinova.carstfm.shared.model.TrackDetails
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class BookmarkType(val storedValue: String) {
    TRACK_INFO("1"),
    PLAYS_OR_TAGS("2"),
}

data class BookmarksSetting(
    val track: TrackDetails,
    val type: BookmarkType,
) {
    companion object {
        private object Delimiter {
            const val BOOKMARK = ","
            const val BOOKMARK_VALUE = "|"
            const val STORED_TRACK = "/"
        }

        fun serialise(bookmarks: List<BookmarksSetting>): String {
            return bookmarks
                .joinToString(Delimiter.BOOKMARK) { serialiseData(it) }
        }

        fun deserialise(value: String): List<BookmarksSetting> {
            return value.split(Delimiter.BOOKMARK)
                .filter { it.isNotEmpty() }
                .map { deserialiseData(it) }
                .distinct()
        }

        private fun serialiseData(data: BookmarksSetting): String {
            return listOf(
                // track part:
                listOf(data.track.artist, data.track.album, data.track.song)
                    .joinToString(Delimiter.STORED_TRACK) { encode(it ?: "") },
                // type part:
                data.type.storedValue,
            ).joinToString(Delimiter.BOOKMARK_VALUE)
        }

        private fun deserialiseData(str: String): BookmarksSetting {
            val mainParts = str.split(Delimiter.BOOKMARK_VALUE)
            val trackPart = mainParts[0]
            val typePart = mainParts.getOrNull(1) ?: BookmarkType.TRACK_INFO.storedValue

            val trackParts = trackPart
                .split(Delimiter.STORED_TRACK)
                .map { decode(it) }
            val track = TrackDetails(
                artist = trackParts[0],
                album = trackParts[1].ifEmpty { null },
                song = trackParts[2],
            )
            val type = when (typePart) {
                BookmarkType.TRACK_INFO.storedValue -> BookmarkType.TRACK_INFO
                BookmarkType.PLAYS_OR_TAGS.storedValue -> BookmarkType.PLAYS_OR_TAGS
                else -> throw Error("Invalid bookmark type")
            }
            return BookmarksSetting(track, type)
        }

        private fun encode(string: String): String {
            return URLEncoder.encode(string, StandardCharsets.UTF_8)
        }

        private fun decode(string: String): String {
            return URLDecoder.decode(string, StandardCharsets.UTF_8)
        }
    }
}
