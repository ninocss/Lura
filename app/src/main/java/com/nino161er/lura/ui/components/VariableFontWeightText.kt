package com.nino161er.rssfeed.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ScrollingHeadline(
    text: String,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    var targetWeight by remember { mutableFloatStateOf(400f) }
    var currentWeight by remember { mutableFloatStateOf(400f) }

    val firstVisibleItem = listState.firstVisibleItemIndex
    val scrollOffset = listState.firstVisibleItemScrollOffset

    val scrollProgress = if (firstVisibleItem > 0) {
        1f
    } else {
        (scrollOffset.coerceIn(0, 200) / 200f).coerceIn(0f, 1f)
    }

    targetWeight = 900f - (scrollProgress * 300f)

    LaunchedEffect(targetWeight) {
        animate(
            initialValue = currentWeight,
            targetValue = targetWeight,
            animationSpec = tween(durationMillis = 150)
        ) { value, _ ->
            currentWeight = value
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight(currentWeight.toInt())
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
