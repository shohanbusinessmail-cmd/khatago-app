package com.shohan.khatago.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RecordTypeTest {
    @Test fun unknownSerializedTypeHasSafeFallback() { assertEquals(RecordType.EXPENSE, RecordType.from("unknown")) }
    @Test fun obligationsAreExplicit() { assertEquals(true, RecordType.LOAN.isObligation); assertEquals(false, RecordType.INCOME.isObligation) }
}
