package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class NavigationTab(val route: String, val titleAr: String, val iconKey: String) {
    HOME("home", "الرئيسية", "bolt"),
    CATEGORIES("categories", "الأقسام", "category"),
    CART("cart", "السلة", "shopping_cart"),
    WISHLIST("wishlist", "المفضلة", "favorite"),
    ACCOUNT("account", "طلباتي", "receipt")
}

@Composable
fun AmazonBottomNavBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    cartItemCount: Int,
    wishlistCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
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
                .height(60.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationTab.values().forEach { tab ->
                val isSelected = tab == currentTab
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
                    label = "tabScale"
                )
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) Mint else TextSecondaryDark,
                    label = "tabColor"
                )

                Column(
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (tab == NavigationTab.CART && cartItemCount > 0) {
                                Badge(
                                    containerColor = PrimaryOlive,
                                    contentColor = Mint
                                ) {
                                    Text(text = cartItemCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (tab == NavigationTab.WISHLIST && wishlistCount > 0) {
                                Badge(
                                    containerColor = PrimaryOliveLight,
                                    contentColor = MintLight
                                ) {
                                    Text(text = wishlistCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    ) {
                        SimpleAppIcon(
                            name = tab.iconKey,
                            size = 22.dp,
                            tint = iconColor
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = tab.titleAr,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = iconColor
                    )

                    // Active underline indicator in Mint
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .width(16.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Mint)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
