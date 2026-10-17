package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

object StoreRepository {

    // --- State Holders ---
    private val _categories = MutableStateFlow<List<Category>>(initialCategories())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(initialProducts())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _banners = MutableStateFlow<List<PromoBanner>>(initialBanners())
    val banners: StateFlow<List<PromoBanner>> = _banners.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _savedForLater = MutableStateFlow<List<SavedForLaterItem>>(emptyList())
    val savedForLater: StateFlow<List<SavedForLaterItem>> = _savedForLater.asStateFlow()

    private val _wishlistIds = MutableStateFlow<Set<String>>(setOf("bot_scalper_01", "acc_ftmo_100k"))
    val wishlistIds: StateFlow<Set<String>> = _wishlistIds.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(initialOrders())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _reviews = MutableStateFlow<Map<String, List<ProductReview>>>(initialReviews())
    val reviews: StateFlow<Map<String, List<ProductReview>>> = _reviews.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(listOf("حسابات FTMO", "روبوت ذهب MT5", "Next.js", "تطبيق توصيل"))
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _recentlyViewedIds = MutableStateFlow<List<String>>(listOf("bot_scalper_01", "acc_ftmo_100k", "web_saas_01"))
    val recentlyViewedIds: StateFlow<List<String>> = _recentlyViewedIds.asStateFlow()

    private val _currency = MutableStateFlow<AppCurrency>(AppCurrency.SAR)
    val currency: StateFlow<AppCurrency> = _currency.asStateFlow()

    private val _language = MutableStateFlow<AppLanguage>(AppLanguage.AR)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _activeCoupon = MutableStateFlow<Coupon?>(null)
    val activeCoupon: StateFlow<Coupon?> = _activeCoupon.asStateFlow()

    // Valid discount coupons
    val availableCoupons = listOf(
        Coupon("AMARIX10", 10, 0.0, "خصم 10% على إجمالي السلة", "10% off total cart"),
        Coupon("AMARIX25", 25, 200.0, "خصم 25% للطلبات فوق 200 ر.س", "25% off on orders over 200 SAR"),
        Coupon("VIP007", 30, 500.0, "خصم 30% لكبار العملاء فوق 500 ر.س", "30% VIP discount on orders over 500 SAR")
    )

    // --- Currency & Language ---
    fun setCurrency(newCurrency: AppCurrency) {
        _currency.value = newCurrency
    }

    fun setLanguage(newLanguage: AppLanguage) {
        _language.value = newLanguage
    }

