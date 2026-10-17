package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.StoreRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.DarkBg
import com.example.ui.theme.MyApplicationTheme

sealed class AppScreen {
    object Home : AppScreen()
    data class CategoryView(val categoryId: String? = null) : AppScreen()
    data class ProductDetail(val product: Product) : AppScreen()
    object Cart : AppScreen()
    object Checkout : AppScreen()
    data class OrderSuccess(val order: Order) : AppScreen()
    object MyOrders : AppScreen()
    object Wishlist : AppScreen()
    object Admin : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val context = LocalContext.current

    // Collect Reactive State from StoreRepository
    val categories by StoreRepository.categories.collectAsStateWithLifecycle()
    val products by StoreRepository.products.collectAsStateWithLifecycle()
    val banners by StoreRepository.banners.collectAsStateWithLifecycle()
    val cartItems by StoreRepository.cart.collectAsStateWithLifecycle()
    val savedItems by StoreRepository.savedForLater.collectAsStateWithLifecycle()
    val wishlistIds by StoreRepository.wishlistIds.collectAsStateWithLifecycle()
    val orders by StoreRepository.orders.collectAsStateWithLifecycle()
    val reviewsMap by StoreRepository.reviews.collectAsStateWithLifecycle()
    val searchHistory by StoreRepository.searchHistory.collectAsStateWithLifecycle()
    val recentlyViewedIds by StoreRepository.recentlyViewedIds.collectAsStateWithLifecycle()
    val currency by StoreRepository.currency.collectAsStateWithLifecycle()
    val language by StoreRepository.language.collectAsStateWithLifecycle()
    val activeCoupon by StoreRepository.activeCoupon.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIdInHeader by remember { mutableStateOf<String?>(null) }

