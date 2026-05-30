package com.example.myapplication111.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {
    private const val TAG = "BackupManager"
    private const val DB_NAME = "dock_note.db"
    private const val BACKUP_DIR_NAME = "database_backups"
    private const val MAX_BACKUPS = 5

    suspend fun backupDatabase(context: Context, isExport: Boolean = false): File? = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                Log.w(TAG, "Database file does not exist, skipping backup")
                return@withContext null
            }

            val backupDir = if (isExport) context.cacheDir else File(context.getExternalFilesDir(null), BACKUP_DIR_NAME)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val prefix = if (isExport) "Export_Data_" else "backup_"
            val backupFile = File(backupDir, "${prefix}${timestamp}.db")

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "Backup created: ${backupFile.absolutePath}")
            if (!isExport) rotateBackups(backupDir)
            backupFile
        } catch (e: Exception) {
            Log.e(TAG, "Error during backup", e)
            null
        }
    }

    fun getBackups(context: Context): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), BACKUP_DIR_NAME)
        return backupDir.listFiles { file -> file.name.startsWith("backup_") && file.name.endsWith(".db") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    suspend fun restoreBackup(context: Context, backupFile: File) = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            
            // Safe restore: Close DB and clear WAL/SHM
            com.example.myapplication111.data.DockNoteDatabase.getInstance(context).close()
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()
            
            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d(TAG, "Database restored from: ${backupFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error during restore", e)
            false
        }
    }

    suspend fun restoreFromUri(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            
            // Safe restore: Close DB and clear WAL/SHM
            com.example.myapplication111.data.DockNoteDatabase.getInstance(context).close()
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error during URI restore", e)
            false
        }
    }

    private fun shareBackupFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享数据文件"))
    }

    suspend fun exportAndShareDatabase(context: Context) {
        val file = backupDatabase(context, isExport = true)
        if (file != null) {
            shareBackupFile(context, file)
        }
    }

    private fun rotateBackups(backupDir: File) {
        val files = backupDir.listFiles { file -> file.name.startsWith("backup_") && file.name.endsWith(".db") }
            ?: return
        
        if (files.size > MAX_BACKUPS) {
            files.sortBy { it.lastModified() }
            val toDelete = files.size - MAX_BACKUPS
            for (i in 0 until toDelete) {
                if (files[i].delete()) {
                    Log.d(TAG, "Deleted old backup: ${files[i].name}")
                }
            }
        }
    }
}
