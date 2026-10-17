package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Wishlist Heart with subtle pulse and mint particle burst.
 */
@Composable
fun AnimatedHeartButton(
    isWishlisted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 24
) {
    var triggerBurst by remember { mutableStateOf(false) }

    val scale = remember { Animatable(1f) }
    val burstProgress = remember { Animatable(0f) }

    LaunchedEffect(isWishlisted) {
        if (isWishlisted) {
            triggerBurst = true
            scale.animateTo(
                targetValue = 1.30f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
            )
        } else {
            scale.snapTo(1f)
            triggerBurst = false
        }
    }

    LaunchedEffect(triggerBurst) {
        if (triggerBurst) {
            burstProgress.snapTo(0f)
            burstProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
            triggerBurst = false
        }
    }

    Box(
        modifier = modifier
            .size((size + 14).dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        // Particle burst on Canvas in brand mint & olive shades
        if (burstProgress.value in 0.01f..0.99f) {
            Canvas(modifier = Modifier.size(50.dp)) {
                val particleCount = 6
                val radius = burstProgress.value * 24.dp.toPx()
                val alpha = (1f - burstProgress.value).coerceIn(0f, 1f)
                val brandColors = listOf(Mint, MediumGreen, MintLight, DarkGreen, MintInstantDelivery)

                for (i in 0 until particleCount) {
                    val angle = (i * (360.0 / particleCount)) * (Math.PI / 180.0)
                    val cx = center.x + (radius * cos(angle)).toFloat()
                    val cy = center.y + (radius * sin(angle)).toFloat()
                    val dotColor = brandColors[i % brandColors.size].copy(alpha = alpha)
                    drawCircle(
                        color = dotColor,
                        radius = (3.dp.toPx() * (1f - burstProgress.value * 0.4f)),
                        center = Offset(cx, cy)
                    )
                }
            }
        }

        Icon(
            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "المفضلة",
            tint = if (isWishlisted) Mint else TextSecondaryDark,
            modifier = Modifier
                .size(size.dp)
                .scale(scale.value)
        )
    }
}

/**
 * Animated Cart Icon with jumping bounce on count changes.
 */
@Composable
fun AnimatedCartBadge(
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bounceAnim = remember { Animatable(0f) }

    LaunchedEffect(itemCount) {
        if (itemCount > 0) {
            bounceAnim.animateTo(
                targetValue = -7f,
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
            )
            bounceAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f)
            )
        }
    }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        BadgedBox(
            badge = {
                if (itemCount > 0) {
                    Badge(
                        containerColor = PrimaryOlive,
                        contentColor = Mint,
                        modifier = Modifier.offset { IntOffset(0, bounceAnim.value.dp.roundToPx()) }
                    ) {
                        Text(
                            text = itemCount.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            SimpleAppIcon(
                name = "shopping_cart",
                size = 24.dp,
                tint = Color.White
            )
        }
    }
}
