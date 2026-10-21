package com.app.newlib

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.newlib.screens.EditCameraRecordings
import com.app.newlib.ui.theme.Android_alhawanajdTheme

class ComposeChangeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val screenType = intent.getStringExtra("screenType") ?: ""
        setContent {
            Android_alhawanajdTheme {
                MainScreen(
                    screenType = screenType,
                    onBack = { finish() }
                )
            }
        }
    }
}


@Composable
fun MainScreen(
    screenType: String,
    onBack: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (screenType) {
            "editor" -> {
                EditCameraRecordings(
                    onBack = onBack,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            "preview" -> {
                Text(
                    text = "Preview Screen",
                    modifier = Modifier.padding(innerPadding)
                )
            }

            "settings" -> {
                Text(
                    text = "Settings Screen",
                    modifier = Modifier.padding(innerPadding)
                )
            }

            else -> {
                Text(
                    text = "Unknown Screen: $screenType",
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}


