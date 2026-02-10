package com.astarloa.esgrima

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class MatchStateManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("match_state", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_STATE = "current_state"
        private const val KEY_LAST_IP = "last_ip"
    }

    fun saveState(state: MatchState) {
        val json = gson.toJson(state)
        prefs.edit().putString(KEY_STATE, json).apply()
    }

    fun loadState(): MatchState {
        val json = prefs.getString(KEY_STATE, null)
        return if (json != null) {
            gson.fromJson(json, MatchState::class.java)
        } else {
            MatchState() // Estado por defecto
        }
    }

    fun clearState() {
        prefs.edit().remove(KEY_STATE).apply()
    }

    fun saveLastIp(ip: String) {
        prefs.edit().putString(KEY_LAST_IP, ip).apply()
    }

    fun getLastIp(): String {
        return prefs.getString(KEY_LAST_IP, "") ?: ""
    }
}
