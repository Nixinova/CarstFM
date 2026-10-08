package com.nixinova.carstfm.app.mobile.components

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nixinova.carstfm.R
import com.nixinova.carstfm.shared.model.Settings
import com.nixinova.carstfm.shared.model.settings.BookmarkType

@Composable
fun BookmarksList(settings: Settings) {
    var bookmarks by remember { mutableStateOf(settings.bookmarks) }
    var refreshCounter by rememberSaveable { mutableIntStateOf(0) } // modify to refresh UI

    Column {
        Text(
            text = "Bookmarks",
            style = MaterialTheme.typography.headlineMedium
        )

        if (bookmarks.isEmpty()) {
            Text(
                text = "No bookmarks"
            )
        }

        bookmarks.forEach { bookmark ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = listOf(
                        "${bookmark.track.song} by ${bookmark.track.artist}",
                        when(bookmark.type) {
                            BookmarkType.TRACK_INFO -> "track info"
                            BookmarkType.PLAYS_OR_TAGS -> "plays/tags"
                        }
                    ).joinToString(" - "),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 16.dp)
                )

                Button(
                    onClick = {
                        settings.bookmarks -= bookmark
                        refreshCounter++
                    },
                    contentPadding = PaddingValues.Zero,
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Image(
                        painter = painterResource(
                            when (bookmark.type) {
                                BookmarkType.TRACK_INFO -> R.drawable.unbookmark_icon
                                BookmarkType.PLAYS_OR_TAGS -> R.drawable.untag_icon
                            }
                        ),
                        contentDescription = "Bookmarked",
                        modifier = Modifier
                            .size(28.dp)
                    )
                }
            }
        }
    }

    val handler = Handler(Looper.getMainLooper())

    val settingsRefreshRunnable = object : Runnable {
        override fun run() {
            bookmarks = settings.bookmarks
            handler.postDelayed(this, 1_000)
        }
    }

    LaunchedEffect(Unit) {
        handler.post(settingsRefreshRunnable)
    }
}
