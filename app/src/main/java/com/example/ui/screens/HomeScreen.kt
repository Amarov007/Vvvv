package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    categories: List<Category>,
    products: List<Product>,
    banners: List<PromoBanner>,
    currency: AppCurrency,
    wishlistIds: Set<String>,
    recentlyViewedIds: List<String>,
    onWishlistToggle: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onProductClick: (Product) -> Unit,
    onCategoryClick: (String) -> Unit,
    onNavigateShelf: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val deals = remember(products) { products.filter { it.isOnDeal } }
    val bestSellers = remember(products) { products.filter { it.isBestSeller }.sortedBy { it.bestSellerRank ?: 99 } }
    val newArrivals = remember(products) { products.sortedByDescending { it.createdAtEpochMs } }
    val recentlyViewed = remember(products, recentlyViewedIds) {
        recentlyViewedIds.mapNotNull { id -> products.find { it.id == id } }
    }
    val recommended = remember(products) { products.filter { it.rating >= 4.85 } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // --- 1. Hero Promo Carousel Banner ---
        item {
            val activeBanners = banners.filter { it.isActive }
            if (activeBanners.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { activeBanners.size })
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) { page ->
                        val banner = activeBanners[page]
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    if (banner.targetCategoryId != null) {
                                        onCategoryClick(banner.targetCategoryId)
                                    } else if (banner.targetProductId != null) {
                                        val p = products.find { it.id == banner.targetProductId }
                                        if (p != null) onProductClick(p)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(DarkCard)
                                    .padding(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.72f),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        color = PrimaryOlive,
                                        shape = RoundedCornerShape(4.dp),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(MediumGreen.copy(alpha = 0.5f))
                                        )
                                    ) {
                                        Text(
                                            text = banner.tagAr,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Mint,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = banner.titleAr,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = banner.subtitleAr,
                                            fontSize = 11.sp,
                                            color = TextSecondaryDark,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "تسوق الآن",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Mint
                                        )
                                        SimpleAppIcon("arrow_forward", size = 12.dp, tint = Mint)
                                    }
                                }

                                // Graphic icon in brand olive & mint tile
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 6.dp)
                                ) {
                                    AppIcon(
                                        name = banner.iconKey,
                                        size = AppIconSize.LARGE,
                                        tileColor = PrimaryOlive,
                                        iconTint = Mint
                                    )
                                }
                            }
                        }
                    }

                    // Dot indicators
                    if (activeBanners.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(activeBanners.size) { iteration ->
                                val isSelected = pagerState.currentPage == iteration
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(if (isSelected) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Mint else DarkBorder)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Categories Department Cards ---
        // As requested: all 4 categories use the template olive/mint colors, distinguished by icon and title
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = "أقسام AMARIX007 المتخصصة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories.filter { it.isVisible }.sortedBy { it.order }) { cat ->
                        val count = products.count { it.categoryId == cat.id }
                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { onCategoryClick(cat.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AppIcon(
                                    name = cat.iconName,
                                    size = AppIconSize.MEDIUM,
                                    tileColor = PrimaryOlive,
                                    iconTint = Mint
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = cat.nameAr,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "$count منتج متاح",
                                    fontSize = 10.sp,
                                    color = MintInstantDelivery
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Deals of the Day Shelf ---
        item {
            DealsShelfWithCountdown(
                deals = deals,
                currency = currency,
                wishlistIds = wishlistIds,
                onWishlistToggle = onWishlistToggle,
                onAddToCart = onAddToCart,
                onProductClick = onProductClick,
                onSeeAllClick = { onNavigateShelf("deals") }
            )
        }

        // --- 4. Best Sellers Shelf ---
        item {
            HorizontalProductShelf(
                title = "الأكثر مبيعاً في المتجر",
                badgeText = "الأعلى طلباً",
                badgeColor = PrimaryOlive,
                products = bestSellers,
                currency = currency,
                wishlistIds = wishlistIds,
                onWishlistToggle = onWishlistToggle,
                onAddToCart = onAddToCart,
                onProductClick = onProductClick,
                onSeeAllClick = { onNavigateShelf("bestsellers") }
            )
        }

        // --- 5. New Arrivals Shelf ---
        item {
            HorizontalProductShelf(
                title = "وصل حديثاً",
                badgeText = "جديد",
                badgeColor = PrimaryOliveLight,
                products = newArrivals,
                currency = currency,
                wishlistIds = wishlistIds,
                onWishlistToggle = onWishlistToggle,
                onAddToCart = onAddToCart,
                onProductClick = onProductClick
            )
        }

        // --- 6. Recently Viewed Shelf ---
        item {
            HorizontalProductShelf(
                title = "شوهد مؤخراً",
                products = recentlyViewed,
                currency = currency,
                wishlistIds = wishlistIds,
                onWishlistToggle = onWishlistToggle,
                onAddToCart = onAddToCart,
                onProductClick = onProductClick
            )
        }

        // --- 7. Recommended For You Shelf ---
        item {
            HorizontalProductShelf(
                title = "مقترح لك خصيصاً",
                badgeText = "تقييم 4.9+ ★",
                badgeColor = PrimaryOlive,
                products = recommended,
                currency = currency,
                wishlistIds = wishlistIds,
                onWishlistToggle = onWishlistToggle,
                onAddToCart = onAddToCart,
                onProductClick = onProductClick
            )
        }

        // --- 8. Trust Badges Row ---
        item {
            TrustBadgesRow()
        }
    }
}