    // --- Cart & Saved For Later ---
    fun addToCart(product: Product, quantity: Int = 1) {
        _cart.update { current ->
            val existingIndex = current.indexOfFirst { it.product.id == product.id }
            if (existingIndex >= 0) {
                current.toMutableList().also { list ->
                    val existing = list[existingIndex]
                    list[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
                }
            } else {
                current + CartItem(product = product, quantity = quantity)
            }
        }
    }

    fun updateCartQuantity(productId: String, newQuantity: Int) {
        _cart.update { current ->
            if (newQuantity <= 0) {
                current.filterNot { it.product.id == productId }
            } else {
                current.map {
                    if (it.product.id == productId) it.copy(quantity = newQuantity) else it
                }
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cart.update { current -> current.filterNot { it.product.id == productId } }
    }

    fun moveToSavedForLater(productId: String) {
        val item = _cart.value.find { it.product.id == productId } ?: return
        removeFromCart(productId)
        _savedForLater.update { current ->
            if (current.none { it.product.id == productId }) {
                current + SavedForLaterItem(product = item.product)
            } else current
        }
    }

    fun moveToCartFromSaved(productId: String) {
        val item = _savedForLater.value.find { it.product.id == productId } ?: return
        _savedForLater.update { current -> current.filterNot { it.product.id == productId } }
        addToCart(item.product, 1)
    }

    fun removeSavedForLater(productId: String) {
        _savedForLater.update { current -> current.filterNot { it.product.id == productId } }
    }

    fun applyCoupon(code: String): Pair<Boolean, String> {
        val cleanCode = code.trim().uppercase()
        val found = availableCoupons.find { it.code == cleanCode }
        return if (found != null) {
            val subtotal = calculateSubtotalSar()
            if (subtotal >= found.minOrderSar) {
                _activeCoupon.value = found
                Pair(true, "تم تطبيق الكوبون بنجاح (${found.discountPercent}% خصم)")
            } else {
                Pair(false, "الحد الأدنى لتفعيل هذا الكوبون هو ${found.minOrderSar} ر.س")
            }
        } else {
            Pair(false, "رمز الكوبون غير صحيح")
        }
    }

    fun removeCoupon() {
        _activeCoupon.value = null
    }

    fun calculateSubtotalSar(): Double {
        return _cart.value.sumOf { it.product.currentPriceSar * it.quantity }
    }

    fun calculateDiscountSar(): Double {
        val coupon = _activeCoupon.value ?: return 0.0
        val subtotal = calculateSubtotalSar()
        return subtotal * (coupon.discountPercent / 100.0)
    }

    fun calculateVatSar(): Double {
        // Digital delivery standard 15% VAT on discounted subtotal
        val taxableAmount = calculateSubtotalSar() - calculateDiscountSar()
        return taxableAmount * 0.15
    }

    fun calculateTotalSar(): Double {
        val subtotal = calculateSubtotalSar()
        val discount = calculateDiscountSar()
        val vat = calculateVatSar()
        return (subtotal - discount + vat).coerceAtLeast(0.0)
    }

    // --- Wishlist ---
    fun toggleWishlist(productId: String): Boolean {
        var added = false
        _wishlistIds.update { current ->
            if (current.contains(productId)) {
                current - productId
            } else {
                added = true
                current + productId
            }
        }
        return added
    }

    fun isWishlisted(productId: String): Boolean {
        return _wishlistIds.value.contains(productId)
    }

    // --- Browsing History ---
    fun recordProductView(productId: String) {
        _recentlyViewedIds.update { current ->
            (listOf(productId) + current.filterNot { it == productId }).take(10)
        }
    }

    // --- Search History ---
    fun addSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        _searchHistory.update { current ->
            (listOf(trimmed) + current.filterNot { it.equals(trimmed, ignoreCase = true) }).take(10)
        }
    }

    fun removeSearchQuery(query: String) {
        _searchHistory.update { current -> current.filterNot { it == query } }
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    // --- Checkout & Orders ---
    fun placeOrder(
        customerName: String,
        customerEmail: String,
        paymentMethodAr: String
    ): Order {
        val currentCart = _cart.value
        val subtotal = calculateSubtotalSar()
        val discount = calculateDiscountSar()
        val vat = calculateVatSar()
        val total = calculateTotalSar()

        val deliveredAssets = currentCart.map { item ->
            DeliveredAsset(
                productId = item.product.id,
                productTitle = item.product.titleAr,
                deliveryType = item.product.deliveryType,
                payload = item.product.deliveryPayload,
                instructions = item.product.deliveryInstructionsAr
            )
        }

        val orderNumber = "AMX-${(100000..999999).random()}"
        val newOrder = Order(
            id = UUID.randomUUID().toString(),
            orderNumber = orderNumber,
            dateEpochMs = System.currentTimeMillis(),
            items = currentCart,
            subtotalSar = subtotal,
            discountSar = discount,
            vatSar = vat,
            totalSar = total,
            paymentMethodAr = paymentMethodAr,
            customerEmail = customerEmail,
            customerName = customerName,
            status = OrderStatus.DELIVERED,
            deliveredAssets = deliveredAssets
        )

        _orders.update { listOf(newOrder) + it }
        _cart.value = emptyList()
        _activeCoupon.value = null
        return newOrder
    }

    // --- Reviews Helpful Voting ---
    fun toggleReviewHelpful(productId: String, reviewId: String) {
        _reviews.update { map ->
            val list = map[productId] ?: return@update map
            val updatedList = list.map { r ->
                if (r.id == reviewId) {
                    val voted = !r.userVotedHelpful
                    val votes = if (voted) r.helpfulVotes + 1 else (r.helpfulVotes - 1).coerceAtLeast(0)
                    r.copy(helpfulVotes = votes, userVotedHelpful = voted)
                } else r
            }
            map + (productId to updatedList)
        }
    }

    // --- Admin Operations ---
    fun updateProductDeal(productId: String, discountPercent: Int?, durationHours: Int?) {
        val expiryMs = if (durationHours != null && durationHours > 0) {
            System.currentTimeMillis() + (durationHours * 3600 * 1000L)
        } else null

        _products.update { list ->
            list.map { p ->
                if (p.id == productId) {
                    p.copy(
                        dealDiscountPercent = discountPercent,
                        dealExpiryEpochMs = expiryMs
                    )
                } else p
            }
        }
    }

    fun toggleProductBestSeller(productId: String) {
        _products.update { list ->
            list.map { p ->
                if (p.id == productId) {
                    val nextState = !p.isBestSeller
                    p.copy(
                        isBestSeller = nextState,
                        bestSellerRank = if (nextState) (1..5).random() else null
                    )
                } else p
            }
        }
    }

    fun toggleProductFeatured(productId: String) {
        _products.update { list ->
            list.map { p ->
                if (p.id == productId) p.copy(isFeatured = !p.isFeatured) else p
            }
        }
    }

    fun updateProductPriceAndDelivery(
        productId: String,
        newPrice: Double,
        deliveryPayload: String,
        instructions: String
    ) {
        _products.update { list ->
            list.map { p ->
                if (p.id == productId) {
                    p.copy(
                        priceSar = newPrice,
                        deliveryPayload = deliveryPayload,
                        deliveryInstructionsAr = instructions
                    )
                } else p
            }
        }
    }

    fun updateCategory(category: Category) {
        _categories.update { list ->
            val exists = list.any { it.id == category.id }
            if (exists) {
                list.map { if (it.id == category.id) category else it }
            } else {
                list + category
            }
        }
    }

    fun updateBanner(banner: PromoBanner) {
        _banners.update { list ->
            val exists = list.any { it.id == banner.id }
            if (exists) {
                list.map { if (it.id == banner.id) banner else it }
            } else {
                list + banner
            }
        }
    }

    // --- Seed Data ---
    private fun initialCategories(): List<Category> {
        return listOf(
            Category(
                id = "accounts",
                nameAr = "حسابات التداول",
                nameEn = "Trading Accounts",
                descriptionAr = "حسابات تحديات ممولة من كبرى شركات التمويل العالمية مع تسليم مباشر",
                descriptionEn = "Funded trading challenge accounts from top prop firms with instant credentials",
                iconName = "account_balance",
                order = 1,
                isVisible = true,
                filters = listOf(
                    CategoryFilter("size", "حجم الحساب", "Account Size", listOf("$10k", "$25k", "$50k", "$100k", "$200k")),
                    CategoryFilter("firm", "شركة التمويل", "Prop Firm", listOf("FTMO", "TheFundedTrader", "FundedNext", "Alpha Capital")),
                    CategoryFilter("stage", "مرحلة التقييم", "Stage", listOf("مرحلة واحدة", "مرحلتين", "ممولة فوراً"))
                )
            ),
            Category(
                id = "websites",
                nameAr = "مواقع ومنصات",
                nameEn = "Websites & Platforms",
                descriptionAr = "منصات ويب متكاملة، حلول SaaS، ومتاجر إلكترونية جاهزة للإطلاق",
                descriptionEn = "Turnkey web platforms, SaaS starters, and ready-to-launch online stores",
                iconName = "language",
                order = 2,
                isVisible = true,
                filters = listOf(
                    CategoryFilter("field", "مجال المنصة", "Industry", listOf("تجارة إلكترونية", "SaaS اشتراكات", "بورتفوليو ووكالات", "حجوزات واستشارات")),
                    CategoryFilter("backend", "قاعدة البيانات", "Backend", listOf("PostgreSQL", "Supabase", "Firebase", "MongoDB"))
                )
            ),
            Category(
                id = "apps",
                nameAr = "تطبيقات وبرمجيات",
                nameEn = "Apps & Software",
                descriptionAr = "تطبيقات جوال ولوحات تحكم إدارية حديثة بسورس كود نظيف وقابل للتطوير",
                descriptionEn = "Mobile apps and modern admin dashboards with clean, scalable source code",
                iconName = "smartphone",
                order = 3,
                isVisible = true,
                filters = listOf(
                    CategoryFilter("tech", "التقنية المستخدمة", "Tech Stack", listOf("Flutter", "Jetpack Compose", "React Native", "Next.js")),
                    CategoryFilter("category", "نوع التطبيق", "App Type", listOf("توصيل ومتاجر", "محفظة مالية", "لوحة تحكم CRM", "دردشة ومراسلة"))
                )
            ),
            Category(
                id = "bots",
                nameAr = "روبوتات وخوارزميات",
                nameEn = "Trading Bots & Algos",
                descriptionAr = "خوارزميات تداول آلية مدروسة ومختبرة لمنصات MT4 وMT5 وTradingView",
                descriptionEn = "Automated algorithmic trading bots for MT4, MT5, and TradingView",
                iconName = "smart_toy",
                order = 4,
                isVisible = true,
                filters = listOf(
                    CategoryFilter("platform", "المنصة المستهدفة", "Platform", listOf("MetaTrader 5", "MetaTrader 4", "TradingView Pine", "cTrader")),
                    CategoryFilter("market", "السوق", "Market", listOf("الذهب XAUUSD", "المؤشرات US30/NAS100", "العملات Forex", "الكريبتو Crypto"))
                )
            )
        )
    }

    private fun initialProducts(): List<Product> {
        val now = System.currentTimeMillis()
        val dealExpiry = now + (7 * 3600 * 1000L) + (43 * 60 * 1000L) // 7h 43m remaining

        return listOf(
            Product(
                id = "acc_ftmo_100k",
                titleAr = "حساب تحدي FTMO الرسمي بقيمة $100,000 (مرحلتين)",
                titleEn = "Official FTMO Challenge Account $100K (2-Step)",
                categoryId = "accounts",
                priceSar = 1950.0,
                originalPriceSar = 2450.0,
                rating = 4.9,
                reviewCount = 184,
                isBestSeller = true,
                bestSellerRank = 1,
                isFeatured = true,
                dealDiscountPercent = 20,
                dealExpiryEpochMs = dealExpiry,
                deliveryType = DeliveryType.ACCESS_CREDENTIALS,
                deliveryPayload = "الخادم: FTMO-Server-Live\nرقم الحساب: 8840192\nكلمة مرور المتداول: Amx#Tr@de2026\nرابط لوحة العميل: https://trader.ftmo.com/portal",
                deliveryInstructionsAr = "انسخ بيانات الحساب وسجّل الدخول إلى منصة MT5 مباشرة. الحساب مفعل ومسجل بريدك الإلكتروني.",
                deliveryInstructionsEn = "Copy credentials and log in to MT5. Your challenge begins on the first executed trade.",
                shortSummaryAr = "أكبر شركة تمويل عالمية مع سبريد منخفض وسحب أرباح سريع كل 14 يوماً.",
                shortSummaryEn = "Top global prop firm with tight spreads and bi-weekly profit splits up to 90%.",
                descriptionAr = "حساب تحدي رسمي بقيمة 100 ألف دولار من شركة FTMO الرائدة عالمياً. يتيح لك التداول على العملات والذهب والمؤشرات وفق قواعد واضحة ومدروسة. يشمل شروط مرنة وسبريد فائق الانخفاض وإمكانية التداول أثناء الأخبار.",
                descriptionEn = "100K FTMO Evaluation account with standard prop trading rules. Trade Forex, Gold, and Indices with premium liquidity and up to 90% profit split.",
                specifications = mapOf(
                    "حجم الحساب" to "$100,000",
                    "الشركة" to "FTMO",
                    "المنصة" to "MetaTrader 5",
                    "الحد الأقصى للخسارة الكلية" to "10% ($10,000)",
                    "الحد الأقصى للخسارة اليومية" to "5% ($5,000)",
                    "هدف المرحلة الأولى" to "10%",
                    "هدف المرحلة الثانية" to "5%",
                    "تقاسم الأرباح" to "80% - 90%"
                ),
                tags = listOf("FTMO", "$100k", "مرحلتين", "تسليم فوري"),
                iconKey = "account_balance",
                attributes = mapOf("size" to "$100k", "firm" to "FTMO", "stage" to "مرحلتين")
            ),
            Product(
                id = "acc_funded_next_200k",
                titleAr = "حساب ممول FundedNext Stellar بقيمة $200,000 بدون قيود زمنية",
                titleEn = "FundedNext Stellar 2-Step $200K Account - No Time Limit",
                categoryId = "accounts",
                priceSar = 3690.0,
                originalPriceSar = 4200.0,
                rating = 4.8,
                reviewCount = 96,
                isBestSeller = false,
                isFeatured = true,
                dealDiscountPercent = null,
                deliveryType = DeliveryType.ACCESS_CREDENTIALS,
                deliveryPayload = "الخادم: FundedNext-Server-04\nرقم الحساب: 7719402\nكلمة مرور المتداول: FNext#Stellar88\nلوحة التحكم: https://app.fundednext.com",
                deliveryInstructionsAr = "سجّل الدخول إلى منصة MT5 أو cTrader عبر بيانات الاعتماد المرفقة وابدأ التداول بدون أي ضغط زمني.",
                deliveryInstructionsEn = "Log in via MT5 or cTrader with provided credentials. No minimum or maximum trading days required.",
                shortSummaryAr = "بدون حد زمني لإكمال التحدي ومشاركة 15% من أرباح فترة التقييم.",
                shortSummaryEn = "No time limits with 15% profit sharing during the evaluation phase.",
                descriptionAr = "حساب ضخم بقيمة 200 ألف دولار يوفر لك حرية التداول وإدارة المخاطر دون قلق من انقضاء الأيام. استلم بيانات الدخول فوراً برابط رسمي معتمد.",
                descriptionEn = "Comprehensive 200K evaluation account with zero time restriction and instant setup.",
                specifications = mapOf(
                    "حجم الحساب" to "$200,000",
                    "الشركة" to "FundedNext",
                    "المنصة" to "MT5 / cTrader",
                    "الحد الزمني" to "غير محدود",
                    "تقاسم الأرباح" to "حتى 95%"
                ),
                tags = listOf("FundedNext", "$200k", "بدون وقت"),
                iconKey = "account_balance",
                attributes = mapOf("size" to "$200k", "firm" to "FundedNext", "stage" to "مرحلتين")
            ),
            Product(
                id = "bot_scalper_01",
                titleAr = "روبوت التداول الآلي AMARIX Trend Scalper Pro (MT5)",
                titleEn = "AMARIX Trend Scalper Pro Trading Bot (MT5)",
                categoryId = "bots",
                priceSar = 890.0,
                originalPriceSar = 1450.0,
                rating = 4.95,
                reviewCount = 247,
                isBestSeller = true,
                bestSellerRank = 2,
                isFeatured = true,
                dealDiscountPercent = 38,
                dealExpiryEpochMs = dealExpiry,
                deliveryType = DeliveryType.DOWNLOAD_FILE,
                deliveryPayload = "https://vault.amarix007.com/downloads/amarix-trend-scalper-v4.8.ex5?token=AMX-EX5-99812",
                deliveryInstructionsAr = "حمّل ملف .ex5 وضعه في مجلد Experts بمنصة MT5. يأتي مع ملف إعدادات Setfiles جاهزة للذهب XAUUSD وفريم الدقيقة والـ 5 دقائق.",
                deliveryInstructionsEn = "Download the .ex5 file and place in MQL5/Experts. Pre-configured setfiles for XAUUSD included.",
                shortSummaryAr = "خوارزمية ذكية تتداول السيولة والانعكاسات على الذهب والنازداك بدون مضاعفات خطيرة.",
                shortSummaryEn = "Dynamic scalping algo for Gold & Indices with hard stop loss and strict risk control.",
                descriptionAr = "خوارزمية تداول احترافية مصممة خصيصاً لاجتياز حسابات التمويل (FTMO / FundedNext) والتداول في الحسابات الحقيقية. تعتمد على مناطق السيولة ونماذج الفيبوناتشي والزخم مع وقف خسارة ثابت لكل صفقة دون أي استخدام لشبكات التبريد أو المضاعفات الخطرة.",
                descriptionEn = "Engineered specifically for passing prop firm evaluations and safe live execution. Employs ATR trailing stop and liquidity sweep triggers.",
                specifications = mapOf(
                    "المنصة" to "MetaTrader 5 (MT5)",
                    "الأزواج المدعومة" to "XAUUSD (الذهب), US30, NAS100, EURUSD",
                    "الفريم الزمني" to "M1, M5, M15",
                    "إدارة المخاطر" to "Stop Loss إلزامي لكل صفقة",
                    "نوع الترخيص" to "تسليم كود تنفيذي مباشر مع تحديثات مدى الحياة"
                ),
                tags = listOf("روبوت", "MT5", "ذهب", "سكالبينج"),
                iconKey = "smart_toy",
                attributes = mapOf("platform" to "MetaTrader 5", "market" to "الذهب XAUUSD")
            ),
            Product(
                id = "bot_gold_hunter_02",
                titleAr = "خوارزمية صائد الذهب الذكية Gold Hunter Grid Algo (MT4/MT5)",
                titleEn = "Gold Hunter Smart Algo for MT4 & MT5",
                categoryId = "bots",
                priceSar = 650.0,
                originalPriceSar = 920.0,
                rating = 4.7,
                reviewCount = 89,
                isBestSeller = false,
                isFeatured = false,
                dealDiscountPercent = 25,
                dealExpiryEpochMs = dealExpiry,
                deliveryType = DeliveryType.DOWNLOAD_FILE,
                deliveryPayload = "https://vault.amarix007.com/downloads/gold-hunter-bundle-v3.zip?token=AMX-GH3-771",
                deliveryInstructionsAr = "الملف المضغوط يحتوي نسختي MT4 وMT5 بالإضافة إلى فيديو شرح طريقة التركيب وضبط إدارة رأس المال.",
                deliveryInstructionsEn = "Zip package includes MT4 & MT5 builds plus full installation video guide.",
                shortSummaryAr = "اقتناص رالي الذهب في الفترتين الأوروبية والأمريكية مع مؤشر قوة الاتجاه.",
                shortSummaryEn = "Captures Gold momentum during London and New York sessions with trend filtering.",
                descriptionAr = "نظام برمجي متقدم للتداول على معدن الذهب. يحدد مناطق دخول السيولة العالية ويتتبع الاتجاه بدقة متناهية.",
                descriptionEn = "Advanced Gold algorithm detecting institutional order flow with automated profit taking.",
                specifications = mapOf(
                    "المنصة" to "MT4 & MT5",
                    "السوق" to "الذهب XAUUSD",
                    "الرافعة المالية" to "1:30 فما فوق"
                ),
                tags = listOf("MT4", "MT5", "ذهب"),
                iconKey = "smart_toy",
                attributes = mapOf("platform" to "MetaTrader 5", "market" to "الذهب XAUUSD")
            ),
            Product(
                id = "web_saas_01",
                titleAr = "منصة SaaS سحابية متكاملة (Next.js 15 + Stripe + Supabase)",
                titleEn = "Complete Turnkey SaaS Platform (Next.js 15 + Stripe + Supabase)",
                categoryId = "websites",
                priceSar = 1450.0,
                originalPriceSar = 2100.0,
                rating = 4.9,
                reviewCount = 112,
                isBestSeller = true,
                bestSellerRank = 3,
                isFeatured = true,
                dealDiscountPercent = 30,
                dealExpiryEpochMs = dealExpiry,
                deliveryType = DeliveryType.DIRECT_LINK,
                deliveryPayload = "https://github.com/amarix007-templates/saas-enterprise-starter\nرمز الوصول السري: ghp_AmxSaaS902187TokenAccessPro\nدليل الإعداد السريع: https://docs.amarix007.com/saas-starter",
                deliveryInstructionsAr = "تمت إضافة صلاحيات الوصول إلى مستودع GitHub فوراً. يمكنك استنساخ المشروع وربط حساب Stripe وSupabase بضغطة زر.",
                deliveryInstructionsEn = "Full GitHub repo access granted. Follow the quickstart guide to configure Stripe & Supabase env variables.",
                shortSummaryAr = "سورس كود كامل لمنصة اشتراكات جاهزة للإطلاق التجاري مع نظام دفع وأمان مدمج.",
                shortSummaryEn = "Production-grade SaaS boilerplate with multi-tenancy, Stripe billing, and user auth.",
                descriptionAr = "قالب برمجيات كخدمة (SaaS) احترافي مبني بأحدث معايير الويب (Next.js 15 App Router, TypeScript, Tailwind CSS, Supabase SSR Auth, Stripe Subscriptions & Webhooks). يحتوي على لوحة تحكم المستخدم، إدارة الخطط، والفواتير الضريبية.",
                descriptionEn = "Enterprise SaaS codebase engineered with modern best practices. Includes landing page, authentication, dark mode, Stripe billing, and database migrations.",
                specifications = mapOf(
                    "الواجهة الأمامية" to "Next.js 15, React 19, Tailwind CSS",
                    "قاعدة البيانات" to "Supabase PostgreSQL",
                    "نظام الدفع" to "Stripe Checkout & Customer Portal",
                    "المصادقة" to "Supabase Auth (Google, Email Magic Link)",
                    "حقوق الاستخدام" to "سورس كود مفتوح غير محدود المشاريع التجارية"
                ),
                tags = listOf("Next.js", "SaaS", "Stripe", "Supabase"),
                iconKey = "language",
                attributes = mapOf("field" to "SaaS اشتراكات", "backend" to "Supabase")
            ),
            Product(
                id = "web_ecommerce_02",
                titleAr = "متجر إلكتروني عالي الأداء مع لوحة إدارة وسلة فورية",
                titleEn = "High-Performance Headless E-Commerce Store & Admin",
                categoryId = "websites",
                priceSar = 1190.0,
                originalPriceSar = 1600.0,
                rating = 4.85,
                reviewCount = 74,
                isBestSeller = false,
                isFeatured = true,
                dealDiscountPercent = null,
                deliveryType = DeliveryType.DIRECT_LINK,
                deliveryPayload = "https://github.com/amarix007-templates/headless-commerce-pro\nالمستودع: مرخص بالكامل مع ملفات Docker وDocker Compose الجاهزة",
                deliveryInstructionsAr = "سورس كود المتجر ولوحة التحكم متاح للاستنساخ عبر Git. يشمل شروحات بالفيديو لربط بوابات الدفع المحلية.",
                deliveryInstructionsEn = "Access git repository to clone full headless storefront and modern inventory admin portal.",
                shortSummaryAr = "متجر فائق السرعة يدعم العربية والإنجليزية وبوابات الدفع مع لوحة تحكم شاملة.",
                shortSummaryEn = "Ultra-fast headless commerce supporting multi-currency, RTL Arabic, and instant checkout.",
                descriptionAr = "حل تجاري متكامل للمتاجر الرقمية والفيزيائية. متوافق مع محركات البحث ومجهز بالكامل بتصميم متجاوب وتجربة دفع سلسة.",
                descriptionEn = "Production-ready headless commerce solution with responsive M3 design and payment integrations.",
                specifications = mapOf(
                    "التقنيات" to "React, Vite, Tailwind CSS, Node.js",
                    "بوابات الدفع" to "مدى، فيزا، Apple Pay، تحويل بنكي",
                    "اللغات" to "عربي RTL + إنجليزي LTR"
                ),
                tags = listOf("متجر", "تجارة إلكترونية", "لوحة تحكم"),
                iconKey = "language",
                attributes = mapOf("field" to "تجارة إلكترونية", "backend" to "PostgreSQL")
            ),
            Product(
                id = "app_delivery_flutter_01",
                titleAr = "تطبيق توصيل طلبات ومتاجر متكامل (Flutter + Node.js Admin)",
                titleEn = "Turnkey Delivery & Multi-Vendor App (Flutter + Backend)",
                categoryId = "apps",
                priceSar = 2100.0,
                originalPriceSar = 2950.0,
                rating = 4.88,
                reviewCount = 63,
                isBestSeller = false,
                isFeatured = true,
                dealDiscountPercent = 25,
                dealExpiryEpochMs = dealExpiry,
                deliveryType = DeliveryType.DIRECT_LINK,
                deliveryPayload = "https://github.com/amarix007-templates/delivery-flutter-ecosystem\nالمستودع يشمل: تطبيق العميل + تطبيق السائق + لوحة تحكم المتجر",
                deliveryInstructionsAr = "تطبيقات Flutter لنظامي Android وiOS مع السيرفر الخلفي وقواعد البيانات. سورس كود نظيف بهيكلية Clean Architecture.",
                deliveryInstructionsEn = "Includes Customer App, Driver App, and Web Admin dashboard with full documentation.",
                shortSummaryAr = "نظام توصيل طلبات كامل مع تتبع حي للخرائط وتطبيقات للسائق والعميل والمتجر.",
                shortSummaryEn = "Full delivery ecosystem with real-time GPS tracking and 3 interconnected applications.",
                descriptionAr = "منظومة توصيل طلبات متكاملة تشمل تطبيق العميل للشراء وتطبيق السائق لاستلام الطلبات وتتبع المسار عبر خرائط Google، مع لوحة ويب لإدارة الطلبات والمدفوعات.",
                descriptionEn = "Complete multi-vendor delivery mobile app with real-time driver tracking and push notifications.",
                specifications = mapOf(
                    "إطار العمل" to "Flutter 3.x (Android & iOS)",
                    "الخادم الخلفي" to "Node.js Express & Socket.io",
                    "التتبع" to "Google Maps Navigation API",
                    "الإشعارات" to "Firebase Cloud Messaging"
                ),
                tags = listOf("Flutter", "توصيل", "تطبيقات"),
                iconKey = "smartphone",
                attributes = mapOf("tech" to "Flutter", "category" to "توصيل ومتاجر")
            ),
            Product(
                id = "app_crypto_wallet_02",
                titleAr = "سورس كود تطبيق محفظة رقمية وتداول (Jetpack Compose)",
                titleEn = "Crypto & Forex Wallet App (Native Jetpack Compose)",
                categoryId = "apps",
                priceSar = 1350.0,
                originalPriceSar = 1800.0,
                rating = 4.92,
                reviewCount = 51,
                isBestSeller = false,
                isFeatured = false,
                dealDiscountPercent = null,
                deliveryType = DeliveryType.DIRECT_LINK,
                deliveryPayload = "https://github.com/amarix007-templates/compose-crypto-wallet-pro\nسورس كود Kotlin أصيل مع اختبارات آلية ومكتبات رسمية",
                deliveryInstructionsAr = "افتح المشروع مباشرة في Android Studio. جاهز للبناء مع تكامل واجهات CoinGecko وBinance العامة.",
                deliveryInstructionsEn = "Open directly in Android Studio. Pre-configured with CoinGecko and Binance market APIs.",
                shortSummaryAr = "واجهات أندرويد أصلية حديثة بتقنية Jetpack Compose M3 ورسوم بيانية تفاعلية.",
                shortSummaryEn = "Modern native Android Compose architecture with live candle charts and biometric login.",
                descriptionAr = "تطبيق أندرويد نيتف احترافي لإدارة الأصول المالية والعملات الرقمية ومتابعة الأسعار مباشرة. أمان عالي مع البصمة وتشفير المفاتيح.",
                descriptionEn = "Native Android application built exclusively with Jetpack Compose, Coroutines, and MVVM.",
                specifications = mapOf(
                    "اللغة" to "Kotlin 2.x & Jetpack Compose M3",
                    "المعمارية" to "MVVM + Coroutines Flow",
                    "الأمان" to "BiometricPrompt & EncryptedSharedPreferences"
                ),
                tags = listOf("Compose", "محفظة", "Kotlin"),
                iconKey = "smartphone",
                attributes = mapOf("tech" to "Jetpack Compose", "category" to "محفظة مالية")
            )
        )
    }

    private fun initialBanners(): List<PromoBanner> {
        return listOf(
            PromoBanner(
                id = "banner_deals",
                titleAr = "عروض التوفير الكبرى من AMARIX007",
                titleEn = "AMARIX007 Mega Savings Deals",
                subtitleAr = "خصومات حقيقية تصل إلى 38% على خوارزميات التداول وحسابات التحدي",
                subtitleEn = "Up to 38% real discounts on trading bots and evaluation accounts",
                tagAr = "لفترة محدودة",
                tagEn = "Limited Time",
                targetCategoryId = "bots",
                iconKey = "bolt"
            ),
            PromoBanner(
                id = "banner_accounts",
                titleAr = "حسابات تمويل فورية رسمية معتمدة",
                titleEn = "Official Certified Prop Firm Accounts",
                subtitleAr = "استلم بيانات حسابك فوراً على منصة MT5 وابدأ مسيرتك الاحترافية",
                subtitleEn = "Instant MT5 credentials delivery with up to 90% profit splits",
                tagAr = "تسليم مباشر",
                tagEn = "Instant Delivery",
                targetCategoryId = "accounts",
                iconKey = "account_balance"
            ),
            PromoBanner(
                id = "banner_saas",
                titleAr = "أطلق مشروعك الرقمي القادم في ساعات",
                titleEn = "Launch Your Next Tech Venture Today",
                subtitleAr = "قوالب SaaS ومنصات ويب متكاملة مع سورس كود نظيف وتوثيق هندسي",
                subtitleEn = "Production-grade SaaS boilerplates with Next.js & Stripe",
                tagAr = "كود نقي 100%",
                tagEn = "100% Clean Code",
                targetCategoryId = "websites",
                iconKey = "language"
            )
        )
    }

    private fun initialReviews(): Map<String, List<ProductReview>> {
        return mapOf(
            "acc_ftmo_100k" to listOf(
                ProductReview(
                    id = "rev_ftmo_1",
                    productId = "acc_ftmo_100k",
                    authorName = "م. خالد العتيبي",
                    rating = 5,
                    dateText = "قبل 3 أيام",
                    commentAr = "تسليم فوري ومباشر! نسخت بيانات الحساب ودخلت MT5 في نفس اللحظة بدون أي تعقيد. خدمة عملاء راقية.",
                    commentEn = "Instant delivery! Credentials worked on MT5 immediately.",
                    isVerifiedPurchase = true,
                    helpfulVotes = 24
                ),
                ProductReview(
                    id = "rev_ftmo_2",
                    productId = "acc_ftmo_100k",
                    authorName = "سارة الشمري",
                    rating = 5,
                    dateText = "قبل أسبوع",
                    commentAr = "السبريد ممتاز والقواعد واضحة تماماً. أشكر متجر AMARIX007 على الصدق والاحترافية.",
                    commentEn = "Spreads are excellent and rules are completely transparent.",
                    isVerifiedPurchase = true,
                    helpfulVotes = 15
                )
            ),
            "bot_scalper_01" to listOf(
                ProductReview(
                    id = "rev_bot_1",
                    productId = "bot_scalper_01",
                    authorName = "فهد الدوسري",
                    rating = 5,
                    dateText = "أمس",
                    commentAr = "الخوارزمية ممتازة جداً على فريم الدقيقة للذهب، التزمت بملف الإعدادات المرفق وحققت نتائج رائعة بدون دروداون عالي.",
                    commentEn = "Superb M1 gold scalping algorithm. Followed setfiles with minimal drawdown.",
                    isVerifiedPurchase = true,
                    helpfulVotes = 42
                ),
                ProductReview(
                    id = "rev_bot_2",
                    productId = "bot_scalper_01",
                    authorName = "طارق المرزوقي",
                    rating = 5,
                    dateText = "قبل 5 أيام",
                    commentAr = "أفضل ما في الروبوت أنه لا يستخدم Martingale إطلاقاً، كل صفقة لها وقف خسارة وهدف محدد.",
                    commentEn = "Best feature is zero martingale. Fixed stop loss on every single trade.",
                    isVerifiedPurchase = true,
                    helpfulVotes = 29
                )
            ),
            "web_saas_01" to listOf(
                ProductReview(
                    id = "rev_saas_1",
                    productId = "web_saas_01",
                    authorName = "عمر باقيس",
                    rating = 5,
                    dateText = "قبل يومين",
                    commentAr = "سورس كود نظيف جداً وتوثيق خطوة بخطوة. وفّر علي أكثر من شهرين من العمل البرمجي.",
                    commentEn = "Clean code and step-by-step documentation. Saved me months of development.",
                    isVerifiedPurchase = true,
                    helpfulVotes = 18
                )
            )
        )
    }

    private fun initialOrders(): List<Order> {
        val sampleItem = CartItem(
            product = initialProducts()[2], // bot_scalper_01
            quantity = 1
        )
        return listOf(
            Order(
                id = "order_sample_01",
                orderNumber = "AMX-892104",
                dateEpochMs = System.currentTimeMillis() - (86400000L * 2), // 2 days ago
                items = listOf(sampleItem),
                subtotalSar = 890.0,
                discountSar = 0.0,
                vatSar = 133.5,
                totalSar = 1023.5,
                paymentMethodAr = "مدى / بطاقة ائتمانية",
                customerEmail = "customer@amarix007.com",
                customerName = "عبدالرحمن الشريف",
                status = OrderStatus.DELIVERED,
                deliveredAssets = listOf(
                    DeliveredAsset(
                        productId = sampleItem.product.id,
                        productTitle = sampleItem.product.titleAr,
                        deliveryType = sampleItem.product.deliveryType,
                        payload = sampleItem.product.deliveryPayload,
                        instructions = sampleItem.product.deliveryInstructionsAr
                    )
                )
            )
        )
    }
}
