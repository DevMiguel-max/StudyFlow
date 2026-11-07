package com.studyflow.app.domain.manager

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.privacyDataStore by preferencesDataStore(name = "privacy_settings")

enum class AgeGroup(val key: String, val label: String) {
    UNDER_12(PrivacyManager.AGE_GROUP_UNDER_12, "Menor de 12 anos"),
    TEEN_12_17(PrivacyManager.AGE_GROUP_TEEN_12_17, "12 a 17 anos (Adolescente)"),
    ADULT_18_PLUS(PrivacyManager.AGE_GROUP_ADULT_18_PLUS, "18 anos ou mais");

    companion object {
        fun fromKey(key: String?): AgeGroup = when (key) {
            PrivacyManager.AGE_GROUP_UNDER_12 -> UNDER_12
            PrivacyManager.AGE_GROUP_TEEN_12_17 -> TEEN_12_17
            else -> ADULT_18_PLUS
        }
    }
}

class PrivacyManager(private val context: Context) {

    companion object {
        const val CURRENT_TERMS_VERSION = "1.0_2026-11"

        const val AGE_GROUP_UNDER_12 = "UNDER_12"
        const val AGE_GROUP_TEEN_12_17 = "TEEN_12_17"
        const val AGE_GROUP_ADULT_18_PLUS = "ADULT_18_PLUS"

        private val KEY_AI_CONSENT_DECISION_MADE = booleanPreferencesKey("ai_consent_decision_made")
        private val KEY_AI_CONSENT_GRANTED = booleanPreferencesKey("ai_consent_granted")
        private val KEY_ANALYTICS_OPT_IN = booleanPreferencesKey("analytics_opt_in")
        private val KEY_AGE_GROUP = stringPreferencesKey("age_group")
        private val KEY_TERMS_ACCEPTED_VERSION = stringPreferencesKey("terms_accepted_version")
        private val KEY_TERMS_ACCEPTED_TIMESTAMP = longPreferencesKey("terms_accepted_timestamp")
    }

    val aiConsentDecisionMade: Flow<Boolean> = context.privacyDataStore.data.map {
        it[KEY_AI_CONSENT_DECISION_MADE] ?: false
    }

    val aiConsentGranted: Flow<Boolean> = context.privacyDataStore.data.map {
        it[KEY_AI_CONSENT_GRANTED] ?: false
    }

    val analyticsOptIn: Flow<Boolean> = context.privacyDataStore.data.map {
        it[KEY_ANALYTICS_OPT_IN] ?: false
    }

    val ageGroup: Flow<String?> = context.privacyDataStore.data.map {
        it[KEY_AGE_GROUP]
    }

    val ageGroupEnum: Flow<AgeGroup> = ageGroup.map { AgeGroup.fromKey(it) }

    val termsAcceptedVersion: Flow<String?> = context.privacyDataStore.data.map {
        it[KEY_TERMS_ACCEPTED_VERSION]
    }

    val termsAcceptedTimestamp: Flow<Long> = context.privacyDataStore.data.map {
        it[KEY_TERMS_ACCEPTED_TIMESTAMP] ?: 0L
    }

    val isConsentAndAgeCompleted: Flow<Boolean> = context.privacyDataStore.data.map { prefs ->
        val group = prefs[KEY_AGE_GROUP]
        val timestamp = prefs[KEY_TERMS_ACCEPTED_TIMESTAMP] ?: 0L
        !group.isNullOrBlank() && group != AGE_GROUP_UNDER_12 && timestamp > 0L
    }

    suspend fun setAiConsent(granted: Boolean) {
        context.privacyDataStore.edit { prefs ->
            prefs[KEY_AI_CONSENT_DECISION_MADE] = true
            prefs[KEY_AI_CONSENT_GRANTED] = granted
        }
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        val currentAge = ageGroup.first()
        // ECA Digital: Se for adolescente (12 a 17), Analytics é forçado a permanecer desligado
        val finalEnabled = if (currentAge == AGE_GROUP_TEEN_12_17) false else enabled

        context.privacyDataStore.edit { prefs ->
            prefs[KEY_ANALYTICS_OPT_IN] = finalEnabled
        }

        try {
            FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(finalEnabled)
        } catch (e: Exception) {
            // Analytics pode falhar se ambiente sem play services
        }
    }

    suspend fun setAgeGroup(group: String) {
        context.privacyDataStore.edit { prefs ->
            prefs[KEY_AGE_GROUP] = group
            if (group == AGE_GROUP_TEEN_12_17) {
                // Menores de 18: Analytics sempre desligado
                prefs[KEY_ANALYTICS_OPT_IN] = false
            }
        }
        if (group == AGE_GROUP_TEEN_12_17) {
            try {
                FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(false)
            } catch (e: Exception) {
                // Ignora falha silenciosa
            }
        }
    }

    suspend fun setAgeGroup(group: AgeGroup) = setAgeGroup(group.key)

    suspend fun recordTermsConsent(version: String = CURRENT_TERMS_VERSION, timestamp: Long = System.currentTimeMillis()) {
        context.privacyDataStore.edit { prefs ->
            prefs[KEY_TERMS_ACCEPTED_VERSION] = version
            prefs[KEY_TERMS_ACCEPTED_TIMESTAMP] = timestamp
        }
    }

    suspend fun revokeTermsConsent() {
        context.privacyDataStore.edit { prefs ->
            prefs.remove(KEY_TERMS_ACCEPTED_VERSION)
            prefs.remove(KEY_TERMS_ACCEPTED_TIMESTAMP)
            prefs.remove(KEY_AGE_GROUP)
            prefs[KEY_ANALYTICS_OPT_IN] = false
        }
        try {
            FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(false)
        } catch (e: Exception) {
            // Ignora falha silenciosa
        }
    }

    suspend fun clearAll() {
        context.privacyDataStore.edit { it.clear() }
        try {
            FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(false)
        } catch (e: Exception) {
            // Ignora falha silenciosa
        }
    }
}
