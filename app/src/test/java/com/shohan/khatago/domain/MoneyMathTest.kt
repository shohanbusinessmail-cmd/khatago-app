package com.shohan.khatago.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyMathTest {
    @Test fun parsesMinorUnitsWithoutFloatingPoint() {
        assertEquals(125050L, MoneyMath.validateAmount("1,250.50"))
        assertEquals(900L, MoneyMath.validateAmount("9"))
        assertEquals(5L, MoneyMath.validateAmount("0.05"))
    }
    @Test fun rejectsUnsafeOrNegativeAmounts() {
        assertNull(MoneyMath.validateAmount("12.345"))
        assertNull(MoneyMath.validateAmount("-2"))
        assertNull(MoneyMath.validateAmount("not money"))
    }
    @Test fun balanceNeverGoesNegative() {
        assertEquals(0L, MoneyMath.remaining(1000, 1500))
        assertEquals(0.5f, MoneyMath.progress(1000, 500), 0.001f)
        assertTrue(MoneyMath.progress(1000, 2500) <= 1f)
    }
}
