package com.weyr_associates.animaltrakkerfarmmobile.app.preferences

import android.os.Bundle
import com.weyr_associates.animaltrakkerfarmmobile.R

class ScaleSettingsPreferencesFragment : BasePreferencesFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_scale_settings, rootKey)
    }
}