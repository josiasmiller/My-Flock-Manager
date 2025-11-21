package com.weyr_associates.animaltrakkerfarmmobile.app.preferences

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.preference.EditTextPreference
import com.weyr_associates.animaltrakkerfarmmobile.R

class ReaderSettingsPreferencesFragment : BasePreferencesFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRaceReaderFileNameMod()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_reader_settings, rootKey)
    }

    private fun setupRaceReaderFileNameMod() {
        val preference = findPreference<EditTextPreference>("filenamemod")
        preference?.setOnBindEditTextListener { editText: EditText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }
}
