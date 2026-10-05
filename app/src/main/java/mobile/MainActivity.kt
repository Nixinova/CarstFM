package com.nixinova.carstfm.app.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.nixinova.carstfm.app.mobile.theme.CarstFMTheme
import com.nixinova.carstfm.app.mobile.components.HomeScreen
import com.nixinova.carstfm.shared.model.Settings

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
                        packageInfo = packageManager.getPackageInfo(packageName, 0),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
