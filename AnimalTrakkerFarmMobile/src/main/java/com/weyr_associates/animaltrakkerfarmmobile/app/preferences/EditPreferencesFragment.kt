package com.weyr_associates.animaltrakkerfarmmobile.app.preferences

import android.bluetooth.BluetoothManager
import android.os.Bundle
import androidx.preference.Preference
import com.weyr_associates.animaltrakkerfarmmobile.R

class EditPreferencesFragment : BasePreferencesFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupReaderSettings()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    private fun setupReaderSettings() {
        //Check if there is a Bluetooth device. If not, disable option for Receiver settings
        val preference = findPreference<Preference>("readersettings")
        if (preference != null) {
            val bta = context?.getSystemService(BluetoothManager::class.java)?.adapter
            if (bta == null) {
                preference.isEnabled = false
                preference.setSummary("No Bluetooth Device Found")
            } else {
                preference.isEnabled = true
                preference.setSummary("")
            }
        }
    }
}
