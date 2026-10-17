package com.example.model

/**
 * Supported display currencies with real-time conversion rates against SAR (base currency).
 */
enum class AppCurrency(val code: String, val symbol: String, val rateFromSar: Double) {
    SAR("SAR", "ر.س", 1.0),
    USD("USD", "$", 0.267),
    AED("AED", "د.إ", 0.98),
    EUR("EUR", "€", 0.245);

    fun format(amountInSar: Double): String {
        val converted = amountInSar * rateFromSar
        return when (this) {
            USD, EUR -> "$symbol${String.format("%.2f", converted)}"
            SAR, AED -> "${String.format("%.2f", converted)} $symbol"
        }
    }
}

/**
 * App language support.
 */
enum class AppLanguage(val code: String, val displayName: String) {
    AR("ar", "العربية"),
    EN("en", "English")
}

/**
 * Digital asset delivery mechanism - real credentials/links, zero fake license keys.
 */
enum class DeliveryType(val labelAr: String, val labelEn: String) {
    DIRECT_LINK("رابط المستودع / المنصة", "Repository / Platform Link"),
    ACCESS_CREDENTIALS("بيانات الحساب / الاعتماد", "Account Credentials"),
    DOWNLOAD_FILE("ملف تنفيذي / سورس كود", "Binary / Source Code File")
}

/**
 * Filter tags for specialized category search.
 */
data class CategoryFilter(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val options: List<String>
)

/**
 * Category Model with distinct visual identity and custom department filters.
 */
data class Category(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val iconName: String,
    val order: Int = 1,
    val isVisible: Boolean = true,
    val filters: List<CategoryFilter> = emptyList()
)

/**
 * Product Model with Amazon-style attributes.
 */
data class Product(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val categoryId: String,
    val priceSar: Double,
    val originalPriceSar: Double? = null,
    val rating: Double = 4.8,
    val reviewCount: Int = 42,
    val isBestSeller: Boolean = false,
    val bestSellerRank: Int? = null,
    val isFeatured: Boolean = false,
    val dealDiscountPercent: Int? = null,
    val dealExpiryEpochMs: Long? = null,
    val deliveryType: DeliveryType = DeliveryType.DIRECT_LINK,
    val deliveryPayload: String = "",
    val deliveryInstructionsAr: String = "",
    val deliveryInstructionsEn: String = "",
    val shortSummaryAr: String = "",
    val shortSummaryEn: String = "",
    val descriptionAr: String = "",
    val descriptionEn: String = "",
    val specifications: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList(),
    val iconKey: String = "code",
    val attributes: Map<String, String> = emptyMap(), // firm, accountSize, techStack, botPlatform
    val createdAtEpochMs: Long = System.currentTimeMillis()
) {
    val isOnDeal: Boolean
        get() = dealDiscountPercent != null && (dealExpiryEpochMs == null || dealExpiryEpochMs > System.currentTimeMillis())

    val currentPriceSar: Double
        get() = if (isOnDeal && dealDiscountPercent != null) {
            priceSar * (1.0 - (dealDiscountPercent / 100.0))
        } else {
            priceSar
        }

    val discountPercentage: Int?
        get() {
            if (isOnDeal && dealDiscountPercent != null) return dealDiscountPercent
            if (originalPriceSar != null && originalPriceSar > priceSar) {
                return (((originalPriceSar - priceSar) / originalPriceSar) * 100).toInt()
            }
            return null
        }
}

/**
 * Customer Review Model with verified purchase flag and helpful vote count.
 */
data class ProductReview(
    val id: String,
    val productId: String,
    val authorName: String,
    val rating: Int,
    val dateText: String,
    val commentAr: String,
    val commentEn: String,
    val isVerifiedPurchase: Boolean = true,
    val helpfulVotes: Int = 0,
    val userVotedHelpful: Boolean = false
)

/**
 * Cart Item Model.
 */
data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Saved for Later Item Model.
 */
data class SavedForLaterItem(
    val product: Product,
    val savedAt: Long = System.currentTimeMillis()
)

/**
 * Order Status for tracking delivery progress.
 */
enum class OrderStatus(val titleAr: String, val titleEn: String, val stepIndex: Int) {
    CONFIRMED("تم تأكيد الطلب", "Order Confirmed", 0),
    PROCESSING("جاري تجهيز البيانات", "Processing Credentials", 1),
    DELIVERED("تم التسليم بنجاح", "Delivered Successfully", 2)
}

/**
 * Purchased Asset Delivery Payload.
 */
data class DeliveredAsset(
    val productId: String,
    val productTitle: String,
    val deliveryType: DeliveryType,
    val payload: String,
    val instructions: String
)

/**
 * Customer Order Model.
 */
data class Order(
    val id: String,
    val orderNumber: String,
    val dateEpochMs: Long = System.currentTimeMillis(),
    val items: List<CartItem>,
    val subtotalSar: Double,
    val discountSar: Double,
    val vatSar: Double,
    val totalSar: Double,
    val paymentMethodAr: String,
    val customerEmail: String,
    val customerName: String,
    val status: OrderStatus = OrderStatus.DELIVERED,
    val deliveredAssets: List<DeliveredAsset> = emptyList()
)

/**
 * Hero Banner Model for Admin Banner Management.
 */
data class PromoBanner(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val tagAr: String,
    val tagEn: String,
    val targetCategoryId: String? = null,
    val targetProductId: String? = null,
    val iconKey: String = "bolt",
    val isActive: Boolean = true
)

/**
 * Coupon Code Model.
 */
data class Coupon(
    val code: String,
    val discountPercent: Int,
    val minOrderSar: Double = 0.0,
    val descriptionAr: String,
    val descriptionEn: String
)
