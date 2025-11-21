package com.weyr_associates.animaltrakkerfarmmobile.app.preferences

import android.os.Bundle
import com.weyr_associates.animaltrakkerfarmmobile.R

class LabelSettingsPreferencesFragment : BasePreferencesFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_label_settings, rootKey)
    }
}