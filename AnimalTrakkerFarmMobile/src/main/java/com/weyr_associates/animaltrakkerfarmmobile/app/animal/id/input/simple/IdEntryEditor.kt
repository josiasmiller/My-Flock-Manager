package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple

import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IdEntryEditor {

    enum class IdEntryField {
        FIRST,
        SECOND,
        THIRD
    }

    sealed interface Event

    sealed interface InputEvent : Event {
        data object IdNumber1Changed : InputEvent
        data object IdNumber2Changed : InputEvent
        data object IdNumber3Changed : InputEvent
    }

    var idNumber1: String
    var idNumber2: String
    var idNumber3: String

    val isEditable: StateFlow<Boolean>

    val selectedIdType1: StateFlow<IdType?>
    val selectedIdColor1: StateFlow<IdColor?>
    val selectedIdLocation1: StateFlow<IdLocation?>

    val selectedIdType2: StateFlow<IdType?>
    val selectedIdColor2: StateFlow<IdColor?>
    val selectedIdLocation2: StateFlow<IdLocation?>

    val selectedIdType3: StateFlow<IdType?>
    val selectedIdColor3: StateFlow<IdColor?>
    val selectedIdLocation3: StateFlow<IdLocation?>

    val events: SharedFlow<Event>

    fun selectIdType1(idType: IdType?)
    fun selectIdColor1(idColor: IdColor?)
    fun selectIdLocation1(idLocation: IdLocation?)

    fun selectIdType2(idType: IdType?)
    fun selectIdColor2(idColor: IdColor?)
    fun selectIdLocation2(idLocation: IdLocation?)

    fun selectIdType3(idType: IdType?)
    fun selectIdColor3(idColor: IdColor?)
    fun selectIdLocation3(idLocation: IdLocation?)

    fun onEIDScanned(eidString: String)
}
