package com.shohan.khatago.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shohan.khatago.domain.RecordType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class FinanceDaoTest {
    private lateinit var db: FinanceDatabase
    @Before fun setup() { db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), FinanceDatabase::class.java).allowMainThreadQueries().build() }
    @After fun tearDown() { db.close() }
    @Test fun foreignKeyPaymentAndRecordArePersisted() {
        val dao = db.financeDao()
        val now = System.currentTimeMillis()
        dao.upsertRecord(FinancialRecordEntity("r", RecordType.SHOP_CREDIT.name, "Shop", "", "", 1000, LocalDate.now().toEpochDay().toInt(), null, null, null, "", null, null, now, now))
        dao.insertPayment(PaymentEntity("p", "r", 400, LocalDate.now().toEpochDay().toInt(), "Cash", "", "", now))
        assertEquals(400L, dao.totalPaidFor("r"))
    }
}
