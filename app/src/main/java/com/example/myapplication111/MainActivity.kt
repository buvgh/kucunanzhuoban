package com.example.myapplication111

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.myapplication111.ui.theme.MyApplication111Theme
import com.example.myapplication111.ui.DockNoteApp
import com.example.myapplication111.ui.DockNoteViewModel
import com.example.myapplication111.util.BackupManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<DockNoteViewModel> { DockNoteViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming DB file (Restore from other phones)
        intent?.data?.let { uri ->
            lifecycleScope.launch {
                val success = BackupManager.restoreFromUri(this@MainActivity, uri)
                if (success) {
                    restartApp()
                } else {
                    Toast.makeText(this@MainActivity, "数据同步失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.restoreSuccessTrigger.collectLatest { success ->
                if (success) {
                    restartApp()
                }
            }
        }

        setContent {
            val darkMode by viewModel.isDarkMode.collectAsState()
            MyApplication111Theme(darkTheme = darkMode) {
                DockNoteApp(viewModel = viewModel)
            }
        }
    }

    private fun restartApp() {
        Toast.makeText(this, "还原成功，正在重启应用...", Toast.LENGTH_LONG).show()
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}
