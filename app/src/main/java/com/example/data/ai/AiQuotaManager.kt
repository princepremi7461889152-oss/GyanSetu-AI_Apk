package com.example.data.ai

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AiQuotaManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "gyansetu_ai_quota_prefs"
        private const val KEY_USAGE_DATE = "usage_date"
        private const val KEY_USAGE_COUNT = "usage_count"
        
        // Fair-use quota for shared developer key: 35 requests/day
        const val SHARED_KEY_DAILY_LIMIT = 35
        // Generous quota if user supplies their own Gemini API key: 500 requests/day
        const val CUSTOM_KEY_DAILY_LIMIT = 500
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _dailyUsageCount = MutableStateFlow(0)
    val dailyUsageCount: StateFlow<Int> = _dailyUsageCount.asStateFlow()

    private val _maxQuota = MutableStateFlow(SHARED_KEY_DAILY_LIMIT)
    val maxQuota: StateFlow<Int> = _maxQuota.asStateFlow()

    init {
        checkAndResetDailyCount()
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    @Synchronized
    private fun checkAndResetDailyCount() {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_USAGE_DATE, "") ?: ""
        if (today != lastDate) {
            // New day: reset usage counter
            prefs.edit()
                .putString(KEY_USAGE_DATE, today)
                .putInt(KEY_USAGE_COUNT, 0)
                .apply()
            _dailyUsageCount.value = 0
        } else {
            _dailyUsageCount.value = prefs.getInt(KEY_USAGE_COUNT, 0)
        }
    }

    fun updateKeyType(hasCustomKey: Boolean) {
        _maxQuota.value = if (hasCustomKey) CUSTOM_KEY_DAILY_LIMIT else SHARED_KEY_DAILY_LIMIT
    }

    @Synchronized
    fun canConsumeQuota(): Boolean {
        checkAndResetDailyCount()
        return _dailyUsageCount.value < _maxQuota.value
    }

    @Synchronized
    fun recordRequest(): Boolean {
        checkAndResetDailyCount()
        if (_dailyUsageCount.value >= _maxQuota.value) {
            return false
        }
        val newCount = _dailyUsageCount.value + 1
        prefs.edit()
            .putInt(KEY_USAGE_COUNT, newCount)
            .apply()
        _dailyUsageCount.value = newCount
        return true
    }

    fun getRemainingQuota(): Int {
        checkAndResetDailyCount()
        val remaining = _maxQuota.value - _dailyUsageCount.value
        return if (remaining > 0) remaining else 0
    }
}
