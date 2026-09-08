package com.shohan.khatago.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shohan.khatago.R
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
        if (!SettingsRepository(applicationContext).settings.first().notificationsEnabled) return Result.success()
        val database = FinanceDatabase.get(applicationContext)
        val records = database.financeDao().getAllRecords()
        val payments = database.financeDao().getAllPayments()
        val paid = payments.groupingBy { it.recordId }.fold(0L) { total, payment -> total + payment.amountMinor }
        val today = LocalDate.now().toEpochDay().toInt()
        val due = records.count { it.dueDateEpochDay != null && it.dueDateEpochDay!! <= today + 1 && it.dueDateEpochDay!! >= today && it.amountMinor > (paid[it.id] ?: 0L) }
        if (due == 0) return Result.success()
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Payment reminders", NotificationManager.IMPORTANCE_DEFAULT).apply { description = "Helpful local reminders for upcoming payments" })
        manager.notify(NOTIFICATION_ID, NotificationCompat.Builder(applicationContext, CHANNEL_ID).setSmallIcon(R.drawable.ic_stat_khatago).setContentTitle("KhataGo payment reminder").setContentText("You have $due payment${if (due == 1) "" else "s"} due today or tomorrow.").setAutoCancel(true).build())
        return Result.success()
    }
    companion object { const val CHANNEL_ID = "payment_reminders"; const val NOTIFICATION_ID = 1001 }
}

object ReminderScheduler {
    fun schedule(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("khatago_due_reminders", ExistingPeriodicWorkPolicy.KEEP, PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS).build())
    }
}
