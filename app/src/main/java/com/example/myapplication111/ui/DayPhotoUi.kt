package com.example.myapplication111.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.myapplication111.data.DayPhotoEntity
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayPhotoCard(
    photo: DayPhotoEntity,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }
    
    // 兼容逻辑：如果存的是绝对路径，直接使用；如果是相对路径，动态拼接当前目录
    val photoFile = remember(photo.path) {
        if (photo.path.startsWith("/")) {
            File(photo.path)
        } else {
            val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
            File(baseDir, photo.path)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { showFullImage = true },
                onLongClick = { menuExpanded = true },
            ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = photoFile,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text("照片", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(photoFile.name, style = MaterialTheme.typography.bodySmall)
                Text("点击查看大图，长按可删除", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
        DropdownMenuItem(
            text = { Text("删除照片") },
            onClick = {
                menuExpanded = false
                onDelete()
            },
        )
    }

    if (showFullImage) {
        Dialog(
            onDismissRequest = { showFullImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = photoFile,
                        contentDescription = "Full Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { showFullImage = false },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
