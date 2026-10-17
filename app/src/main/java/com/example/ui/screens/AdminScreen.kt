package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.AppIcon
import com.example.ui.components.AppIconSize
import com.example.ui.components.SimpleAppIcon
import com.example.ui.theme.*

enum class AdminTab(val titleAr: String, val iconKey: String) {
    DEALS("العروض والتخفيضات", "bolt"),
    PRODUCTS("المنتجات والتسليم", "category"),
    CATEGORIES("إدارة الأقسام", "menu"),
    BANNERS("بانرات الرئيسية", "smart_toy"),
    ANALYTICS("مخططات المبيعات", "trending_up")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    products: List<Product>,
    categories: List<Category>,
    banners: List<PromoBanner>,
    orders: List<Order>,
    currency: AppCurrency,
    onUpdateProductDeal: (String, Int?, Int?) -> Unit,
    onToggleBestSeller: (String) -> Unit,
    onToggleFeatured: (String) -> Unit,
    onUpdateProductPriceAndDelivery: (String, Double, String, String) -> Unit,
    onUpdateCategory: (Category) -> Unit,
    onUpdateBanner: (PromoBanner) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AdminTab.DEALS) }
    val context = LocalContext.current

    // Dialog state for editing a product's price and delivery
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var editPriceText by remember { mutableStateOf("") }
    var editDeliveryPayload by remember { mutableStateOf("") }
    var editDeliveryInstructions by remember { mutableStateOf("") }

    // Dialog state for editing a category (strictly name, desc, order, visibility - no color bloat)
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var editCatNameAr by remember { mutableStateOf("") }
    var editCatDescAr by remember { mutableStateOf("") }
    var editCatOrderText by remember { mutableStateOf("") }
    var editCatVisible by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "لوحة تحكم إدارة المتجر (Admin)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Mint
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkFooter)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Admin Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = DarkFooter,
                contentColor = Mint,
                edgePadding = 12.dp
            ) {
                AdminTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SimpleAppIcon(tab.iconKey, size = 16.dp, tint = if (selectedTab == tab) Mint else TextSecondaryDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.titleAr,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == tab) Mint else TextSecondaryDark
                                )
                            }
                        }
                    )
                }
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    AdminTab.DEALS -> {
                        item {
                            Text(
                                text = "إدارة عروض اليوم والتخفيضات النشطة",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تحديد نسبة الخصم وساعات العرض لتحديث العدّاد التنازلي التلقائي:",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }

                        items(products, key = { it.id }) { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = product.titleAr,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1
                                            )
                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                Text(
                                                    text = "السعر الأساسي: ${currency.format(product.priceSar)}",
                                                    fontSize = 11.sp,
                                                    color = Mint
                                                )
                                            }
                                        }

                                        if (product.isOnDeal) {
                                            Surface(
                                                color = PrimaryOliveLight,
                                                shape = RoundedCornerShape(4.dp),
                                                border = CardDefaults.outlinedCardBorder().copy(
                                                    brush = androidx.compose.ui.graphics.SolidColor(Mint.copy(alpha = 0.5f))
                                                )
                                            ) {
                                                Text(
                                                    text = "عرض نشط (${product.dealDiscountPercent}%)",
                                                    color = MintLight,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (product.isOnDeal) {
                                            OutlinedButton(
                                                onClick = {
                                                    onUpdateProductDeal(product.id, null, null)
                                                    Toast.makeText(context, "تم إيقاف العرض", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                                    brush = androidx.compose.ui.graphics.SolidColor(ErrorRed.copy(alpha = 0.6f))
                                                ),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("إيقاف العرض", fontSize = 11.sp)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    onUpdateProductDeal(product.id, 25, 8)
                                                    Toast.makeText(context, "تم تفعيل عرض 25% لمدة 8 ساعات", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = PrimaryOlive,
                                                    contentColor = Mint
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("تفعيل عرض 25% (8 ساعات)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = {
                                                    onUpdateProductDeal(product.id, 35, 24)
                                                    Toast.makeText(context, "تم تفعيل عرض 35% لمدة 24 ساعة", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = DarkCardSubtle,
                                                    contentColor = Color.White
                                                ),
                                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("عرض 35% (يوم)", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdminTab.PRODUCTS -> {
                        item {
                            Text(
                                text = "إدارة المنتجات، الأسعار، وبيانات التسليم الفعلي",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        items(products, key = { it.id }) { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = product.titleAr,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = currency.format(product.priceSar),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Mint
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        color = DarkCardSubtle,
                                        shape = RoundedCornerShape(6.dp),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(DarkBorderSubtle)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "نوع التسليم: ${product.deliveryType.labelAr}",
                                                fontSize = 11.sp,
                                                color = MintInstantDelivery,
                                                fontWeight = FontWeight.Bold
                                            )
                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                Text(
                                                    text = "البيانات: ${product.deliveryPayload.take(50)}...",
                                                    fontSize = 10.sp,
                                                    color = TextSecondaryDark,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilterChip(
                                            selected = product.isBestSeller,
                                            onClick = { onToggleBestSeller(product.id) },
                                            label = {
                                                Text(
                                                    if (product.isBestSeller) "الأكثر مبيعاً (#${product.bestSellerRank ?: 1})" else "وسم الأكثر مبيعاً",
                                                    fontSize = 10.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PrimaryOlive,
                                                selectedLabelColor = Mint
                                            )
                                        )

                                        FilterChip(
                                            selected = product.isFeatured,
                                            onClick = { onToggleFeatured(product.id) },
                                            label = { Text("مميز", fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PrimaryOliveLight,
                                                selectedLabelColor = MintLight
                                            )
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        IconButton(
                                            onClick = {
                                                editingProduct = product
                                                editPriceText = product.priceSar.toString()
                                                editDeliveryPayload = product.deliveryPayload
                                                editDeliveryInstructions = product.deliveryInstructionsAr
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Mint, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdminTab.CATEGORIES -> {
                        item {
                            Text(
                                text = "إدارة أقسام المتجر (الأيقونة، الترتيب، الظهور)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        items(categories, key = { it.id }) { cat ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AppIcon(
                                            name = cat.iconName,
                                            size = AppIconSize.MEDIUM,
                                            tileColor = PrimaryOlive,
                                            iconTint = Mint
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "${cat.nameAr} (#${cat.order})",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = cat.descriptionAr,
                                                fontSize = 10.sp,
                                                color = TextSecondaryDark,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Switch(
                                            checked = cat.isVisible,
                                            onCheckedChange = { onUpdateCategory(cat.copy(isVisible = it)) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Mint,
                                                checkedTrackColor = PrimaryOlive
                                            )
                                        )

                                        IconButton(
                                            onClick = {
                                                editingCategory = cat
                                                editCatNameAr = cat.nameAr
                                                editCatDescAr = cat.descriptionAr
                                                editCatOrderText = cat.order.toString()
                                                editCatVisible = cat.isVisible
                                            }
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Mint)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdminTab.BANNERS -> {
                        item {
                            Text(
                                text = "إدارة بانرات العروض في الصفحة الرئيسية",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        items(banners, key = { it.id }) { banner ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = banner.titleAr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = banner.subtitleAr, fontSize = 11.sp, color = TextSecondaryDark)
                                        Text(text = "الوسم: ${banner.tagAr}", fontSize = 10.sp, color = Mint)
                                    }

                                    Switch(
                                        checked = banner.isActive,
                                        onCheckedChange = { onUpdateBanner(banner.copy(isActive = it)) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Mint,
                                            checkedTrackColor = PrimaryOlive
                                        )
                                    )
                                }
                            }
                        }
                    }

                    AdminTab.ANALYTICS -> {
                        val totalSales = orders.sumOf { it.totalSar }
                        val totalOrdersCount = orders.size

                        item {
                            Text(
                                text = "إحصائيات ومؤشرات المبيعات الحقيقية",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("إجمالي الإيرادات", fontSize = 11.sp, color = TextSecondaryDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(currency.format(totalSales), fontSize = 17.sp, fontWeight = FontWeight.Black, color = Mint)
                                        }
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("عدد الطلبات المكتملة", fontSize = 11.sp, color = TextSecondaryDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$totalOrdersCount طلب", fontSize = 17.sp, fontWeight = FontWeight.Black, color = MintInstantDelivery)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("توزيع المنتجات والمبيعات حسب الأقسام", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    categories.forEach { cat ->
                                        val catProductsCount = products.count { it.categoryId == cat.id }
                                        val percent = (catProductsCount.toFloat() / products.size.coerceAtLeast(1))
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(cat.nameAr, fontSize = 11.sp, color = Color.White)
                                                Text("$catProductsCount منتج (${(percent * 100).toInt()}%)", fontSize = 11.sp, color = TextSecondaryDark)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            LinearProgressIndicator(
                                                progress = { percent },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = MediumGreen,
                                                trackColor = PrimaryOliveDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Product Edit Dialog ---
    if (editingProduct != null) {
        val prod = editingProduct!!
        AlertDialog(
            onDismissRequest = { editingProduct = null },
            containerColor = DarkCard,
            title = { Text("تعديل بيانات المنتج والتسليم", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = prod.titleAr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Mint)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editPriceText,
                        onValueChange = { editPriceText = it },
                        label = { Text("السعر (ر.س)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Mint,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editDeliveryPayload,
                        onValueChange = { editDeliveryPayload = it },
                        label = { Text("بيانات التسليم (رابط أو حساب أو تنزيل)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Mint,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editDeliveryInstructions,
                        onValueChange = { editDeliveryInstructions = it },
                        label = { Text("تعليمات الاستخدام للمشتري") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Mint,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPrice = editPriceText.toDoubleOrNull() ?: prod.priceSar
                        onUpdateProductPriceAndDelivery(prod.id, newPrice, editDeliveryPayload, editDeliveryInstructions)
                        Toast.makeText(context, "تم حفظ تعديلات المنتج بنجاح", Toast.LENGTH_SHORT).show()
                        editingProduct = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryOlive,
                        contentColor = Mint
                    )
                ) {
                    Text("حفظ التعديلات", color = Mint, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProduct = null }) {
                    Text("إلغاء", color = TextSecondaryDark)
                }
            }
        )
    }

    // --- Category Edit Dialog (No Color Input, Clean Department Config) ---
    if (editingCategory != null) {
        val cat = editingCategory!!
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            containerColor = DarkCard,
            title = { Text("تعديل القسم: ${cat.nameAr}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editCatNameAr,
                        onValueChange = { editCatNameAr = it },
                        label = { Text("اسم القسم") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Mint,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editCatDescAr,
                        onValueChange = { editCatDescAr = it },
                        label = { Text("وصف القسم بسطر واحد") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Mint,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editCatOrderText,
                        onValueChange = { editCatOrderText = it },
                        label = { Text("ترتيب العرض (Order)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Mint,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newOrder = editCatOrderText.toIntOrNull() ?: cat.order
                        onUpdateCategory(
                            cat.copy(
                                nameAr = editCatNameAr,
                                descriptionAr = editCatDescAr,
                                order = newOrder,
                                isVisible = editCatVisible
                            )
                        )
                        Toast.makeText(context, "تم حفظ بيانات القسم", Toast.LENGTH_SHORT).show()
                        editingCategory = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryOlive,
                        contentColor = Mint
                    )
                ) {
                    Text("حفظ القسم", color = Mint, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("إلغاء", color = TextSecondaryDark)
                }
            }
        )
    }
}
