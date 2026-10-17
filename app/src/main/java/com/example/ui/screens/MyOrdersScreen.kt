package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.SimpleAppIcon
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class OrdersTab(val titleAr: String) {
    ORDERS("سجل الطلبات"),
    DOWNLOADS("تحميلاتي وبيانات التسليم")
}

@Composable
fun MyOrdersScreen(
    orders: List<Order>,
    currency: AppCurrency,
    onBuyAgain: (Product) -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(OrdersTab.ORDERS) }
    var selectedInvoiceOrder by remember { mutableStateOf<Order?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val allDeliveredAssets = remember(orders) {
        orders.flatMap { it.deliveredAssets }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Tab Bar
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = DarkFooter,
            contentColor = Mint
        ) {
            OrdersTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.titleAr,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == tab) Mint else TextSecondaryDark
                        )
                    }
                )
            }
        }

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SimpleAppIcon("receipt", size = 56.dp, tint = TextMutedDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد طلبات سابقة حتى الآن",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "كافة مشترياتك الرقمية وبيانات التسليم ستظهر هنا فور إتمام الطلب.",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onContinueShopping,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOlive)
                    ) {
                        Text("تصفح المتجر", color = Mint, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            when (selectedTab) {
                OrdersTab.ORDERS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(orders, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                currency = currency,
                                onBuyAgain = { item -> onBuyAgain(item.product) },
                                onShowInvoice = { selectedInvoiceOrder = order }
                            )
                        }
                    }
                }

                OrdersTab.DOWNLOADS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "مركز تحميلاتي وبيانات الوصول المعتمدة (${allDeliveredAssets.size} أصل رقمي)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "روابط السورسات وبيانات الحسابات والملفات التنفيذية متاحة دائماً:",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }

                        items(allDeliveredAssets) { asset ->
                            DeliveredAssetCard(
                                asset = asset,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(asset.payload))
                                    Toast.makeText(context, "تم نسخ بيانات التسليم إلى الحافظة بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Invoice Modal Dialog ---
    if (selectedInvoiceOrder != null) {
        val inv = selectedInvoiceOrder!!
        AlertDialog(
            onDismissRequest = { selectedInvoiceOrder = null },
            containerColor = DarkCard,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فاتورة ضريبية مبسطة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Surface(color = PrimaryOlive, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = "مدفوعة بالكامل",
                            color = Mint,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("المتجر: AMARIX007 الرقمي", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text("رقم الفاتورة: INV-${inv.orderNumber}", fontSize = 11.sp, color = TextSecondaryDark)
                    }
                    val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(inv.dateEpochMs))
                    Text("تاريخ الإصدار: $dateFormatted", fontSize = 11.sp, color = TextSecondaryDark)
                    Text("العميل: ${inv.customerName}", fontSize = 11.sp, color = TextSecondaryDark)

                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))

                    inv.items.forEach { itm ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${itm.product.titleAr} (×${itm.quantity})",
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = currency.format(itm.product.currentPriceSar * itm.quantity),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Mint
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(currency.format(inv.subtotalSar), fontSize = 11.sp, color = Color.White)
                            Text("المجموع الفرعي:", fontSize = 11.sp, color = TextSecondaryDark)
                        }
                        if (inv.discountSar > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("-${currency.format(inv.discountSar)}", fontSize = 11.sp, color = Mint)
                                Text("الخصم:", fontSize = 11.sp, color = Mint)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(currency.format(inv.vatSar), fontSize = 11.sp, color = Color.White)
                            Text("ضريبة القيمة المضافة (15%):", fontSize = 11.sp, color = TextSecondaryDark)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(currency.format(inv.totalSar), fontWeight = FontWeight.Black, fontSize = 14.sp, color = Mint)
                            Text("الإجمالي الكلي المدفوع:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "تم حفظ الفاتورة الضريبية بصيغة PDF", Toast.LENGTH_SHORT).show()
                        selectedInvoiceOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryOlive,
                        contentColor = Mint
                    )
                ) {
                    Text("تنزيل الفاتورة", color = Mint, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInvoiceOrder = null }) {
                    Text("إغلاق", color = TextSecondaryDark)
                }
            }
        )
    }
}

@Composable
private fun OrderCard(
    order: Order,
    currency: AppCurrency,
    onBuyAgain: (CartItem) -> Unit,
    onShowInvoice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "Order: ${order.orderNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    val dateFormatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(order.dateEpochMs))
                    Text(text = "تاريخ الطلب: $dateFormatted", fontSize = 10.sp, color = TextSecondaryDark)
                }

                Surface(
                    color = PrimaryOlive,
                    shape = RoundedCornerShape(4.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MediumGreen.copy(alpha = 0.5f))
                    )
                ) {
                    Text(
                        text = order.status.titleAr,
                        color = Mint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Order Status Tracker Timeline
            OrderStatusTimeline(status = order.status)

            Spacer(modifier = Modifier.height(12.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.titleAr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "${currency.format(item.product.currentPriceSar)} × ${item.quantity}",
                                fontSize = 11.sp,
                                color = Mint
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { onBuyAgain(item) },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(PrimaryOliveHover)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Mint)
                    ) {
                        SimpleAppIcon("bolt", size = 12.dp, tint = Mint)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اشترِ مجدداً", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = "Total: ${currency.format(order.totalSar)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Mint
                    )
                }

                TextButton(onClick = onShowInvoice) {
                    SimpleAppIcon("receipt", size = 14.dp, tint = MediumGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عرض الفاتورة", fontSize = 11.sp, color = MediumGreen)
                }
            }
        }
    }
}

@Composable
private fun OrderStatusTimeline(status: OrderStatus) {
    val steps = listOf(
        "تم الطلب" to 0,
        "تجهيز الاعتماد" to 1,
        "تم التسليم" to 2
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCardSubtle)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, (label, stepIndex) ->
            val isPassed = status.stepIndex >= stepIndex
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isPassed) PrimaryOlive else DarkBorder)
                        .border(1.dp, if (isPassed) Mint else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassed) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Mint, modifier = Modifier.size(10.dp))
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isPassed) FontWeight.Bold else FontWeight.Normal,
                    color = if (isPassed) Color.White else TextMutedDark
                )
            }
            if (idx < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .padding(horizontal = 4.dp)
                        .background(if (status.stepIndex > stepIndex) MediumGreen else DarkBorder)
                )
            }
        }
    }
}
