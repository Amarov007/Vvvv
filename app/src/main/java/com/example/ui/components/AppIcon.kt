package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Mint
import com.example.ui.theme.PrimaryOlive
import com.example.ui.theme.PrimaryOliveHover

enum class AppIconSize(val boxSize: Dp, val iconSize: Dp, val cornerRadius: Dp) {
    SMALL(32.dp, 18.dp, 8.dp),
    MEDIUM(42.dp, 24.dp, 12.dp),
    LARGE(54.dp, 30.dp, 16.dp),
    HERO(72.dp, 38.dp, 20.dp)
}

/**
 * Unified professional AppIcon component:
 * Tile background: #2F442C (PrimaryOlive) with subtle border
 * Icon tint: #A7E8BD (Mint)
 * Animation strictly applied to the icon tile alone. Zero emoji.
 */
@Composable
fun AppIcon(
    name: String,
    modifier: Modifier = Modifier,
    size: AppIconSize = AppIconSize.MEDIUM,
    tileColor: Color = PrimaryOlive,
    iconTint: Color = Mint,
    contentDescription: String? = null,
    clickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && clickable) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "iconScale"
    )

    val iconVector = resolveIcon(name)

    val boxModifier = modifier
        .size(size.boxSize)
        .scale(scale)
        .then(
            if (clickable && onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )

    Box(
        modifier = boxModifier
            .clip(RoundedCornerShape(size.cornerRadius))
            .background(tileColor)
            .border(1.dp, PrimaryOliveHover.copy(alpha = 0.6f), RoundedCornerShape(size.cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = contentDescription ?: name,
            tint = iconTint,
            modifier = Modifier.size(size.iconSize)
        )
    }
}

/**
 * Standalone Vector Icon without background tile.
 */
@Composable
fun SimpleAppIcon(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null
) {
    Icon(
        imageVector = resolveIcon(name),
        contentDescription = contentDescription ?: name,
        tint = tint,
        modifier = modifier.size(size)
    )
}

fun resolveIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "account_balance", "accounts", "bank" -> Icons.Default.AccountBalance
        "language", "website", "web", "globe" -> Icons.Default.Language
        "smartphone", "mobile", "app", "phone" -> Icons.Default.Smartphone
        "smart_toy", "bot", "algo", "robot" -> Icons.Default.SmartToy
        "bolt", "flash", "lightning" -> Icons.Default.Bolt
        "shopping_cart", "cart" -> Icons.Default.ShoppingCart
        "shopping_cart_outlined" -> Icons.Outlined.ShoppingCart
        "favorite", "heart" -> Icons.Default.Favorite
        "favorite_border", "heart_outline" -> Icons.Outlined.FavoriteBorder
        "search" -> Icons.Default.Search
        "tune", "filter", "filters" -> Icons.Default.Tune
        "content_copy", "copy" -> Icons.Default.ContentCopy
        "check_circle", "check", "success" -> Icons.Default.CheckCircle
        "star" -> Icons.Default.Star
        "verified", "verified_user", "badge" -> Icons.Default.Verified
        "shield", "security", "safe" -> Icons.Default.Security
        "support", "support_agent", "help" -> Icons.AutoMirrored.Filled.Help
        "history", "recent" -> Icons.Default.History
        "local_shipping", "shipping", "delivery" -> Icons.Default.LocalShipping
        "receipt", "invoice", "order" -> Icons.AutoMirrored.Filled.ReceiptLong
        "download", "file_download" -> Icons.Default.FileDownload
        "delete", "trash" -> Icons.Default.Delete
        "share" -> Icons.Default.Share
        "visibility", "eye" -> Icons.Default.Visibility
        "admin_panel", "admin", "dashboard" -> Icons.Default.AdminPanelSettings
        "trending_up", "trend" -> Icons.AutoMirrored.Filled.TrendingUp
        "arrow_back" -> Icons.AutoMirrored.Filled.ArrowBack
        "arrow_forward" -> Icons.AutoMirrored.Filled.ArrowForward
        "close", "clear" -> Icons.Default.Close
        "add", "plus" -> Icons.Default.Add
        "remove", "minus" -> Icons.Default.Remove
        "menu" -> Icons.Default.Menu
        "category", "grid" -> Icons.Default.Category
        "percent", "discount" -> Icons.Default.Discount
        "timer", "clock", "countdown" -> Icons.Default.Timer
        "code" -> Icons.Default.Code
        "currency_exchange", "currency" -> Icons.Default.CurrencyExchange
        "translate", "language_switch" -> Icons.Default.Translate
        "info" -> Icons.Default.Info
        "person", "account", "profile" -> Icons.Default.Person
        else -> Icons.Default.Widgets
    }
}
