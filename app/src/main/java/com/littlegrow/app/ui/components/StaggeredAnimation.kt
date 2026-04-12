package com.littlegrow.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.staggeredFadeSlideIn(index: Int): Modifier {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(30f) }
    
    val effectSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    
    LaunchedEffect(Unit) {
        delay(index * 50L)
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = effectSpec
            )
        }
        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = spatialSpec
            )
        }
    }
    
    return this
        .alpha(alpha.value)
        .graphicsLayer { translationY = offsetY.value }
}
