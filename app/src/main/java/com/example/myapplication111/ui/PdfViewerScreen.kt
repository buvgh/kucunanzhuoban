package com.example.myapplication111.ui

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun PdfViewerScreen(
    pdfUri: Uri,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var rendererHolder by remember(pdfUri) { mutableStateOf<PdfRendererHolder?>(null) }
    var openError by remember(pdfUri) { mutableStateOf<String?>(null) }

    DisposableEffect(pdfUri) {
        runCatching {
            val descriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: error("无法打开 PDF 文件")
            PdfRendererHolder(descriptor, PdfRenderer(descriptor))
        }.onSuccess {
            rendererHolder = it
            openError = null
        }.onFailure {
            rendererHolder = null
            openError = it.message ?: "PDF 打开失败"
        }

        onDispose {
            rendererHolder?.close()
            rendererHolder = null
        }
    }

    when {
        openError != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = openError ?: "PDF 打开失败",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        rendererHolder == null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        else -> {
            val pageCount = rendererHolder!!.renderer.pageCount
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items((0 until pageCount).toList()) { pageIndex ->
                    PdfPageCard(
                        holder = rendererHolder!!,
                        pageIndex = pageIndex,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfPageCard(
    holder: PdfRendererHolder,
    pageIndex: Int,
    modifier: Modifier = Modifier,
) {
    val pageBitmap by produceState<Bitmap?>(initialValue = null, holder, pageIndex) {
        value = runCatching {
            holder.renderer.openPage(pageIndex).use { page ->
                val targetWidth = 1440
                val scale = targetWidth / page.width.toFloat()
                val bitmap = Bitmap.createBitmap(
                    targetWidth,
                    (page.height * scale).toInt().coerceAtLeast(1),
                    Bitmap.Config.ARGB_8888,
                )
                bitmap.eraseColor(AndroidColor.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
        }.getOrNull()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
        ) {
            Text(
                text = "第 ${pageIndex + 1} 页",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            )
            if (pageBitmap == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Image(
                    bitmap = pageBitmap!!.asImageBitmap(),
                    contentDescription = "PDF 第 ${pageIndex + 1} 页",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth,
                )
            }
        }
    }
}

private class PdfRendererHolder(
    val fileDescriptor: ParcelFileDescriptor,
    val renderer: PdfRenderer,
) {
    fun close() {
        renderer.close()
        fileDescriptor.close()
    }
}
