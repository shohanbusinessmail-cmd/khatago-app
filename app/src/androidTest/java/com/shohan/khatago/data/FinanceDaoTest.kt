package com.shohan.khatago.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shohan.khatago.domain.RecordType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class FinanceDaoTest {
    private lateinit var db: FinanceDatabase

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), FinanceDatabase::class.java)
            .allowMainThreadQueries().build()
    }
    @After fun tearDown() { db.close() }

    @Test fun paymentTotalPersistsAndDeletingPaymentRecalculates() = runBlocking {
        val dao = db.financeDao(); val now = System.currentTimeMillis(); val today = LocalDate.now().toEpochDay().toInt()
        dao.upsertRecord(FinancialRecordEntity("r", RecordType.SHOP_CREDIT.name, "Shop", "", "", 1000, today, null, null, null, "", null, null, now, now))
        dao.insertPayment(PaymentEntity("p", "r", 400, today, "Cash", "", "", now))
        assertEquals(400L, dao.totalPaidFor("r"))
        dao.deletePayment("p")
        assertEquals(0L, dao.totalPaidFor("r"))
    }

    @Test fun deletingRecordCascadesToPayments() = runBlocking {
        val dao = db.financeDao(); val now = System.currentTimeMillis(); val today = LocalDate.now().toEpochDay().toInt()
        dao.upsertRecord(FinancialRecordEntity("r", RecordType.LOAN.name, "Loan", "", "", 10000, today, null, null, null, "", null, null, now, now))
        dao.insertPayment(PaymentEntity("p", "r", 1000, today, "Cash", "", "", now))
        dao.deleteRecord("r")
        assertEquals(0L, dao.totalPaidFor("r"))
    }
}
