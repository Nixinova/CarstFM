package com.nixinova.carstfm.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DateFormatting {
    companion object {
        fun date(instant: Instant): String {
            val date = instant.toZoned()
            val now = Instant.now().toZoned()
            val pattern = if (now.year == date.year) {
                "d MMM"
            } else {
                "d MMM yyyy"
            }
            return date.format(DateTimeFormatter.ofPattern(pattern))
        }

        fun time(instant: Instant): String {
            val date = instant.toZoned()
            val pattern = "h:mma"
            return date.format(DateTimeFormatter.ofPattern(pattern))
        }

        fun relDate(instant: Instant): String {
            val date = instant.toZoned()
            val now = Instant.now().toZoned()
            return when (val daysAgo = now.dayOfYear - date.dayOfYear) {
                0 -> "Today"
                1 -> "Yesterday"
                2, 3, 4 -> "$daysAgo days ago"
                else -> date(instant)
            }
        }

        fun relDateTime(instant: Instant): String {
            val date = instant.toZoned()
            val now = Instant.now().toZoned()
            if (date.dayOfYear == now.dayOfYear) {
                return time(instant)
            }
            return time(instant) + ", " + date(instant)
        }

        private fun Instant.toZoned(): ZonedDateTime {
            return this.atZone(ZoneId.systemDefault())
        }
    }
}
