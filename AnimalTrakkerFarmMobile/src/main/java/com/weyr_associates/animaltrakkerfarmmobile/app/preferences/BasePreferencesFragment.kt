package com.weyr_associates.animaltrakkerfarmmobile.app.preferences

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen

abstract class BasePreferencesFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializePreferences()
    }

    override fun onResume() {
        super.onResume()
        // Set up a listener whenever a key changes
        preferenceScreen.getSharedPreferences()
            ?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        // Unregister the listener whenever a key changes
        preferenceScreen.getSharedPreferences()
            ?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null) {
            val preference = findPreference<Preference>(key)
            // Do something. A preference value changed
            if (preference != null) {
                updatePrefSummary(preference)
            }
        }
    }

    private fun initializePreferences() {
        for (i in 0 until preferenceScreen.preferenceCount) {
            // Display the current values
            val preference = preferenceScreen.getPreference(i)
            initSummary(preference)
            if (preference is HasPrerequisites) {
                preference.registerPrerequisiteFulfillment(this)
            }
        }
    }

    private fun initSummary(preference: Preference) {
        if (preference is PreferenceScreen) {
            for (i in 0 until preference.preferenceCount) {
                initSummary(preference.getPreference(i))
            }
        } else {
            updatePrefSummary(preference)
        }
    }

    private fun updatePrefSummary(preference: Preference) {
        (preference as? ListPreference)?.setSummary(preference.getEntry())
            ?: (preference as? EditTextPreference)?.setSummary(preference.text)
    }
}
