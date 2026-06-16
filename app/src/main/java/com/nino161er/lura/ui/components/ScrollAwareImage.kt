package com.nino161er.rssfeed.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.abs

@Composable
fun ParallaxImage(
    imageUrl: String?,
    contentDescription: String?,
    listState: LazyListState,
    itemIndex: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val scale by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == itemIndex }
            if (itemInfo != null) {
                val itemCenter = itemInfo.offset + itemInfo.size / 2
                val viewportCenter = layoutInfo.viewportEndOffset / 2
                val distance = abs(itemCenter - viewportCenter).toFloat() / viewportCenter.coerceAtLeast(1)
                1f - (distance * 0.06f).coerceIn(0f, 0.06f)
            } else 0.95f
        }
    }

    val animScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(stiffness = 200f),
        label = "parallax_scale"
    )

    val animAlpha by animateFloatAsState(
        targetValue = 0.7f + ((scale - 0.94f) / 0.06f).coerceIn(0f, 1f) * 0.3f,
        animationSpec = spring(stiffness = 200f),
        label = "parallax_alpha"
    )

    val animatedModifier = modifier
        .scale(animScale)
        .alpha(animAlpha)

    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = animatedModifier
        )
    } else {
        Box(
            modifier = animatedModifier,
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
