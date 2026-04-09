package com.littlegrow.app.ui

import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import com.littlegrow.app.media.PendingPhotoCapture
import com.littlegrow.app.media.PhotoStore

data class ManagedPhotoAttachmentState(
    val photoPath: String?,
    val onTakePhoto: () -> Unit,
    val onPickPhoto: () -> Unit,
    val onRemovePhoto: () -> Unit,
    val commitChanges: () -> Unit,
    val discardChanges: () -> Unit,
)

@Composable
fun rememberManagedPhotoAttachment(
    initialPhotoPath: String?,
    photoTag: String,
    onError: (String?) -> Unit,
): ManagedPhotoAttachmentState {
    val context = LocalContext.current
    val currentOnError by rememberUpdatedState(onError)
    var photoPath by rememberSaveable(initialPhotoPath) { mutableStateOf(initialPhotoPath) }
    var pendingCapture by remember(initialPhotoPath) { mutableStateOf<PendingPhotoCapture?>(null) }

    fun replacePhoto(newPath: String?) {
        if (photoPath != initialPhotoPath) {
            PhotoStore.deletePhoto(photoPath)
        }
        photoPath = newPath
        currentOnError(null)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            runCatching { PhotoStore.importPhoto(context, it, photoTag) }
                .onSuccess(::replacePhoto)
                .onFailure { currentOnError(it.message ?: "照片导入失败。") }
        }
    }
    val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val capture = pendingCapture
        pendingCapture = null
        if (success && capture != null) {
            replacePhoto(capture.path)
        } else {
            PhotoStore.deletePhoto(capture?.path)
        }
    }

    return ManagedPhotoAttachmentState(
        photoPath = photoPath,
        onTakePhoto = {
            val capture = PhotoStore.createPendingCapture(context, photoTag)
            pendingCapture = capture
            takePhotoLauncher.launch(capture.uri)
        },
        onPickPhoto = {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onRemovePhoto = { replacePhoto(null) },
        commitChanges = {
            pendingCapture = null
            currentOnError(null)
        },
        discardChanges = {
            buildSet {
                pendingCapture?.path?.let(::add)
                photoPath?.takeIf { it != initialPhotoPath }?.let(::add)
            }.forEach(PhotoStore::deletePhoto)
            pendingCapture = null
            photoPath = initialPhotoPath
            currentOnError(null)
        },
    )
}

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
