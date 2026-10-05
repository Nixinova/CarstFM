package com.nixinova.carstfm.mobile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nixinova.carstfm.model.Settings

@Composable
fun CustomEndpointForm(settings: Settings) {
    var inputEndpoint by rememberSaveable { mutableStateOf(settings.customPlaysEndpoint ?: "") }
    var savedEndpoint by rememberSaveable { mutableStateOf(settings.customPlaysEndpoint) }

    return Column {
        Text(
            text = "Custom endpoint",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Specify an optional additional custom track history URL"
        )
        Text(
            text = "(Must return data of the type {date, artist, track}[])",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            label = {
                Text("URL")
            },
            value = inputEndpoint,
            onValueChange = { inputEndpoint = it },
            modifier = Modifier
                .fillMaxWidth()
        )

        Button(
            onClick = {
                settings.customPlaysEndpoint = inputEndpoint
                savedEndpoint = inputEndpoint // update state to refresh UI
            },
            enabled = inputEndpoint != savedEndpoint,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = if (inputEndpoint == savedEndpoint) "Saved" else "Save"
            )
        }
    }
}
