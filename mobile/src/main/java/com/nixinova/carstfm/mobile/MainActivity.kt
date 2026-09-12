package com.nixinova.carstfm.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nixinova.carstfm.model.Settings
import com.nixinova.carstfm.mobile.theme.CarstFMTheme

class MainActivity : ComponentActivity() {
    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings = Settings(this)

        enableEdgeToEdge()
        setContent {
            CarstFMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        settings = settings,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(settings: Settings, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "CarstFM",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "See information about your currently playing track from Last.fm."
        )

        UsernameForm(settings)
    }
}

@Composable
private fun UsernameForm(settings: Settings) {
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