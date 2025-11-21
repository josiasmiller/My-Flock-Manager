package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input

import android.text.InputType
import android.widget.EditText
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

object IdInputSettings {
    fun applyTo(editText: EditText, idType: EntityId?) {
        with(editText) {
            when (idType) {
                IdType.ID_TYPE_ID_EID -> {
                    inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    filters = IdInputFilters.EID
                    hint = "###_############"
                }
                IdType.ID_TYPE_ID_TRICH -> {
                    inputType = InputType.TYPE_CLASS_NUMBER
                    filters = IdInputFilters.TRICH
                    hint = "#####"
                }
                IdType.ID_TYPE_ID_FED -> {
                    inputType = InputType.TYPE_CLASS_TEXT
                    filters = IdInputFilters.FEDERAL
                    hint = "flock#-id#"
                }
                IdType.ID_TYPE_ID_FED_CANADIAN -> {
                    inputType = InputType.TYPE_CLASS_TEXT
                    filters = IdInputFilters.FEDERAL_CANADIAN
                    hint = ""
                }
                null -> {
                    inputType = InputType.TYPE_CLASS_TEXT
                    filters = emptyArray()
                    hint = ""
                }
                else -> {
                    inputType = InputType.TYPE_CLASS_TEXT
                    filters = IdInputFilters.DEFAULT
                    hint = "#, a, -, _, ⌴"
                }
            }
        }
    }
}
