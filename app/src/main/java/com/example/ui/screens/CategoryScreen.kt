package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

enum class SortOption(val titleAr: String) {
    RECOMMENDED("الموصى به"),
    PRICE_LOW_TO_HIGH("السعر: من الأقل للأعلى"),
    PRICE_HIGH_TO_LOW("السعر: من الأعلى للأقل"),
    HIGHEST_RATED("أعلى تقييم"),
    NEWEST("الأحدث")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    category: Category?,
    categories: List<Category>,
    allProducts: List<Product>,
    currency: AppCurrency,
    wishlistIds: Set<String>,
    onCategoryChange: (String?) -> Unit,
    onWishlistToggle: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var isGridView by remember { mutableStateOf(true) }
    var selectedSort by remember { mutableStateOf(SortOption.RECOMMENDED) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var onlyDeals by remember { mutableStateOf(false) }
    var minRating by remember { mutableStateOf<Double?>(null) }
    var maxPriceLimit by remember { mutableStateOf(5000f) }
    val activeCustomFilters = remember { mutableStateMapOf<String, String>() }

    // Filter products
    val filteredProducts = remember(
        category, allProducts, onlyDeals, minRating, maxPriceLimit, activeCustomFilters, selectedSort
    ) {
        var list = allProducts.filter { p ->
            (category == null || p.categoryId == category.id) &&
            (!onlyDeals || p.isOnDeal) &&
            (minRating == null || p.rating >= minRating!!) &&
            (p.currentPriceSar <= maxPriceLimit)
        }

        activeCustomFilters.forEach { (_, chosenOption) ->
            list = list.filter { p ->
                p.attributes.values.any { it.contains(chosenOption, ignoreCase = true) } ||
                p.specifications.values.any { it.contains(chosenOption, ignoreCase = true) } ||
                p.tags.any { it.contains(chosenOption, ignoreCase = true) }
            }
        }

        when (selectedSort) {
            SortOption.RECOMMENDED -> list.sortedByDescending { it.rating * (it.reviewCount + 10) }
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.currentPriceSar }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.currentPriceSar }
            SortOption.HIGHEST_RATED -> list.sortedByDescending { it.rating }
            SortOption.NEWEST -> list.sortedByDescending { it.createdAtEpochMs }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // --- 1. Category Hero Identity Banner (Olive/Mint Theme) ---
        if (category != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(
                        name = category.iconName,
                        size = AppIconSize.LARGE,
                        tileColor = PrimaryOlive,
                        iconTint = Mint
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.nameAr,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = category.descriptionAr,
                            fontSize = 11.sp,
                            color = TextSecondaryDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // --- 2. Category Switcher Chips Bar ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCardSubtle)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = category == null,
                    onClick = { onCategoryChange(null) },
                    label = { Text("الكل", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryOlive,
                        selectedLabelColor = Mint,
                        containerColor = DarkCard,
                        labelColor = TextSecondaryDark
                    )
                )
            }
            items(categories) { cat ->
                FilterChip(
                    selected = category?.id == cat.id,
                    onClick = { onCategoryChange(cat.id) },
                    label = { Text(cat.nameAr, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryOlive,
                        selectedLabelColor = Mint,
                        containerColor = DarkCard,
                        labelColor = TextSecondaryDark
                    )
                )
            }
        }

        // --- 3. Filter Bar (Filter Button, Active Chips, Grid/List Toggle) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Filter Button
            OutlinedButton(
                onClick = { showFilterSheet = true },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                border = ButtonDefaults.outlinedButtonBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(PrimaryOliveHover)
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Mint)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "فلاتر",
                    tint = Mint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val totalFiltersCount = (if (onlyDeals) 1 else 0) + (if (minRating != null) 1 else 0) + activeCustomFilters.size
                Text(
                    text = if (totalFiltersCount > 0) "فلاتر ($totalFiltersCount)" else "تصفية",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Mint
                )
            }

            // Results Count
            Text(
                text = "${filteredProducts.size} نتيجة",
                fontSize = 12.sp,
                color = TextSecondaryDark
            )

            // Grid / List Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            ) {
                IconButton(
                    onClick = { isGridView = true },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "شبكة",
                        tint = if (isGridView) Mint else TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { isGridView = false },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewList,
                        contentDescription = "قائمة",
                        tint = if (!isGridView) Mint else TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Active Filter Chips
        if (onlyDeals || minRating != null || activeCustomFilters.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onlyDeals) {
                    item {
                        ActiveFilterChip(label = "عروض فقط", onRemove = { onlyDeals = false })
                    }
                }
                if (minRating != null) {
                    item {
                        ActiveFilterChip(label = "${minRating}★ وأعلى", onRemove = { minRating = null })
                    }
                }
                items(activeCustomFilters.entries.toList()) { entry ->
                    ActiveFilterChip(
                        label = entry.value,
                        onRemove = { activeCustomFilters.remove(entry.key) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // --- 4. Products View ---
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SimpleAppIcon("search", size = 48.dp, tint = TextMutedDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد نتائج مطابقة للفلاتر المحددة",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "جرّب تخفيف معايير التصفية للوصول لخيارات أكثر",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            onlyDeals = false
                            minRating = null
                            activeCustomFilters.clear()
                            maxPriceLimit = 5000f
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOlive)
                    ) {
                        Text("إعادة ضبط الفلاتر", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            currency = currency,
                            isWishlisted = wishlistIds.contains(product.id),
                            onWishlistToggle = { onWishlistToggle(product.id) },
                            onAddToCart = { onAddToCart(product) },
                            onClick = { onProductClick(product) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductListItem(
                            product = product,
                            currency = currency,
                            isWishlisted = wishlistIds.contains(product.id),
                            onWishlistToggle = { onWishlistToggle(product.id) },
                            onAddToCart = { onAddToCart(product) },
                            onClick = { onProductClick(product) }
                        )
                    }
                }
            }
        }
    }

    // --- Side / Modal Filter Sheet ---
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = DarkCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تصفية المنتجات",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(
                        onClick = {
                            onlyDeals = false
                            minRating = null
                            activeCustomFilters.clear()
                            maxPriceLimit = 5000f
                        }
                    ) {
                        Text("مسح الكل", color = Mint)
                    }
                }

                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))

                // Deals toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("عروض وتخفيضات اليوم فقط", color = Color.White, fontSize = 13.sp)
                    Switch(
                        checked = onlyDeals,
                        onCheckedChange = { onlyDeals = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Mint,
                            checkedTrackColor = PrimaryOlive
                        )
                    )
                }

                // Minimum Rating Filter
                Text("أقل تقييم للعملاء", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(4.8, 4.5, 4.0).forEach { rating ->
                        FilterChip(
                            selected = minRating == rating,
                            onClick = { minRating = if (minRating == rating) null else rating },
                            label = { Text("${rating}★ فما فوق", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryOlive,
                                selectedLabelColor = Mint
                            )
                        )
                    }
                }

                // Specialized Department Category Filters
                if (category != null && category.filters.isNotEmpty()) {
                    category.filters.forEach { filterSpec ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = filterSpec.titleAr,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            items(filterSpec.options) { option ->
                                val isSelected = activeCustomFilters[filterSpec.id] == option
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            activeCustomFilters.remove(filterSpec.id)
                                        } else {
                                            activeCustomFilters[filterSpec.id] = option
                                        }
                                    },
                                    label = { Text(option, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryOlive,
                                        selectedLabelColor = Mint
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOlive)
                ) {
                    Text(
                        text = "تطبيق النتائج (${filteredProducts.size})",
                        color = Mint,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(
    label: String,
    onRemove: () -> Unit
) {
    Surface(
        color = PrimaryOlive,
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MediumGreen.copy(alpha = 0.5f))
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 11.sp, color = Mint)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "إلغاء",
                tint = Mint,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}
