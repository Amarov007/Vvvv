package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCurrency
import com.example.model.Product
import com.example.ui.theme.*

/**
 * Standard Product Card (Grid / Shelf mode) using official olive & mint palette.
 */
@Composable
fun ProductCard(
    product: Product,
    currency: AppCurrency,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // --- Top Banner: Badges & Wishlist Heart ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Badges in official Olive & Mint
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (product.isBestSeller) {
                        Surface(
                            color = PrimaryOlive,
                            shape = RoundedCornerShape(4.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MediumGreen.copy(alpha = 0.5f))
                            )
                        ) {
                            Text(
                                text = "#${product.bestSellerRank ?: 1} الأكثر مبيعاً",
                                color = Mint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (product.isOnDeal && product.discountPercentage != null) {
                        Surface(
                            color = PrimaryOliveLight,
                            shape = RoundedCornerShape(4.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Mint.copy(alpha = 0.5f))
                            )
                        ) {
                            Text(
                                text = "خصم ${product.discountPercentage}%",
                                color = MintLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Wishlist Heart
                AnimatedHeartButton(
                    isWishlisted = isWishlisted,
                    onToggle = onWishlistToggle,
                    size = 20
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Product Visual Tile (Olive Background with Mint Icon) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCardSubtle)
                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    name = product.iconKey,
                    size = AppIconSize.LARGE,
                    tileColor = PrimaryOlive,
                    iconTint = Mint
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Title ---
            Text(
                text = product.titleAr,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier.height(36.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // --- Ratings & Reviews (Amber ONLY for Star) ---
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AmberStar,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = String.format("%.1f", product.rating),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${product.reviewCount})",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // --- Price Section (Protected with LTR so numbers never invert) ---
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currency.format(product.currentPriceSar),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Mint
                    )

                    val orig = product.originalPriceSar
                    if (orig != null && orig > product.currentPriceSar) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currency.format(orig),
                            fontSize = 11.sp,
                            color = TextMutedDark,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Fast Delivery Tag in MintInstantDelivery
            Row(verticalAlignment = Alignment.CenterVertically) {
                SimpleAppIcon("local_shipping", size = 12.dp, tint = MintInstantDelivery)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "تسليم رقمي فوري",
                    fontSize = 10.sp,
                    color = MintInstantDelivery,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Button in PrimaryOlive with Mint text
            Button(
                onClick = onAddToCart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryOlive,
                    contentColor = Mint
                ),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                SimpleAppIcon("shopping_cart", size = 15.dp, tint = Mint)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "أضف للسلة",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Mint
                )
            }
        }
    }
}

/**
 * List layout variation for Category Screen List Mode.
 */
@Composable
fun ProductListItem(
    product: Product,
    currency: AppCurrency,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Tile
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCardSubtle)
                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    name = product.iconKey,
                    size = AppIconSize.MEDIUM,
                    tileColor = PrimaryOlive,
                    iconTint = Mint
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                if (product.isBestSeller) {
                    Text(
                        text = "#${product.bestSellerRank ?: 1} الأكثر مبيعاً",
                        color = Mint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = product.titleAr,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AmberStar,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "${String.format("%.1f", product.rating)} (${product.reviewCount})",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = currency.format(product.currentPriceSar),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Mint
                        )
                        val orig = product.originalPriceSar
                        if (orig != null && orig > product.currentPriceSar) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currency.format(orig),
                                fontSize = 11.sp,
                                color = TextMutedDark,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedHeartButton(
                    isWishlisted = isWishlisted,
                    onToggle = onWishlistToggle,
                    size = 20
                )

                IconButton(
                    onClick = onAddToCart,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = PrimaryOlive)
                ) {
                    SimpleAppIcon("shopping_cart", size = 16.dp, tint = Mint)
                }
            }
        }
    }
}
