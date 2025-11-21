package cl.example.hirmi.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("session_prefs")

class SessionDataStore(private val context: Context) {

    companion object {
        private val LOGGED_USER_ID = stringPreferencesKey("logged_user_id")
    }

    val loggedUserId: Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[LOGGED_USER_ID]
        }

    suspend fun saveUserSession(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[LOGGED_USER_ID] = userId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(LOGGED_USER_ID)
        }
    }
}
