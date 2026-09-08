package com.shohan.khatago.data

import com.shohan.khatago.domain.AppSettings
import com.shohan.khatago.domain.BackupSnapshot
import com.shohan.khatago.domain.CurrencyOption
import com.shohan.khatago.domain.BackupValidationException
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.domain.Payment
import com.shohan.khatago.domain.RecordType
import com.shohan.khatago.util.AppConstants
import org.json.JSONArray
import org.json.JSONObject

/** Versioned, human-readable backup format. Import is parsed completely before it can touch Room. */
object BackupCodec {
    fun encode(snapshot: BackupSnapshot): String {
        val root = JSONObject().apply {
            put("format", "khatago-backup")
            put("schemaVersion", snapshot.schemaVersion)
            put("exportedAt", snapshot.exportedAt)
            put("settings", JSONObject().apply {
                put("name", snapshot.settings.name)
                put("currencyCode", snapshot.settings.currencyCode)
                put("onboardingComplete", snapshot.settings.onboardingComplete)
                put("notificationsEnabled", snapshot.settings.notificationsEnabled)
            })
            put("records", JSONArray().apply { snapshot.records.forEach { put(recordJson(it)) } })
            put("payments", JSONArray().apply { snapshot.payments.forEach { put(paymentJson(it)) } })
        }
        return root.toString(2)
    }

    fun decode(raw: String): BackupSnapshot {
        try {
            val root = JSONObject(raw)
            if (root.optString("format") != "khatago-backup") throw BackupValidationException("This is not a KhataGo backup file.")
            val schema = root.optInt("schemaVersion", -1)
            if (schema != AppConstants.BACKUP_SCHEMA_VERSION) throw BackupValidationException("This backup version is not supported.")
            val settingsJson = root.optJSONObject("settings") ?: throw BackupValidationException("Backup settings are missing.")
            val recordsJson = root.optJSONArray("records") ?: throw BackupValidationException("Backup records are missing.")
            val paymentsJson = root.optJSONArray("payments") ?: throw BackupValidationException("Backup payments are missing.")
            val records = buildList { for (i in 0 until recordsJson.length()) add(parseRecord(recordsJson.getJSONObject(i))) }
            if (records.map { it.id }.toSet().size != records.size) throw BackupValidationException("The backup contains duplicate record ids.")
            val recordIds = records.map { it.id }.toSet()
            val recordsById = records.associateBy { it.id }
            val payments = buildList { for (i in 0 until paymentsJson.length()) add(parsePayment(paymentsJson.getJSONObject(i), recordIds, recordsById)) }
            if (payments.map { it.id }.toSet().size != payments.size) throw BackupValidationException("The backup contains duplicate payment ids.")
            val totalPaid = payments.groupingBy { it.recordId }.fold(0L) { sum, p -> Math.addExact(sum, p.amountMinor) }
            records.forEach { if ((totalPaid[it.id] ?: 0L) > it.amountMinor && it.type.isObligation) throw BackupValidationException("A payment total exceeds a record balance.") }
            return BackupSnapshot(
                schemaVersion = schema, exportedAt = root.optLong("exportedAt", 0L),
                settings = AppSettings(
                    name = settingsJson.optString("name"),
                    currencyCode = CurrencyOption.find(settingsJson.optString("currencyCode", "BDT")).code,
                    onboardingComplete = settingsJson.optBoolean("onboardingComplete", true),
                    notificationsEnabled = settingsJson.optBoolean("notificationsEnabled", true)
                ), records = records, payments = payments
            )
        } catch (e: BackupValidationException) { throw e }
        catch (e: Exception) { throw BackupValidationException("The backup is damaged or could not be read safely.") }
    }

    private fun recordJson(r: FinancialRecord) = JSONObject().apply {
        put("id", r.id); put("type", r.type.name); put("title", r.title); put("counterparty", r.counterparty)
        put("category", r.category); put("amountMinor", r.amountMinor); put("dateEpochDay", r.dateEpochDay)
        putNullable("dueDateEpochDay", r.dueDateEpochDay); putNullable("installmentAmountMinor", r.installmentAmountMinor)
        putNullable("installmentCount", r.installmentCount); put("notes", r.notes); putNullable("paymentMethod", r.paymentMethod)
        putNullable("phone", r.phone); put("createdAt", r.createdAt); put("updatedAt", r.updatedAt)
    }
    private fun paymentJson(p: Payment) = JSONObject().apply {
        put("id", p.id); put("recordId", p.recordId); put("amountMinor", p.amountMinor); put("paidOnEpochDay", p.paidOnEpochDay)
        put("method", p.method); put("reference", p.reference); put("note", p.note); put("createdAt", p.createdAt)
    }
    private fun JSONObject.putNullable(key: String, value: Any?) { if (value == null) put(key, JSONObject.NULL) else put(key, value) }
    private fun JSONObject.nullableString(key: String): String? = if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
    private fun JSONObject.nullableLong(key: String): Long? = if (isNull(key)) null else optLong(key)
    private fun JSONObject.nullableInt(key: String): Int? = if (isNull(key)) null else optInt(key)

    private fun parseRecord(o: JSONObject): FinancialRecord {
        val id = o.optString("id").takeIf { it.isNotBlank() } ?: throw BackupValidationException("A record has no id.")
        val type = runCatching { RecordType.valueOf(o.optString("type")) }.getOrNull() ?: throw BackupValidationException("A record has an invalid type.")
        val amount = o.optLong("amountMinor", -1L)
        if (amount <= 0L) throw BackupValidationException("A record has an invalid amount.")
        return FinancialRecord(id, type, o.optString("title"), o.optString("counterparty"), o.optString("category"), amount,
            o.optInt("dateEpochDay"), o.nullableInt("dueDateEpochDay"), o.nullableLong("installmentAmountMinor"), o.nullableInt("installmentCount"),
            o.optString("notes"), o.nullableString("paymentMethod"), o.nullableString("phone"), 0L, o.optLong("createdAt"), o.optLong("updatedAt"))
    }
    private fun parsePayment(o: JSONObject, recordIds: Set<String>, records: Map<String, FinancialRecord>): Payment {
        val id = o.optString("id").takeIf { it.isNotBlank() } ?: throw BackupValidationException("A payment has no id.")
        val recordId = o.optString("recordId").takeIf { it in recordIds && records[it]?.type?.isObligation == true } ?: throw BackupValidationException("A payment references a record that cannot accept payments.")
        val amount = o.optLong("amountMinor", -1L)
        if (amount <= 0L) throw BackupValidationException("A payment has an invalid amount.")
        return Payment(id, recordId, amount, o.optInt("paidOnEpochDay"), o.optString("method"), o.optString("reference"), o.optString("note"), o.optLong("createdAt"))
    }
}
