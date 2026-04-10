package com.littlegrow.app.ui

import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.littlegrow.app.R
import com.littlegrow.app.media.PhotoStore

@Composable
fun BabyAvatar(
    avatarPath: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (avatarPath.isNullOrBlank()) {
            Image(
                painter = painterResource(id = R.drawable.default_baby_avatar),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        adjustViewBounds = false
                        this.contentDescription = contentDescription
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { imageView ->
                    imageView.contentDescription = contentDescription
                    imageView.setImageURI(PhotoStore.toUri(avatarPath))
                },
            )
        }
    }
}
