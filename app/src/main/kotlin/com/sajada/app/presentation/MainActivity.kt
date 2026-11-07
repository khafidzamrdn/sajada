package com.sajada.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sajada.app.presentation.ui.screens.MainScreen
import com.sajada.app.presentation.ui.theme.SajadaTheme
import com.sajada.app.presentation.viewmodel.AttendanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SajadaTheme {
                val viewModel: AttendanceViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
