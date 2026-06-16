package com.nino161er.rssfeed.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.nino161er.rssfeed.data.util.ColorExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun DominantColorAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onColorExtracted: (Color) -> Unit = {}
) {
    var state by remember { mutableStateOf<AsyncImagePainter.State?>(null) }
    val imageUrl = model?.toString()

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        onState = { state = it }
    )

    LaunchedEffect(imageUrl) {
        if (imageUrl != null && state is AsyncImagePainter.State.Success) {
            val drawable = (state as AsyncImagePainter.State.Success).result.drawable
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    if (drawable is android.graphics.drawable.BitmapDrawable) {
                        drawable.bitmap
                    } else {
                        drawable.toBitmap()
                    }
                } catch (e: Exception) {
                    try {
                        URL(imageUrl).openStream().use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    } catch (e2: Exception) {
                        null
                    }
                }
            }
            if (bitmap != null) {
                val colorInt = ColorExtractor.extractDominantColor(bitmap)
                if (colorInt != null) {
                    onColorExtracted(Color(colorInt))
                }
            }
        }
    }
}
