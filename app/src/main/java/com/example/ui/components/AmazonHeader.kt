package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCurrency
import com.example.model.AppLanguage
import com.example.model.Category
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmazonHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelect: (String?) -> Unit,
    searchHistory: List<String>,
    onRemoveSearchHistory: (String) -> Unit,
    onClearSearchHistory: () -> Unit,
    currentCurrency: AppCurrency,
    onCurrencyChange: (AppCurrency) -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    cartItemCount: Int,
    onCartClick: () -> Unit,
    onAccountClick: () -> Unit,
    onAdminClick: () -> Unit,
    onNavigateShelf: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchFocused by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkFooter)
    ) {
        // --- Row 1: Brand Logo (Right to Left in RTL), Currency, Account, Cart ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Correct Brand Logo structure:
            // Icon on the right in RTL, AMARIX007 strictly LTR with 007 in Mint, "رسمي" badge, and subtitle underneath
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onSearchQueryChange("")
                    onCategorySelect(null)
                    onNavigateShelf("home")
                }
            ) {
                // Logo Icon on the right
                AppIcon(
                    name = "smart_toy",
                    size = AppIconSize.SMALL,
                    tileColor = PrimaryOlive,
                    iconTint = Mint
                )

                Column(verticalArrangement = Arrangement.Center) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Keep "AMARIX007" strictly LTR so letters and numbers never flip
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AMARIX",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "007",
                                    color = Mint,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                            }
                        }

                        // "رسمي" Official Badge
                        Surface(
                            color = PrimaryOlive,
                            shape = RoundedCornerShape(4.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MediumGreen.copy(alpha = 0.5f))
                            )
                        ) {
                            Text(
                                text = "رسمي",
                                color = Mint,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Subtitle under the logo
                    Text(
                        text = "المتجر الرقمي المعتمد",
                        fontSize = 9.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            // Action Pills (Currency, Account, Cart)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Currency Switcher Pill in Olive/Mint
                Surface(
                    onClick = { showCurrencyDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryOlive,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MediumGreen.copy(alpha = 0.4f))
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentCurrency.symbol,
                                    color = Mint,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = currentCurrency.code,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "عملة",
                            tint = Mint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Account / Admin Menu Button
                Box {
                    IconButton(
                        onClick = { showAccountMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "الحساب",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showAccountMenu,
                        onDismissRequest = { showAccountMenu = false },
                        modifier = Modifier
                            .background(DarkCard)
                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("طلباتي وتاريخ الشراء", color = Color.White, fontSize = 12.sp) },
                            leadingIcon = { SimpleAppIcon("receipt", tint = Mint, size = 18.dp) },
                            onClick = {
                                showAccountMenu = false
                                onAccountClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("تحميلاتي وبيانات التسليم", color = Color.White, fontSize = 12.sp) },
                            leadingIcon = { SimpleAppIcon("download", tint = MintInstantDelivery, size = 18.dp) },
                            onClick = {
                                showAccountMenu = false
                                onNavigateShelf("downloads")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("المفضلة", color = Color.White, fontSize = 12.sp) },
                            leadingIcon = { SimpleAppIcon("favorite", tint = Mint, size = 18.dp) },
                            onClick = {
                                showAccountMenu = false
                                onNavigateShelf("wishlist")
                            }
                        )
                        HorizontalDivider(color = DarkBorder)
                        DropdownMenuItem(
                            text = { Text("لوحة تحكم الإدارة (Admin)", color = Mint, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            leadingIcon = { SimpleAppIcon("admin_panel", tint = Mint, size = 18.dp) },
                            onClick = {
                                showAccountMenu = false
                                onAdminClick()
                            }
                        )
                    }
                }

                // Cart Icon with Bounce Badge
                AnimatedCartBadge(
                    itemCount = cartItemCount,
                    onClick = onCartClick,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // --- Row 2: Search Bar with Category Dropdown & Olive Search Button ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryOliveHover, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = DarkCardSubtle
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Dropdown
                    Box {
                        Surface(
                            onClick = { showCategoryMenu = true },
                            color = PrimaryOlive,
                            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val currentCatName = categories.find { it.id == selectedCategoryId }?.nameAr ?: "الكل"
                                Text(
                                    text = currentCatName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Mint,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "الأقسام",
                                    tint = Mint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier
                                .background(DarkCard)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("جميع الأقسام", fontWeight = FontWeight.Bold, color = Mint) },
                                onClick = {
                                    onCategorySelect(null)
                                    showCategoryMenu = false
                                }
                            )
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.nameAr, color = Color.White) },
                                    leadingIcon = {
                                        SimpleAppIcon(cat.iconName, tint = Mint, size = 18.dp)
                                    },
                                    onClick = {
                                        onCategorySelect(cat.id)
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Search Text Input
                    TextField(
                        value = searchQuery,
                        onValueChange = {
                            onSearchQueryChange(it)
                            isSearchFocused = true
                        },
                        placeholder = {
                            Text(
                                text = "ابحث في AMARIX007 (حسابات، روبوتات، مواقع...)",
                                fontSize = 12.sp,
                                color = TextMutedDark,
                                maxLines = 1
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                isSearchFocused = false
                                onSearchSubmit(searchQuery)
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )

                    // Clear button
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Olive / Mint Search Submit Button
                    Surface(
                        onClick = {
                            focusManager.clearFocus()
                            isSearchFocused = false
                            onSearchSubmit(searchQuery)
                        },
                        color = PrimaryOlive,
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = Mint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Search Suggestions & Recent History Popover ---
        AnimatedVisibility(
            visible = isSearchFocused && (searchHistory.isNotEmpty() || searchQuery.isNotEmpty()),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                color = DarkCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "عمليات البحث الأخيرة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryDark
                        )
                        if (searchHistory.isNotEmpty()) {
                            Text(
                                text = "مسح السجل",
                                fontSize = 11.sp,
                                color = MediumGreen,
                                modifier = Modifier.clickable { onClearSearchHistory() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                        items(searchHistory) { historyItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSearchQueryChange(historyItem)
                                        isSearchFocused = false
                                        focusManager.clearFocus()
                                        onSearchSubmit(historyItem)
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MediumGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = historyItem,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onRemoveSearchHistory(historyItem) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "حذف",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Row 3: Sub-Nav Shelf Pills in Dark/Olive ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Row(
                    modifier = Modifier.clickable { onNavigateShelf("categories") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "الكل",
                        tint = Mint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "جميع الأقسام",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            item {
                Text(
                    text = "عروض اليوم",
                    fontSize = 12.sp,
                    color = Mint,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateShelf("deals") }
                )
            }
            item {
                Text(
                    text = "الأكثر مبيعاً",
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.clickable { onNavigateShelf("bestsellers") }
                )
            }
            item {
                Text(
                    text = "وصل حديثاً",
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.clickable { onNavigateShelf("new") }
                )
            }
            item {
                Text(
                    text = "تحميلاتي",
                    fontSize = 12.sp,
                    color = MintInstantDelivery,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateShelf("downloads") }
                )
            }
        }
    }

    // --- Currency Dialog ---
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            containerColor = DarkCard,
            title = {
                Text(
                    text = "اختر العملة المفضلة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "يتم تحويل الأسعار تلقائياً حسب أحدث أسعار الصرف:",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    AppCurrency.values().forEach { curr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCurrencyChange(curr)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = curr == currentCurrency,
                                    onClick = {
                                        onCurrencyChange(curr)
                                        showCurrencyDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Mint)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(
                                        text = "${curr.code} (${curr.symbol})",
                                        fontWeight = if (curr == currentCurrency) FontWeight.Bold else FontWeight.Normal,
                                        color = if (curr == currentCurrency) Mint else Color.White
                                    )
                                }
                            }
                            Text(
                                text = if (curr == AppCurrency.SAR) "الأساسية" else "${curr.rateFromSar}",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("إغلاق", color = Mint)
                }
            }
        )
    }
}
