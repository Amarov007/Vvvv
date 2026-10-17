package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.StoreRepository
import com.example.model.AppCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AMARIX007", appName)
    }

    @Test
    fun `test store repository products and categories exist`() {
        val categories = StoreRepository.categories.value
        val products = StoreRepository.products.value
        assertTrue("Categories should not be empty", categories.isNotEmpty())
        assertTrue("Products should not be empty", products.isNotEmpty())

        // Verify the 4 specialized departments
        val categoryIds = categories.map { it.id }.toSet()
        assertTrue(categoryIds.contains("accounts"))
        assertTrue(categoryIds.contains("websites"))
        assertTrue(categoryIds.contains("apps"))
        assertTrue(categoryIds.contains("bots"))
    }

    @Test
    fun `test coupon validation logic`() {
        val (invalidSuccess, _) = StoreRepository.applyCoupon("INVALID_CODE")
        assertTrue("Invalid coupon should fail", !invalidSuccess)

        val (validSuccess, _) = StoreRepository.applyCoupon("AMARIX10")
        assertTrue("AMARIX10 should succeed", validSuccess)
    }

    @Test
    fun `test currency formatting`() {
        val sar = AppCurrency.SAR.format(100.0)
        assertTrue(sar.contains("ر.س") || sar.contains("100"))

        val usd = AppCurrency.USD.format(100.0)
        assertTrue(usd.startsWith("$"))
    }
}
