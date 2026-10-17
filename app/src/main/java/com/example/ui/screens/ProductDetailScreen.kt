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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

enum class ProductDetailTab(val titleAr: String) {
    DESCRIPTION("الوصف التفصيلي"),
    SPECIFICATIONS("المواصفات الفنية"),
    REVIEWS("التقييمات والمراجعات"),
    FAQ("أسئلة وأجوبة")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    allProducts: List<Product>,
    reviews: List<ProductReview>,
    currency: AppCurrency,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onBuyNow: (Product) -> Unit,
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit,
    onHelpfulVote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ProductDetailTab.DESCRIPTION) }

    val customersAlsoBought = remember(product, allProducts) {
        allProducts.filter { it.id != product.id }.take(3)
    }
    val similarProducts = remember(product, allProducts) {
        allProducts.filter { it.id != product.id && it.categoryId == product.categoryId }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = product.titleAr,
                        maxLines = 1,
                        fontSize = 15.sp,
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
                actions = {
                    AnimatedHeartButton(
                        isWishlisted = isWishlisted,
                        onToggle = onWishlistToggle,
                        size = 22
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkFooter)
            )
        },
        bottomBar = {
            // Sticky Buy Box at the bottom (Amazon 1-Click Buy Now & Add to Cart)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "المبلغ الإجمالي:",
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = currency.format(product.currentPriceSar),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Mint
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Add to Cart in Outlined Olive
                        OutlinedButton(
                            onClick = { onAddToCart(product) },
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(PrimaryOliveHover)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Mint)
                        ) {
                            SimpleAppIcon("shopping_cart", size = 16.dp, tint = Mint)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أضف للسلة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Buy Now in Solid PrimaryOlive
                        Button(
                            onClick = { onBuyNow(product) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryOlive,
                                contentColor = Mint
                            )
                        ) {
                            SimpleAppIcon("bolt", size = 16.dp, tint = Mint)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اشترِ الآن",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Mint
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // --- Product Visual Banner ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(DarkCard)
                        .border(1.dp, DarkBorderSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        name = product.iconKey,
                        size = AppIconSize.HERO,
                        tileColor = PrimaryOlive,
                        iconTint = Mint
                    )
                }
            }

            // --- Product Title, Badges, Price ---
            item {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (product.isOnDeal && product.discountPercentage != null) {
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
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product.titleAr,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Ratings row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { starIndex ->
                            val starColor = if (starIndex < product.rating.toInt()) AmberStar else TextMutedDark
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = starColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "${product.rating} / 5",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${product.reviewCount} تقييم موثّق)",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Price Block (LTR protected)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currency.format(product.currentPriceSar),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Mint
                            )

                            val orig = product.originalPriceSar
                            if (orig != null && orig > product.currentPriceSar) {
                                Text(
                                    text = currency.format(orig),
                                    fontSize = 14.sp,
                                    color = TextMutedDark,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Real Delivery Mechanism Box (No license bloat, genuine delivery data)
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SimpleAppIcon("local_shipping", size = 24.dp, tint = MintInstantDelivery)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "نوع التسليم: ${product.deliveryType.labelAr}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "يتم إتاحة بيانات الوصول ورابط التحميل مباشرة بعد الدفع مع زر نسخ فوري.",
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                            }
                        }
                    }
                }
            }

            // --- Content Tabs (Description, Specs, Reviews, FAQ) ---
            item {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = DarkFooter,
                    contentColor = Mint
                ) {
                    ProductDetailTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.titleAr,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == tab) Mint else TextSecondaryDark
                                )
                            }
                        )
                    }
                }
            }

            // --- Tab Body ---
            item {
                Column(modifier = Modifier.padding(14.dp)) {
                    when (selectedTab) {
                        ProductDetailTab.DESCRIPTION -> {
                            Text(
                                text = product.descriptionAr,
                                fontSize = 13.sp,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                            if (product.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("وسوم المنتج:", fontSize = 11.sp, color = TextSecondaryDark)
                                    product.tags.forEach { tag ->
                                        Surface(
                                            color = DarkCardSubtle,
                                            shape = RoundedCornerShape(4.dp),
                                            border = CardDefaults.outlinedCardBorder().copy(
                                                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                            )
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = 10.sp,
                                                color = Mint,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        ProductDetailTab.SPECIFICATIONS -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                product.specifications.forEach { (specKey, specValue) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(DarkCard, RoundedCornerShape(6.dp))
                                            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = specKey, fontSize = 12.sp, color = TextSecondaryDark, fontWeight = FontWeight.Medium)
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(text = specValue, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        ProductDetailTab.REVIEWS -> {
                            Column {
                                RatingBreakdownChart(
                                    averageRating = product.rating,
                                    totalReviews = product.reviewCount
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "مراجعات العملاء الموثقة",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                reviews.forEach { review ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    SimpleAppIcon("person", size = 16.dp, tint = Mint)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = review.authorName,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                Text(text = review.dateText, fontSize = 10.sp, color = TextMutedDark)
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                repeat(5) { i ->
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = if (i < review.rating) AmberStar else TextMutedDark,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                                if (review.isVerifiedPurchase) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = PrimaryOlive,
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "شراء موثّق ✓",
                                                            fontSize = 10.sp,
                                                            color = Mint,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = review.commentAr,
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                lineHeight = 18.sp
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Helpful Voting Button
                                            OutlinedButton(
                                                onClick = { onHelpfulVote(review.id) },
                                                shape = RoundedCornerShape(6.dp),
                                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                                    brush = androidx.compose.ui.graphics.SolidColor(PrimaryOliveHover)
                                                ),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = if (review.userVotedHelpful) Mint else TextSecondaryDark
                                                )
                                            ) {
                                                Text(
                                                    text = if (review.userVotedHelpful) "تم التصويت كـ مفيد (${review.helpfulVotes})" else "مفيد (${review.helpfulVotes})",
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ProductDetailTab.FAQ -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FaqItem(
                                    question = "كيف يتم استلام المنتج بعد إتمام الدفع؟",
                                    answer = "بمجرد إتمام الدفع، يتم نقلك مباشرة لصفحة التسليم الفوري حيث تجد رابط الوصول أو بيانات الحساب مع زر نسخ بضغطة واحدة، كما تحفظ نسخة دائمة في تبويب 'تحميلاتي'."
                                )
                                FaqItem(
                                    question = "هل تشمل المنتجات التحديثات والدعم الفني؟",
                                    answer = "نعم، كافة السورسات والخوارزميات تشمل تحديثات مستمرة ودعم فني مخصص عبر القنوات الرسمية."
                                )
                                FaqItem(
                                    question = "هل الحسابات أو البرمجيات آمنة للتداول الحقيقي؟",
                                    answer = "جميع الخوارزميات تم اختبارها على حسابات حقيقية بدون استخدام أي استراتيجيات تبريد خطرة ومجهزة بوقف خسارة صارم."
                                )
                            }
                        }
                    }
                }
            }

            // --- Customers Also Bought Shelf ---
            item {
                HorizontalProductShelf(
                    title = "عملاء اشتروا أيضاً",
                    products = customersAlsoBought,
                    currency = currency,
                    wishlistIds = emptySet(),
                    onWishlistToggle = {},
                    onAddToCart = onAddToCart,
                    onProductClick = onProductClick
                )
            }

            // --- Similar Products Shelf ---
            item {
                HorizontalProductShelf(
                    title = "منتجات مشابهة",
                    products = similarProducts,
                    currency = currency,
                    wishlistIds = emptySet(),
                    onWishlistToggle = {},
                    onAddToCart = onAddToCart,
                    onProductClick = onProductClick
                )
            }
        }
    }
}

/**
 * Rating Distribution Breakdown Chart in Olive & Mint.
 */
@Composable
fun RatingBreakdownChart(
    averageRating: Double,
    totalReviews: Int
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "${String.format("%.1f", averageRating)} / 5",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Mint
                        )
                    }
                    Text(
                        text = "بناءً على $totalReviews تقييم عميل",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberStar,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val percentages = listOf(0.85f, 0.10f, 0.03f, 0.01f, 0.01f)
            percentages.forEachIndexed { index, percent ->
                val stars = 5 - index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "$stars نجوم", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(42.dp))
                    LinearProgressIndicator(
                        progress = { percent },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MediumGreen,
                        trackColor = PrimaryOliveDark
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "${(percent * 100).toInt()}%",
                            fontSize = 10.sp,
                            color = TextSecondaryDark,
                            modifier = Modifier.width(36.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = question, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Mint)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = answer, fontSize = 11.sp, color = Color.White, lineHeight = 16.sp)
        }
    }
}
