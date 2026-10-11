package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChapterRepository
import com.example.data.ai.AiQuotaManager
import com.example.model.ChapterEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals("GyanSetu AI", appName)
    }

    @Test
    fun `ai quota manager prevents resource exhaustion`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val quotaManager = AiQuotaManager(context)
        quotaManager.updateKeyType(hasCustomKey = false)

        assertEquals(AiQuotaManager.SHARED_KEY_DAILY_LIMIT, quotaManager.maxQuota.value)
        assertTrue(quotaManager.canConsumeQuota())

        // Simulate quota consumption up to limit
        val maxLimit = quotaManager.maxQuota.value
        for (i in 0 until maxLimit) {
            assertTrue("Request $i should be allowed", quotaManager.canConsumeQuota())
            assertTrue("Recording request $i should succeed", quotaManager.recordRequest())
        }

        // Next request must be rejected to protect developer resources
        assertFalse("Excess request must be blocked", quotaManager.canConsumeQuota())
        assertFalse("Recording excess request must return false", quotaManager.recordRequest())
        assertEquals(0, quotaManager.getRemainingQuota())

        // User providing their own key lifts fair-use limit
        quotaManager.updateKeyType(hasCustomKey = true)
        assertEquals(AiQuotaManager.CUSTOM_KEY_DAILY_LIMIT, quotaManager.maxQuota.value)
        assertTrue("Custom key must allow more requests", quotaManager.canConsumeQuota())
    }

    @Test
    fun `chapter password hashing verifies correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.AppDatabase.getDatabase(context, kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO))
        val repo = ChapterRepository(db.chapterDao(), db.studyLogDao())

        val pin = "5678"
        val hash = repo.hashPassword(pin)
        assertTrue(hash.isNotEmpty())

        val lockedChapter = ChapterEntity(
            chapterNumber = 1,
            title = "Secret Notes",
            subject = "Physics",
            content = "Classified exam questions",
            isLocked = true,
            passwordHash = hash
        )

        // Correct PIN
        assertTrue(repo.verifyPassword(lockedChapter, "5678"))
        // Incorrect PIN
        assertFalse(repo.verifyPassword(lockedChapter, "0000"))
        assertFalse(repo.verifyPassword(lockedChapter, "1234"))
    }
}
