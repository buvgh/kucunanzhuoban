package com.example.myapplication111

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.IntentCompat
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
        handleIncomingIntent(intent)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val incomingUri = intent.extractIncomingUri() ?: return
        when {
            isPdfIntent(intent, incomingUri) -> {
                viewModel.openPdf(incomingUri, resolveDisplayName(incomingUri) ?: "PDF查看")
            }

            isDatabaseIntent(intent, incomingUri) -> {
                lifecycleScope.launch {
                    val success = BackupManager.restoreFromUri(this@MainActivity, incomingUri)
                    if (success) {
                        restartApp()
                    } else {
                        Toast.makeText(this@MainActivity, "数据同步失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun Intent?.extractIncomingUri(): Uri? {
        if (this == null) return null
        return when (action) {
            Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(this, Intent.EXTRA_STREAM, Uri::class.java)
            else -> data
        }
    }

    private fun isPdfIntent(intent: Intent?, uri: Uri): Boolean {
        val mimeType = intent?.type ?: contentResolver.getType(uri)
        val path = uri.toString().lowercase()
        return mimeType == "application/pdf" || path.endsWith(".pdf")
    }

    private fun isDatabaseIntent(intent: Intent?, uri: Uri): Boolean {
        val mimeType = intent?.type ?: contentResolver.getType(uri)
        val path = uri.toString().lowercase()
        return mimeType == "application/octet-stream" || path.endsWith(".db")
    }

    private fun resolveDisplayName(uri: Uri): String? {
        return contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    null
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
