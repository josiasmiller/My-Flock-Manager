package com.weyr_associates.animaltrakkerfarmmobile.app.preferences

import android.os.Bundle
import com.weyr_associates.animaltrakkerfarmmobile.R

class BaaCodeSettingsPreferencesFragment : BasePreferencesFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_baa_code_settings, rootKey)
    }
}
