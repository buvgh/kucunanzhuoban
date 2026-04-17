package com.example.myapplication111.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication111.data.DayPhotoEntity
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayPhotoCard(
    photo: DayPhotoEntity,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val fileName = remember(photo.path) { File(photo.path).name }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { menuExpanded = true },
            ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = photo.path,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text("照片", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(fileName, style = MaterialTheme.typography.bodySmall)
                Text("长按可删除", style = MaterialTheme.typography.bodySmall)
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
}
