package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    savedItems: List<SavedForLaterItem>,
    recommendedProducts: List<Product>,
    currency: AppCurrency,
    activeCoupon: Coupon?,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveFromCart: (String) -> Unit,
    onMoveToSaved: (String) -> Unit,
    onMoveToCartFromSaved: (String) -> Unit,
    onRemoveSaved: (String) -> Unit,
    onApplyCoupon: (String) -> Pair<Boolean, String>,
    onRemoveCoupon: () -> Unit,
    subtotalSar: Double,
    discountSar: Double,
    vatSar: Double,
    totalSar: Double,
    onProceedToCheckout: () -> Unit,
    onContinueShopping: () -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var couponInput by remember { mutableStateOf("") }
    var couponMessage by remember { mutableStateOf<String?>(null) }
    var isCouponSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        if (cartItems.isEmpty() && savedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SimpleAppIcon("shopping_cart_outlined", size = 64.dp, tint = TextMutedDark)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "سلة التسوق فارغة حالياً",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "استكشف خوارزميات التداول، الحسابات الممولة، وسورسات الويب والتطبيقات.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onContinueShopping,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOlive)
                    ) {
                        Text(
                            text = "ابدأ التسوق الآن",
                            color = Mint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                item {
                    Text(
                        text = "سلة المشتريات (${cartItems.sumOf { it.quantity }} منتج)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Cart Items List
                items(cartItems, key = { it.product.id }) { item ->
                    CartItemCard(
                        item = item,
                        currency = currency,
                        onUpdateQuantity = { qty -> onUpdateQuantity(item.product.id, qty) },
                        onRemove = { onRemoveFromCart(item.product.id) },
                        onMoveToSaved = { onMoveToSaved(item.product.id) },
                        onProductClick = { onProductClick(item.product) }
                    )
                }

                // Coupon Code Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "كوبون الخصم الترويجي",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (activeCoupon != null) {
                                Surface(
                                    color = PrimaryOlive,
                                    shape = RoundedCornerShape(8.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(MediumGreen.copy(alpha = 0.5f))
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                Text(
                                                    text = activeCoupon.code,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Mint,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            Text(
                                                text = activeCoupon.descriptionAr,
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        }
                                        TextButton(onClick = onRemoveCoupon) {
                                            Text("إلغاء", color = ErrorRed, fontSize = 11.sp)
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = { couponInput = it.uppercase() },
                                        placeholder = { Text("مثال: AMARIX10 أو VIP007", fontSize = 11.sp, color = TextMutedDark) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Mint,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val (success, msg) = onApplyCoupon(couponInput)
                                            isCouponSuccess = success
                                            couponMessage = msg
                                            if (success) couponInput = ""
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOlive)
                                    ) {
                                        Text("تطبيق", color = Mint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                if (couponMessage != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = couponMessage!!,
                                        color = if (isCouponSuccess) Mint else ErrorRed,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Saved for Later Section
                if (savedItems.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = "محفوظ لوقت لاحق (${savedItems.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            savedItems.forEach { saved ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCardSubtle),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = saved.product.titleAr,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                Text(
                                                    text = currency.format(saved.product.currentPriceSar),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Mint
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedButton(
                                                onClick = { onMoveToCartFromSaved(saved.product.id) },
                                                shape = RoundedCornerShape(6.dp),
                                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                                    brush = androidx.compose.ui.graphics.SolidColor(PrimaryOliveHover)
                                                ),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("نقل للسلة", fontSize = 11.sp, color = Mint)
                                            }
                                            IconButton(
                                                onClick = { onRemoveSaved(saved.product.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = TextMutedDark, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bundle Suggestions
                if (recommendedProducts.isNotEmpty()) {
                    item {
                        HorizontalProductShelf(
                            title = "يُشترى معه عادةً",
                            badgeText = "اقتراح ذكي",
                            products = recommendedProducts.take(3),
                            currency = currency,
                            wishlistIds = emptySet(),
                            onWishlistToggle = {},
                            onAddToCart = { prod -> onUpdateQuantity(prod.id, 1) },
                            onProductClick = onProductClick
                        )
                    }
                }
            }

            // Sticky Bottom Summary Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = DarkFooter,
                tonalElevation = 10.dp,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(currency.format(subtotalSar), fontSize = 11.sp, color = Color.White)
                            Text("المجموع الفرعي:", fontSize = 11.sp, color = TextSecondaryDark)
                        }

                        if (discountSar > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("-${currency.format(discountSar)}", fontSize = 11.sp, color = Mint, fontWeight = FontWeight.Bold)
                                Text("الخصم الترويجي:", fontSize = 11.sp, color = Mint)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(currency.format(vatSar), fontSize = 11.sp, color = Color.White)
                            Text("ضريبة القيمة المضافة (15%):", fontSize = 11.sp, color = TextSecondaryDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("المبلغ الإجمالي للدفع:", fontSize = 11.sp, color = Color.White)
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = currency.format(totalSar),
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Mint
                                )
                            }
                        }

                        Button(
                            onClick = onProceedToCheckout,
                            enabled = cartItems.isNotEmpty(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryOlive,
                                contentColor = Mint
                            ),
                            modifier = Modifier.height(44.dp)
                        ) {
                            SimpleAppIcon("bolt", size = 16.dp, tint = Mint)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "متابعة الشراء (${cartItems.size})",
                                color = Mint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    currency: AppCurrency,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit,
    onMoveToSaved: () -> Unit,
    onProductClick: () -> Unit
) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkCardSubtle)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp))
                        .clickable(onClick = onProductClick),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(name = item.product.iconKey, size = AppIconSize.MEDIUM, tileColor = PrimaryOlive, iconTint = Mint)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.titleAr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        modifier = Modifier.clickable(onClick = onProductClick)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = currency.format(item.product.currentPriceSar),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Mint
                            )
                            val orig = item.product.originalPriceSar
                            if (orig != null && orig > item.product.currentPriceSar) {
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

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "تسليم رقمي فوري",
                        fontSize = 10.sp,
                        color = MintInstantDelivery
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stepper
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryOlive),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onUpdateQuantity(item.quantity - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "إنقاص", tint = Mint, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = item.quantity.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Mint,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { onUpdateQuantity(item.quantity + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "زيادة", tint = Mint, modifier = Modifier.size(14.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "احفظ لوقت لاحق",
                        fontSize = 11.sp,
                        color = MediumGreen,
                        modifier = Modifier.clickable(onClick = onMoveToSaved)
                    )
                    Text(
                        text = "حذف",
                        fontSize = 11.sp,
                        color = TextMutedDark,
                        modifier = Modifier.clickable(onClick = onRemove)
                    )
                }
            }
        }
    }
}
