package com.nino161er.rssfeed.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun FeaturedCard(
    title: String,
    description: String,
    imageUrl: String?,
    pubDate: String?,
    listState: LazyListState,
    itemIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(28.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Box {
                ParallaxImage(
                    imageUrl = imageUrl,
                    contentDescription = title,
                    listState = listState,
                    itemIndex = itemIndex,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                )
                
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "FEATURED",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (pubDate != null) {
                    Text(
                        text = pubDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardRow(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    pubDate: String? = null,
    sourceName: String? = null,
    sourceIconUrl: String? = null,
    isRead: Boolean = false,
    onSwipeMarkRead: () -> Unit = {}
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onSwipeMarkRead()
            }
            false
        }
    )

    val contentAlpha by animateColorAsState(
        targetValue = if (isRead) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(500),
        label = "content_alpha"
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 0.8f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "swipe_scale"
            )
            val bgColor by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(250),
                label = "swipe_color"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(bgColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp * scale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp * scale)
                            )
                        }
                        Text(
                            text = "Read",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    ) {
        Surface(
            onClick = onClick,
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Source Icon & Unread Indicator
                Box(modifier = Modifier.size(24.dp).padding(top = 2.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        if (sourceIconUrl != null) {
                            AsyncImage(
                                model = sourceIconUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                alpha = if (isRead) 0.6f else 1f
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Circle, 
                                contentDescription = null, 
                                modifier = Modifier.size(16.dp), 
                                tint = contentAlpha.copy(alpha = contentAlpha.alpha * 0.6f)
                            )
                        }
                    }
                    
                    // Small unread dot
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !isRead,
                        enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
                        exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center),
                        modifier = Modifier.align(Alignment.TopEnd).padding(1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sourceName ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isRead) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = pubDate ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentAlpha.copy(alpha = contentAlpha.alpha * 0.6f)
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isRead) FontWeight.Medium else FontWeight.Bold,
                        color = contentAlpha,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 4.dp),
                        lineHeight = 20.sp
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentAlpha.copy(alpha = contentAlpha.alpha * 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
