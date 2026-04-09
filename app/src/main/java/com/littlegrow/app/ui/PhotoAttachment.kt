package com.littlegrow.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import android.widget.ImageView
import com.littlegrow.app.media.PhotoStore

@Composable
fun PhotoPreviewCard(
    filePath: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    this.contentDescription = contentDescription
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            update = { imageView ->
                imageView.contentDescription = contentDescription
                imageView.setImageURI(PhotoStore.toUri(filePath))
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoActionRow(
    hasPhoto: Boolean,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("照片", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onTakePhoto) {
                Text("拍照")
            }
            OutlinedButton(onClick = onPickPhoto) {
                Text("选图")
            }
            if (hasPhoto) {
                TextButton(onClick = onRemovePhoto) {
                    Text("移除照片")
                }
            }
        }
    }
}
