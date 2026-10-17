package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCurrency
import com.example.model.DeliveredAsset
import com.example.model.Order
import com.example.ui.components.AppIcon
import com.example.ui.components.AppIconSize
import com.example.ui.components.SimpleAppIcon
import com.example.ui.theme.*

@Composable
fun OrderSuccessScreen(
    order: Order,
    currency: AppCurrency,
    onViewOrders: () -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Success Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppIcon(
                        name = "check_circle",
                        size = AppIconSize.LARGE,
                        tileColor = PrimaryOlive,
                        iconTint = Mint
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "تم تأكيد طلبك بنجاح!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "Order: ${order.orderNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Mint
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "المبلغ الإجمالي المدفوع: ${currency.format(order.totalSar)}",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
        }

        // Direct Delivery Credentials
        item {
            Text(
                text = "بيانات الاستلام والتسليم الفوري",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "تم تجهيز روابط الوصول وبيانات الاعتماد الخاصة بمنتجاتك المشتراة:",
                fontSize = 11.sp,
                color = TextSecondaryDark
            )
        }

        items(order.deliveredAssets) { asset ->
            DeliveredAssetCard(
                asset = asset,
                onCopy = {
                    clipboardManager.setText(AnnotatedString(asset.payload))
                    Toast.makeText(context, "تم نسخ بيانات التسليم إلى الحافظة بنجاح", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Action Buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onViewOrders,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryOlive,
                        contentColor = Mint
                    )
                ) {
                    SimpleAppIcon("receipt", size = 16.dp, tint = Mint)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "عرض في طلباتي وتحميلاتي",
                        color = Mint,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onContinueShopping,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PrimaryOliveHover)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("متابعة التسوق في AMARIX007")
                }
            }
        }
    }
}

@Composable
fun DeliveredAssetCard(
    asset: DeliveredAsset,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
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
                Text(
                    text = asset.productTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = PrimaryOlive,
                    shape = RoundedCornerShape(4.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MediumGreen.copy(alpha = 0.5f))
                    )
                ) {
                    Text(
                        text = asset.deliveryType.labelAr,
                        color = Mint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payload Box (Always LTR so links, servers, logins are completely readable)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkCardSubtle,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorderSubtle)
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = asset.payload,
                            fontSize = 12.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Mint,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (asset.instructions.isNotEmpty()) {
                    Text(
                        text = asset.instructions,
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = onCopy,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryOlive,
                        contentColor = Mint
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    SimpleAppIcon("content_copy", size = 14.dp, tint = Mint)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "نسخ البيانات",
                        fontSize = 11.sp,
                        color = Mint,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
