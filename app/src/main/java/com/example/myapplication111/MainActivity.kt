package com.example.myapplication111

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication111.ui.theme.MyApplication111Theme
import com.example.myapplication111.ui.DockNoteApp
import com.example.myapplication111.ui.DockNoteViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<DockNoteViewModel> { DockNoteViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkMode by viewModel.isDarkMode.collectAsState()
            MyApplication111Theme(darkTheme = darkMode) {
                DockNoteApp(viewModel = viewModel)
            }
        }
    }
}
