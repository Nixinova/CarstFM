package com.nixinova.carstfm.mobile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
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
fun UsernameForm(settings: Settings) {
    var inputUsername by rememberSaveable { mutableStateOf(settings.username ?: "") }
    var savedUsername by rememberSaveable { mutableStateOf(settings.username) }

    return Column {
        OutlinedTextField(
            label = {
                Text(
                    text = "Last.FM Username"
                )
            },
            value = inputUsername,
            onValueChange = { inputUsername = it },
        )

        Button(
            onClick = {
                settings.username = inputUsername
                savedUsername = inputUsername // update state to refresh UI
            },
            enabled = inputUsername != savedUsername,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (inputUsername == savedUsername) "Saved" else "Save"
            )
        }
    }
}
