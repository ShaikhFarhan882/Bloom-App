package apps.farhan.newage.bloom.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.bloomDataStore: DataStore<Preferences> by preferencesDataStore(name = "bloom_prefs")

data class OnboardingResult(
    val name: String,
    val trackedKeys: Set<String>,
    val reminderSlot: String,
    val reminderHour: Int,
    val reminderMinute: Int,
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val NAME = stringPreferencesKey("name")
        val TRACKED = stringSetPreferencesKey("tracked_items")
        val REMINDER_SLOT = stringPreferencesKey("reminder_slot")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    // Must be declared before any property that uses it
    private val safeData: Flow<Preferences> = context.bloomDataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    val onboardingCompleted: Flow<Boolean> = safeData.map { it[Keys.ONBOARDING_DONE] ?: false }
    val userName: Flow<String> = safeData.map { it[Keys.NAME].orEmpty() }
    val trackedKeys: Flow<Set<String>> = safeData.map { it[Keys.TRACKED] ?: emptySet() }
    val reminderTime: Flow<Pair<Int, Int>> =
        safeData.map { (it[Keys.REMINDER_HOUR] ?: 8) to (it[Keys.REMINDER_MINUTE] ?: 0) }

    suspend fun completeOnboarding(result: OnboardingResult) {
        context.bloomDataStore.edit { prefs ->
            prefs[Keys.NAME] = result.name
            prefs[Keys.TRACKED] = result.trackedKeys
            prefs[Keys.REMINDER_SLOT] = result.reminderSlot
            prefs[Keys.REMINDER_HOUR] = result.reminderHour
            prefs[Keys.REMINDER_MINUTE] = result.reminderMinute
            prefs[Keys.ONBOARDING_DONE] = true
        }
    }

    suspend fun setName(name: String) {
        context.bloomDataStore.edit { it[Keys.NAME] = name }
    }

    suspend fun setTracked(keys: Set<String>) {
        context.bloomDataStore.edit { it[Keys.TRACKED] = keys }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.bloomDataStore.edit {
            it[Keys.REMINDER_HOUR] = hour
            it[Keys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun clearAll() {
        context.bloomDataStore.edit { it.clear() }
    }
}