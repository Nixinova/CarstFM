package com.nixinova.carstfm.mobile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nixinova.carstfm.model.Settings

@Composable
fun HomeScreen(settings: Settings, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "CarstFM",
            style = MaterialTheme.typography.headlineLarge,
        )

        Text(
            text = "See information about your currently playing track from Last.fm."
        )

        UsernameForm(settings)
        BookmarksList(settings)
    }
}
