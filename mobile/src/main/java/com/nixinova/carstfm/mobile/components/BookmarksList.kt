package com.nixinova.carstfm.mobile.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.nixinova.carstfm.mobile.R
import com.nixinova.carstfm.model.Settings

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

        bookmarks.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "${it.song} by ${it.artist}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 16.dp)
                )

                Button(
                    onClick = {
                        settings.bookmarks -= it
                        refreshCounter++
                    },
                    contentPadding = PaddingValues.Zero,
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.unbookmark_icon),
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
