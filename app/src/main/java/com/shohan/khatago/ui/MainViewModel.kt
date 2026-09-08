package com.shohan.khatago.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shohan.khatago.data.BackupCodec
import com.shohan.khatago.data.FinanceDatabase
import com.shohan.khatago.data.FinanceRepository
import com.shohan.khatago.data.SettingsRepository
import com.shohan.khatago.domain.AppSettings
import com.shohan.khatago.domain.BackupSnapshot
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.domain.Payment
import com.shohan.khatago.domain.RecordCalculator
import com.shohan.khatago.domain.RecordType
import com.shohan.khatago.domain.MoneyMath
import com.shohan.khatago.util.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FinanceDatabase.get(application)
    private val repository = FinanceRepository(database)
    private val settingsRepository = SettingsRepository(application)

    val records: StateFlow<List<FinancialRecord>> = repository.records.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val payments: StateFlow<List<Payment>> = repository.payments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())
    val summary: StateFlow<com.shohan.khatago.domain.DashboardSummary> = records.combine(settings) { list, _ -> RecordCalculator.calculateSummary(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.shohan.khatago.domain.DashboardSummary())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _selectedType = MutableStateFlow<RecordType?>(null)
    val selectedType: StateFlow<RecordType?> = _selectedType.asStateFlow()
    val visibleRecords: StateFlow<List<FinancialRecord>> = combine(records, query, selectedType) { list, q, type ->
        list.filter { record ->
            (type == null || record.type == type) && (q.isBlank() || listOf(record.title, record.counterparty, record.category, record.notes).any { it.contains(q, ignoreCase = true) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    fun clearMessage() { _message.value = null }
    fun setQuery(value: String) { _query.value = value }
    fun setType(type: RecordType?) { _selectedType.value = type }

    fun completeOnboarding() = viewModelScope.launch { settingsRepository.setOnboardingComplete(true) }
    fun updateName(value: String) = viewModelScope.launch { settingsRepository.updateName(value) }
    fun updateCurrency(value: String) = viewModelScope.launch { settingsRepository.updateCurrency(value) }
    fun updateNotifications(value: Boolean) = viewModelScope.launch { settingsRepository.setNotificationsEnabled(value) }

    fun saveRecord(record: FinancialRecord) = viewModelScope.launch {
        if (record.title.isBlank()) { _message.value = "Add a name so you can find this record later."; return@launch }
        if (record.amountMinor <= 0L) { _message.value = "Enter an amount greater than zero."; return@launch }
        repository.saveRecord(record)
        _message.value = "${record.type.label} saved"
    }

    fun addRecord(
        type: RecordType, title: String, counterparty: String, category: String, amountText: String,
        date: LocalDate, dueDate: LocalDate?, installmentText: String?, installmentCountText: String?,
        notes: String, paymentMethod: String, phone: String
    ) {
        val amount = MoneyMath.validateAmount(amountText)
        val installment = installmentText?.takeIf { it.isNotBlank() }?.let(MoneyMath::validateAmount)
        val count = installmentCountText?.toIntOrNull()?.takeIf { it > 0 }
        if (amount == null || amount <= 0) { _message.value = "Enter a valid amount, for example 1250.00."; return }
        if (installmentText?.isNotBlank() == true && installment == null) { _message.value = "Enter a valid installment amount."; return }
        if (installmentCountText?.isNotBlank() == true && count == null) { _message.value = "Enter a valid installment count."; return }
        saveRecord(FinancialRecord(UUID.randomUUID().toString(), type, title.trim(), counterparty.trim(), category.trim(), amount,
            date.toEpochDay().toInt(), dueDate?.toEpochDay()?.toInt(), installment, count, notes.trim(), paymentMethod.trim().takeIf { it.isNotBlank() }, phone.trim().takeIf { it.isNotBlank() }))
    }

    fun recordPayment(record: FinancialRecord, amountText: String, date: LocalDate, method: String, reference: String, note: String) = viewModelScope.launch {
        val amount = MoneyMath.validateAmount(amountText)
        if (amount == null || amount <= 0L) { _message.value = "Enter a valid payment amount."; return@launch }
        repository.addPayment(record, amount, date.toEpochDay().toInt(), method.trim(), reference.trim(), note.trim()).fold(
            onSuccess = { _message.value = "Payment recorded" }, onFailure = { _message.value = it.message ?: "Payment could not be recorded." }
        )
    }

    fun deleteRecord(record: FinancialRecord) = viewModelScope.launch {
        repository.deleteRecord(record.id)
        _message.value = "${record.title} deleted"
    }

    fun deletePayment(payment: Payment) = viewModelScope.launch {
        repository.deletePayment(payment.id)
        _message.value = "Payment deleted and balance recalculated"
    }

    fun editPayment(record: FinancialRecord, existing: Payment, amountText: String, date: LocalDate, method: String, reference: String, note: String) = viewModelScope.launch {
        val amount = MoneyMath.validateAmount(amountText)
        if (amount == null || amount <= 0L) { _message.value = "Enter a valid payment amount."; return@launch }
        repository.editPayment(record, existing, amount, date.toEpochDay().toInt(), method.trim(), reference.trim(), note.trim()).fold(
            onSuccess = { _message.value = "Payment updated" }, onFailure = { _message.value = it.message ?: "Payment could not be updated." }
        )
    }

    suspend fun makeBackup(): String {
        val snapshot = BackupSnapshot(AppConstants.BACKUP_SCHEMA_VERSION, System.currentTimeMillis(), settings.value, records.value, payments.value)
        return BackupCodec.encode(snapshot)
    }

    fun restoreBackup(raw: String) = viewModelScope.launch {
        try {
            val snapshot = BackupCodec.decode(raw)
            repository.replaceAll(snapshot.records, snapshot.payments)
            settingsRepository.updateName(snapshot.settings.name)
            settingsRepository.updateCurrency(snapshot.settings.currencyCode)
            settingsRepository.setNotificationsEnabled(snapshot.settings.notificationsEnabled)
            _message.value = "Backup restored safely"
        } catch (e: Exception) { _message.value = e.message ?: "Backup could not be restored." }
    }

    fun deleteAllData() = viewModelScope.launch {
        repository.clearAll()
        settingsRepository.clear()
        _message.value = "All local data deleted"
    }
}

class MainViewModelFactory(private val application: Application) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = MainViewModel(application) as T
}