    val layoutDirection = if (language == AppLanguage.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Handle Back Press gracefully
    BackHandler(enabled = currentScreen !is AppScreen.Home) {
        when (currentScreen) {
            is AppScreen.ProductDetail,
            is AppScreen.Checkout,
            is AppScreen.OrderSuccess,
            is AppScreen.Admin -> {
                currentScreen = AppScreen.Home
                currentTab = NavigationTab.HOME
            }
            else -> {
                currentScreen = AppScreen.Home
                currentTab = NavigationTab.HOME
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBg,
            topBar = {
                // Show Amazon Header on Home, CategoryView, Wishlist
                if (currentScreen is AppScreen.Home || currentScreen is AppScreen.CategoryView || currentScreen is AppScreen.Wishlist) {
                    AmazonHeader(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onSearchSubmit = { query ->
                            StoreRepository.addSearchQuery(query)
                            currentScreen = AppScreen.CategoryView(selectedCategoryIdInHeader)
                            currentTab = NavigationTab.CATEGORIES
                        },
                        categories = categories,
                        selectedCategoryId = selectedCategoryIdInHeader,
                        onCategorySelect = { catId ->
                            selectedCategoryIdInHeader = catId
                            currentScreen = AppScreen.CategoryView(catId)
                            currentTab = NavigationTab.CATEGORIES
                        },
                        searchHistory = searchHistory,
                        onRemoveSearchHistory = { StoreRepository.removeSearchQuery(it) },
                        onClearSearchHistory = { StoreRepository.clearSearchHistory() },
                        currentCurrency = currency,
                        onCurrencyChange = { StoreRepository.setCurrency(it) },
                        currentLanguage = language,
                        onLanguageChange = { StoreRepository.setLanguage(it) },
                        cartItemCount = cartItems.sumOf { it.quantity },
                        onCartClick = {
                            currentScreen = AppScreen.Cart
                            currentTab = NavigationTab.CART
                        },
                        onAccountClick = {
                            currentScreen = AppScreen.MyOrders
                            currentTab = NavigationTab.ACCOUNT
                        },
                        onAdminClick = {
                            currentScreen = AppScreen.Admin
                        },
                        onNavigateShelf = { shelfKey ->
                            when (shelfKey) {
                                "home" -> {
                                    currentScreen = AppScreen.Home
                                    currentTab = NavigationTab.HOME
                                }
                                "categories" -> {
                                    currentScreen = AppScreen.CategoryView(null)
                                    currentTab = NavigationTab.CATEGORIES
                                }
                                "deals", "bestsellers", "new" -> {
                                    currentScreen = AppScreen.CategoryView(null)
                                    currentTab = NavigationTab.CATEGORIES
                                }
                                "downloads", "account" -> {
                                    currentScreen = AppScreen.MyOrders
                                    currentTab = NavigationTab.ACCOUNT
                                }
                                "wishlist" -> {
                                    currentScreen = AppScreen.Wishlist
                                    currentTab = NavigationTab.WISHLIST
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                // Show Bottom Nav Bar on Main destinations
                if (currentScreen !is AppScreen.ProductDetail &&
                    currentScreen !is AppScreen.Checkout &&
                    currentScreen !is AppScreen.OrderSuccess &&
                    currentScreen !is AppScreen.Admin
                ) {
                    AmazonBottomNavBar(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            currentTab = tab
                            currentScreen = when (tab) {
                                NavigationTab.HOME -> AppScreen.Home
                                NavigationTab.CATEGORIES -> AppScreen.CategoryView(selectedCategoryIdInHeader)
                                NavigationTab.CART -> AppScreen.Cart
                                NavigationTab.WISHLIST -> AppScreen.Wishlist
                                NavigationTab.ACCOUNT -> AppScreen.MyOrders
                            }
                        },
                        cartItemCount = cartItems.sumOf { it.quantity },
                        wishlistCount = wishlistIds.size
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (val screen = currentScreen) {
                    is AppScreen.Home -> {
                        HomeScreen(
                            categories = categories,
                            products = products,
                            banners = banners,
                            currency = currency,
                            wishlistIds = wishlistIds,
                            recentlyViewedIds = recentlyViewedIds,
                            onWishlistToggle = { prodId ->
                                val added = StoreRepository.toggleWishlist(prodId)
                                val msg = if (added) "تمت الإضافة إلى المفضلة" else "تمت الإزالة من المفضلة"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onAddToCart = { prod ->
                                StoreRepository.addToCart(prod, 1)
                                Toast.makeText(context, "تمت إضافة ${prod.titleAr} إلى السلة", Toast.LENGTH_SHORT).show()
                            },
                            onProductClick = { prod ->
                                StoreRepository.recordProductView(prod.id)
                                currentScreen = AppScreen.ProductDetail(prod)
                            },
                            onCategoryClick = { catId ->
                                selectedCategoryIdInHeader = catId
                                currentScreen = AppScreen.CategoryView(catId)
                                currentTab = NavigationTab.CATEGORIES
                            },
                            onNavigateShelf = { shelfKey ->
                                when (shelfKey) {
                                    "deals", "bestsellers" -> {
                                        currentScreen = AppScreen.CategoryView(null)
                                        currentTab = NavigationTab.CATEGORIES
                                    }
                                }
                            }
                        )
                    }

                    is AppScreen.CategoryView -> {
                        val activeCategory = categories.find { it.id == screen.categoryId }
                        val filteredBySearch = if (searchQuery.isNotEmpty()) {
                            products.filter {
                                it.titleAr.contains(searchQuery, ignoreCase = true) ||
                                it.titleEn.contains(searchQuery, ignoreCase = true) ||
                                it.tags.any { t -> t.contains(searchQuery, ignoreCase = true) }
                            }
                        } else {
                            products
                        }

                        CategoryScreen(
                            category = activeCategory,
                            categories = categories,
                            allProducts = filteredBySearch,
                            currency = currency,
                            wishlistIds = wishlistIds,
                            onCategoryChange = { newCatId ->
                                selectedCategoryIdInHeader = newCatId
                                currentScreen = AppScreen.CategoryView(newCatId)
                            },
                            onWishlistToggle = { prodId ->
                                StoreRepository.toggleWishlist(prodId)
                            },
                            onAddToCart = { prod ->
                                StoreRepository.addToCart(prod, 1)
                                Toast.makeText(context, "تمت الإضافة إلى السلة", Toast.LENGTH_SHORT).show()
                            },
                            onProductClick = { prod ->
                                StoreRepository.recordProductView(prod.id)
                                currentScreen = AppScreen.ProductDetail(prod)
                            }
                        )
                    }

                    is AppScreen.ProductDetail -> {
                        val currentReviews = reviewsMap[screen.product.id] ?: emptyList()
                        ProductDetailScreen(
                            product = screen.product,
                            allProducts = products,
                            reviews = currentReviews,
                            currency = currency,
                            isWishlisted = wishlistIds.contains(screen.product.id),
                            onWishlistToggle = {
                                StoreRepository.toggleWishlist(screen.product.id)
                            },
                            onAddToCart = { prod ->
                                StoreRepository.addToCart(prod, 1)
                                Toast.makeText(context, "تمت الإضافة إلى السلة", Toast.LENGTH_SHORT).show()
                            },
                            onBuyNow = { prod ->
                                StoreRepository.addToCart(prod, 1)
                                currentScreen = AppScreen.Checkout
                            },
                            onBack = {
                                currentScreen = AppScreen.Home
                            },
                            onProductClick = { prod ->
                                StoreRepository.recordProductView(prod.id)
                                currentScreen = AppScreen.ProductDetail(prod)
                            },
                            onHelpfulVote = { revId ->
                                StoreRepository.toggleReviewHelpful(screen.product.id, revId)
                            }
                        )
                    }

                    is AppScreen.Cart -> {
                        CartScreen(
                            cartItems = cartItems,
                            savedItems = savedItems,
                            recommendedProducts = products.filter { p -> cartItems.none { it.product.id == p.id } },
                            currency = currency,
                            activeCoupon = activeCoupon,
                            onUpdateQuantity = { prodId, qty -> StoreRepository.updateCartQuantity(prodId, qty) },
                            onRemoveFromCart = { prodId -> StoreRepository.removeFromCart(prodId) },
                            onMoveToSaved = { prodId -> StoreRepository.moveToSavedForLater(prodId) },
                            onMoveToCartFromSaved = { prodId -> StoreRepository.moveToCartFromSaved(prodId) },
                            onRemoveSaved = { prodId -> StoreRepository.removeSavedForLater(prodId) },
                            onApplyCoupon = { code -> StoreRepository.applyCoupon(code) },
                            onRemoveCoupon = { StoreRepository.removeCoupon() },
                            subtotalSar = StoreRepository.calculateSubtotalSar(),
                            discountSar = StoreRepository.calculateDiscountSar(),
                            vatSar = StoreRepository.calculateVatSar(),
                            totalSar = StoreRepository.calculateTotalSar(),
                            onProceedToCheckout = { currentScreen = AppScreen.Checkout },
                            onContinueShopping = {
                                currentScreen = AppScreen.Home
                                currentTab = NavigationTab.HOME
                            },
                            onProductClick = { prod ->
                                StoreRepository.recordProductView(prod.id)
                                currentScreen = AppScreen.ProductDetail(prod)
                            }
                        )
                    }

                    is AppScreen.Checkout -> {
                        CheckoutScreen(
                            items = cartItems,
                            currency = currency,
                            subtotalSar = StoreRepository.calculateSubtotalSar(),
                            discountSar = StoreRepository.calculateDiscountSar(),
                            vatSar = StoreRepository.calculateVatSar(),
                            totalSar = StoreRepository.calculateTotalSar(),
                            onPlaceOrder = { name, email, paymentMethod ->
                                StoreRepository.placeOrder(name, email, paymentMethod)
                            },
                            onOrderCompleted = { order ->
                                currentScreen = AppScreen.OrderSuccess(order)
                            },
                            onBack = {
                                currentScreen = AppScreen.Cart
                                currentTab = NavigationTab.CART
                            }
                        )
                    }

                    is AppScreen.OrderSuccess -> {
                        OrderSuccessScreen(
                            order = screen.order,
                            currency = currency,
                            onViewOrders = {
                                currentScreen = AppScreen.MyOrders
                                currentTab = NavigationTab.ACCOUNT
                            },
                            onContinueShopping = {
                                currentScreen = AppScreen.Home
                                currentTab = NavigationTab.HOME
                            }
                        )
                    }

                    is AppScreen.MyOrders -> {
                        MyOrdersScreen(
                            orders = orders,
                            currency = currency,
                            onBuyAgain = { prod ->
                                StoreRepository.addToCart(prod, 1)
                                currentScreen = AppScreen.Cart
                                currentTab = NavigationTab.CART
                            },
                            onContinueShopping = {
                                currentScreen = AppScreen.Home
                                currentTab = NavigationTab.HOME
                            }
                        )
                    }

                    is AppScreen.Wishlist -> {
                        val wishlistedProducts = products.filter { wishlistIds.contains(it.id) }
                        WishlistScreen(
                            wishlistProducts = wishlistedProducts,
                            currency = currency,
                            onRemoveFromWishlist = { StoreRepository.toggleWishlist(it) },
                            onAddToCart = { prod ->
                                StoreRepository.addToCart(prod, 1)
                                Toast.makeText(context, "تمت الإضافة للسلة", Toast.LENGTH_SHORT).show()
                            },
                            onProductClick = { prod ->
                                StoreRepository.recordProductView(prod.id)
                                currentScreen = AppScreen.ProductDetail(prod)
                            },
                            onContinueShopping = {
                                currentScreen = AppScreen.Home
                                currentTab = NavigationTab.HOME
                            }
                        )
                    }

                    is AppScreen.Admin -> {
                        AdminScreen(
                            products = products,
                            categories = categories,
                            banners = banners,
                            orders = orders,
                            currency = currency,
                            onUpdateProductDeal = { prodId, discount, duration ->
                                StoreRepository.updateProductDeal(prodId, discount, duration)
                            },
                            onToggleBestSeller = { prodId -> StoreRepository.toggleProductBestSeller(prodId) },
                            onToggleFeatured = { prodId -> StoreRepository.toggleProductFeatured(prodId) },
                            onUpdateProductPriceAndDelivery = { prodId, price, payload, inst ->
                                StoreRepository.updateProductPriceAndDelivery(prodId, price, payload, inst)
                            },
                            onUpdateCategory = { cat -> StoreRepository.updateCategory(cat) },
                            onUpdateBanner = { banner -> StoreRepository.updateBanner(banner) },
                            onBack = {
                                currentScreen = AppScreen.Home
                                currentTab = NavigationTab.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}
