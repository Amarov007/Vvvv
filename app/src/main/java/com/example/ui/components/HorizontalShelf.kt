package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCurrency
import com.example.model.Product
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Shelf for standard product lists using official olive & mint design tokens.
 */
@Composable
fun HorizontalProductShelf(
    title: String,
    badgeText: String? = null,
    badgeColor: Color = PrimaryOlive,
    products: List<Product>,
    currency: AppCurrency,
    wishlistIds: Set<String>,
    onWishlistToggle: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onProductClick: (Product) -> Unit,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Shelf Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (badgeText != null) {
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(4.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MediumGreen.copy(alpha = 0.5f))
                        )
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Mint,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (onSeeAllClick != null) {
                Row(
                    modifier = Modifier.clickable(onClick = onSeeAllClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عرض الكل",
                        fontSize = 12.sp,
                        color = Mint,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Mint,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    currency = currency,
                    isWishlisted = wishlistIds.contains(product.id),
                    onWishlistToggle = { onWishlistToggle(product.id) },
                    onAddToCart = { onAddToCart(product) },
                    onClick = { onProductClick(product) },
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}

/**
 * Deals of the Day Shelf with Live Ticking Countdown Timer using Olive & Mint palette.
 */
@Composable
fun DealsShelfWithCountdown(
    deals: List<Product>,
    currency: AppCurrency,
    wishlistIds: Set<String>,
    onWishlistToggle: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onProductClick: (Product) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (deals.isEmpty()) return

    val targetExpiry = remember(deals) {
        deals.mapNotNull { it.dealExpiryEpochMs }.minOrNull() ?: (System.currentTimeMillis() + (8 * 3600 * 1000L))
    }

    var remainingSeconds by remember {
        mutableStateOf(((targetExpiry - System.currentTimeMillis()) / 1000).coerceAtLeast(0L))
    }

    LaunchedEffect(targetExpiry) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds = ((targetExpiry - System.currentTimeMillis()) / 1000).coerceAtLeast(0L)
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Shelf Header with Countdown Clock
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SimpleAppIcon("bolt", size = 18.dp, tint = Mint)
                    Text(
                        text = "عروض اليوم الحصرية",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Timer Pill (LTR protected so countdown format is 00:00:00 without flip)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ينتهي العرض خلال: ",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                    Surface(
                        color = PrimaryOlive,
                        shape = RoundedCornerShape(4.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MediumGreen.copy(alpha = 0.5f))
                        )
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Mint,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "كل العروض",
                fontSize = 12.sp,
                color = Mint,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSeeAllClick)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(deals, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    currency = currency,
                    isWishlisted = wishlistIds.contains(product.id),
                    onWishlistToggle = { onWishlistToggle(product.id) },
                    onAddToCart = { onAddToCart(product) },
                    onClick = { onProductClick(product) },
                    modifier = Modifier.width(185.dp)
                )
            }
        }
    }
}
