package br.dev.guisleri.mototrack.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.UUID

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth"
)

class TokenStorage internal constructor(
    private val dataStore: DataStore<Preferences>
) {

    constructor(context: Context) : this(context.applicationContext.authDataStore)

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val SESSION_ID = stringPreferencesKey("session_id")
    }

    val accessToken: Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    val refreshToken: Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    val hasSession: Flow<Boolean> =
        dataStore.data.map { preferences ->
            val accessToken = preferences[ACCESS_TOKEN]
            val refreshToken = preferences[REFRESH_TOKEN]

            !accessToken.isNullOrBlank() &&
                    !refreshToken.isNullOrBlank()
        }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[SESSION_ID] = UUID.randomUUID().toString()
        }
    }

    suspend fun getSessionSnapshot(): TokenSessionSnapshot? {
        repeat(2) {
            val preferences = dataStore.data.first()
            val accessToken = preferences[ACCESS_TOKEN]
            val refreshToken = preferences[REFRESH_TOKEN]
            if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                return null
            }
            preferences[SESSION_ID]?.let { sessionId ->
                return TokenSessionSnapshot(accessToken, refreshToken, sessionId)
            }

            var snapshot: TokenSessionSnapshot? = null
            dataStore.edit { current ->
                if (
                    current[ACCESS_TOKEN] == accessToken &&
                    current[REFRESH_TOKEN] == refreshToken
                ) {
                    val sessionId = current[SESSION_ID]
                        ?: UUID.randomUUID().toString().also {
                            current[SESSION_ID] = it
                        }
                    snapshot = TokenSessionSnapshot(
                        accessToken,
                        refreshToken,
                        sessionId
                    )
                }
            }
            snapshot?.let { return it }
        }
        return null
    }

    suspend fun clearTokens() {
        clearTokensAndGetRefreshToken()
    }

    suspend fun getTokens(): Pair<String?, String?> {
        val preferences = dataStore.data.first()
        return preferences[ACCESS_TOKEN] to preferences[REFRESH_TOKEN]
    }

    suspend fun saveTokensIfRefreshTokenMatches(
        expectedRefreshToken: String,
        accessToken: String,
        refreshToken: String
    ): Boolean {
        var saved = false
        dataStore.edit { preferences ->
            if (preferences[REFRESH_TOKEN] == expectedRefreshToken) {
                preferences[ACCESS_TOKEN] = accessToken
                preferences[REFRESH_TOKEN] = refreshToken
                if (preferences[SESSION_ID] == null) {
                    preferences[SESSION_ID] = UUID.randomUUID().toString()
                }
                saved = true
            }
        }
        return saved
    }

    suspend fun clearTokensIfRefreshTokenMatches(
        expectedRefreshToken: String
    ): Boolean {
        var cleared = false
        dataStore.edit { preferences ->
            if (preferences[REFRESH_TOKEN] == expectedRefreshToken) {
                preferences.remove(ACCESS_TOKEN)
                preferences.remove(REFRESH_TOKEN)
                preferences.remove(SESSION_ID)
                cleared = true
            }
        }
        return cleared
    }

    suspend fun clearTokensAndGetRefreshToken(): String? {
        var refreshToken: String? = null
        dataStore.edit { preferences ->
            refreshToken = preferences[REFRESH_TOKEN]
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(SESSION_ID)
        }
        return refreshToken
    }

    suspend fun hasStoredSession(): Boolean {
        return hasSession.first()
    }

    suspend fun getRefreshToken(): String? {
        return refreshToken.first()
    }

    suspend fun getAccessToken(): String? {
        return accessToken.first()
    }
}

data class TokenSessionSnapshot(
    val accessToken: String,
    val refreshToken: String,
    val sessionId: String
)
