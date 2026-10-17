package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCurrency
import com.example.model.CartItem
import com.example.model.Order
import com.example.ui.components.SimpleAppIcon
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    items: List<CartItem>,
    currency: AppCurrency,
    subtotalSar: Double,
    discountSar: Double,
    vatSar: Double,
    totalSar: Double,
    onPlaceOrder: (String, String, String) -> Order,
    onOrderCompleted: (Order) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(1) } // 1: Info, 2: Payment, 3: Confirmation
    var customerName by remember { mutableStateOf("عبدالرحمن الشريف") }
    var customerEmail by remember { mutableStateOf("customer@amarix007.com") }
    var customerPhone by remember { mutableStateOf("+966501234567") }
    var selectedPaymentMethod by remember { mutableStateOf("مدى / بطاقة ائتمانية") }
    var isProcessing by remember { mutableStateOf(false) }

    val paymentOptions = listOf(
        "مدى / بطاقة ائتمانية" to "credit_card",
        "Apple Pay" to "smartphone",
        "تحويل بنكي مباشر" to "account_balance",
        "عملات رقمية (USDT)" to "bolt"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "إتمام الطلب السريع",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
            // --- Visual Step Indicator ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard)
                    .border(1.dp, DarkBorder)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepItem(stepNumber = 1, title = "البيانات", isActive = currentStep == 1, isCompleted = currentStep > 1)
                Box(modifier = Modifier.width(30.dp).height(2.dp).background(if (currentStep > 1) Mint else DarkBorder))
                StepItem(stepNumber = 2, title = "طريقة الدفع", isActive = currentStep == 2, isCompleted = currentStep > 2)
                Box(modifier = Modifier.width(30.dp).height(2.dp).background(if (currentStep > 2) Mint else DarkBorder))
                StepItem(stepNumber = 3, title = "تأكيد الطلب", isActive = currentStep == 3, isCompleted = false)
            }

            // Step Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (currentStep) {
                    1 -> {
                        item {
                            Text(
                                text = "1. بيانات المستلم وتأكيد الاستلام",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("الاسم الكامل") },
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
                                value = customerEmail,
                                onValueChange = { customerEmail = it },
                                label = { Text("البريد الإلكتروني (لاستلام نسخة الاعتماد)") },
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
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("رقم الجوال") },
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
                    }

                    2 -> {
                        item {
                            Text(
                                text = "2. اختر طريقة الدفع المعتمدة",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            paymentOptions.forEach { (method, iconKey) ->
                                val isSelected = selectedPaymentMethod == method
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { selectedPaymentMethod = method },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) PrimaryOlive else DarkCard
                                    ),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(
                                            if (isSelected) Mint else DarkBorder
                                        )
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedPaymentMethod = method },
                                            colors = RadioButtonDefaults.colors(selectedColor = Mint)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        SimpleAppIcon(iconKey, size = 20.dp, tint = if (isSelected) Mint else Color.White)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = method,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        item {
                            Text(
                                text = "3. مراجعة الطلب وتأكيد الشراء",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("المشتري: $customerName", color = Color.White, fontSize = 12.sp)
                                    Text("البريد: $customerEmail", color = TextSecondaryDark, fontSize = 12.sp)
                                    Text("طريقة الدفع: $selectedPaymentMethod", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                                    items.forEach { cartItem ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${cartItem.product.titleAr} (×${cartItem.quantity})",
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                modifier = Modifier.weight(1f)
                                            )
                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                Text(
                                                    text = currency.format(cartItem.product.currentPriceSar * cartItem.quantity),
                                                    fontSize = 12.sp,
                                                    color = Mint,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("الإجمالي النهائي:", fontWeight = FontWeight.Bold, color = Color.White)
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = currency.format(totalSar),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 18.sp,
                                                color = Mint
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = DarkFooter,
                tonalElevation = 8.dp,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(PrimaryOliveHover)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("السابق")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentStep < 3) {
                        Button(
                            onClick = { currentStep++ },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryOlive,
                                contentColor = Mint
                            )
                        ) {
                            Text("التالي", color = Mint, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                isProcessing = true
                                val order = onPlaceOrder(customerName, customerEmail, selectedPaymentMethod)
                                isProcessing = false
                                onOrderCompleted(order)
                            },
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryOlive,
                                contentColor = Mint
                            ),
                            modifier = Modifier.height(44.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Mint, modifier = Modifier.size(20.dp))
                            } else {
                                SimpleAppIcon("check_circle", size = 18.dp, tint = Mint)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تأكيد الدفع والاستلام الفوري",
                                    color = Mint,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> PrimaryOliveHover
                        isActive -> PrimaryOlive
                        else -> DarkBorder
                    }
                )
                .border(
                    1.dp,
                    if (isActive || isCompleted) Mint else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
            } else {
                Text(
                    text = stepNumber.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Mint else TextMutedDark
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive || isCompleted) Color.White else TextMutedDark
        )
    }
}
