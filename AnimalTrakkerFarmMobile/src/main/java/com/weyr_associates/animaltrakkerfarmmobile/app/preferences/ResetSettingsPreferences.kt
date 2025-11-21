package com.weyr_associates.animaltrakkerfarmmobile.app.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.weyr_associates.animaltrakkerfarmmobile.R

class ResetSettingsPreferences : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_reset_settings, rootKey)
    }
}
