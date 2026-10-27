package br.dev.guisleri.mototrack.ui.screen.profile

import android.content.Context

/**
 * Remembers the level the rider was last seen at, so a level gained (and only a level
 * gained) can be celebrated once.
 */
class RiderLevelMemory(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences("rider_level", Context.MODE_PRIVATE)

    /**
     * Records [level] and says whether it is higher than the one remembered. The first
     * time there is nothing to compare with, so nothing is celebrated.
     */
    internal fun reached(level: Int): Boolean {
        val before = preferences.getInt(KEY_LEVEL, NONE)

        if (before != level) preferences.edit().putInt(KEY_LEVEL, level).apply()

        return before != NONE && level > before
    }

    /** Called on logout: the next account starts from its own level. */
    fun forget() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val KEY_LEVEL = "level"
        const val NONE = -1
    }
}
