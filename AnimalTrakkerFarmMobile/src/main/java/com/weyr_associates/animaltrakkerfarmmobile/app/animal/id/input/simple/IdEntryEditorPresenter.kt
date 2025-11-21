package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple

import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdInputSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple.IdEntryEditor.*
import com.weyr_associates.animaltrakkerfarmmobile.app.core.FragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.AbbreviationDisplayTextProvider
import com.weyr_associates.animaltrakkerfarmmobile.app.select.idColorSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.idLocationSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.idTypeSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalIdColorSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalIdLocationSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalIdTypeSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewSimpleIdEntryBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType

class IdEntryEditorPresenter(
    private val binding: ViewSimpleIdEntryBinding,
    private val idEntryEditor: IdEntryEditor,
    private val lifecycleOwner: LifecycleOwner,
    private val fragmentResultListenerRegistrar: FragmentResultListenerRegistrar
) {

    companion object {
        private const val REQUEST_SELECT_ID_TYPE_1 = "REQUEST_SELECT_TAG_TYPE_1"
        private const val REQUEST_SELECT_ID_TYPE_2 = "REQUEST_SELECT_TAG_TYPE_2"
        private const val REQUEST_SELECT_ID_TYPE_3 = "REQUEST_SELECT_TAG_TYPE_3"
        private const val REQUEST_SELECT_ID_COLOR_1 = "REQUEST_SELECT_ID_COLOR_1"
        private const val REQUEST_SELECT_ID_COLOR_2 = "REQUEST_SELECT_ID_COLOR_2"
        private const val REQUEST_SELECT_ID_COLOR_3 = "REQUEST_SELECT_ID_COLOR_3"
        private const val REQUEST_SELECT_ID_LOCATION_1 = "REQUEST_SELECT_ID_LOCATION_1"
        private const val REQUEST_SELECT_ID_LOCATION_2 = "REQUEST_SELECT_ID_LOCATION_2"
        private const val REQUEST_SELECT_ID_LOCATION_3 = "REQUEST_SELECT_ID_LOCATION_3"
    }

    private val lifecycleScope: LifecycleCoroutineScope
        get() = lifecycleOwner.lifecycleScope

    private val idType1Presenter: ItemSelectionPresenter<IdType>
    private val idColor1Presenter: ItemSelectionPresenter<IdColor>
    private val idLocation1Presenter: ItemSelectionPresenter<IdLocation>

    private val idType2Presenter: ItemSelectionPresenter<IdType>
    private val idColor2Presenter: ItemSelectionPresenter<IdColor>
    private val idLocation2Presenter: ItemSelectionPresenter<IdLocation>

    private val idType3Presenter: ItemSelectionPresenter<IdType>
    private val idColor3Presenter: ItemSelectionPresenter<IdColor>
    private val idLocation3Presenter: ItemSelectionPresenter<IdLocation>

    init {
        with(binding.inputAnimalId1.inputIdNumber) {
            setText(idEntryEditor.idNumber1)
            addTextChangedListener {
                idEntryEditor.idNumber1 = it.toString()
            }
        }
        with(binding.inputAnimalId2.inputIdNumber) {
            setText(idEntryEditor.idNumber2)
            addTextChangedListener {
                idEntryEditor.idNumber2 = it.toString()
            }
        }
        with(binding.inputAnimalId3.inputIdNumber) {
            setText(idEntryEditor.idNumber3)
            addTextChangedListener {
                idEntryEditor.idNumber3 = it.toString()
            }
        }

        // Setup ID Type Spinners

        idType1Presenter = idTypeSelectionPresenter(
            fragmentResultListenerRegistrar,
            button = binding.inputAnimalId1.spinnerIdType,
            requestKey = REQUEST_SELECT_ID_TYPE_1
        ) { idType -> idEntryEditor.selectIdType1(idType) }.also {
            it.bindToFlow(lifecycleOwner, lifecycleScope, idEntryEditor.selectedIdType1)
        }
        lifecycleOwner.collectLatestOnStart(idEntryEditor.selectedIdType1) { idType ->
            with(binding.inputAnimalId1.inputIdNumber) {
                isEnabled = idType != null
                IdInputSettings.applyTo(this, idType?.id)
            }
        }
        idType2Presenter = optionalIdTypeSelectionPresenter(
            fragmentResultListenerRegistrar,
            button = binding.inputAnimalId2.spinnerIdType,
            requestKey = REQUEST_SELECT_ID_TYPE_2
        ) { idType -> idEntryEditor.selectIdType2(idType) }.also {
            it.bindToFlow(lifecycleOwner, lifecycleScope, idEntryEditor.selectedIdType2)
        }
        lifecycleOwner.collectLatestOnStart(idEntryEditor.selectedIdType2) { idType ->
            with(binding.inputAnimalId2.inputIdNumber) {
                isEnabled = idType != null
                IdInputSettings.applyTo(this, idType?.id)
            }
        }
        idType3Presenter = optionalIdTypeSelectionPresenter(
            fragmentResultListenerRegistrar,
            button = binding.inputAnimalId3.spinnerIdType,
            requestKey = REQUEST_SELECT_ID_TYPE_3
        ) { idType -> idEntryEditor.selectIdType3(idType) }.also {
            it.bindToFlow(lifecycleOwner, lifecycleScope, idEntryEditor.selectedIdType3)
        }
        lifecycleOwner.collectLatestOnStart(idEntryEditor.selectedIdType3) { idType ->
            with(binding.inputAnimalId3.inputIdNumber) {
                isEnabled = idType != null
                IdInputSettings.applyTo(this, idType?.id)
            }
        }

        // Setup ID Color Spinners

        idColor1Presenter = idColorSelectionPresenter(
            fragmentResultListenerRegistrar,
            button = binding.inputAnimalId1.spinnerIdColor,
            requestKey = REQUEST_SELECT_ID_COLOR_1,
            itemDisplayTextProvider = AbbreviationDisplayTextProvider
        ) { idColor -> idEntryEditor.selectIdColor1(idColor) }.also {
            it.bindToFlow(lifecycleOwner, lifecycleScope, idEntryEditor.selectedIdColor1)
        }
        idColor2Presenter = optionalIdColorSelectionPresenter(
            fragmentResultListenerRegistrar,
            button = binding.inputAnimalId2.spinnerIdColor,
            requestKey = REQUEST_SELECT_ID_COLOR_2,
            itemDisplayTextProvider = AbbreviationDisplayTextProvider
        ) { idColor -> idEntryEditor.selectIdColor2(idColor) }.also {
            it.bindToFlow(lifecycleOwner, lifecycleScope, idEntryEditor.selectedIdColor2)
        }
        idColor3Presenter = optionalIdColorSelectionPresenter(
            fragmentResultListenerRegistrar,
            button = binding.inputAnimalId3.spinnerIdColor,
            requestKey = REQUEST_SELECT_ID_COLOR_3,
            itemDisplayTextProvider = AbbreviationDisplayTextProvider
        ) { idColor -> idEntryEditor.selectIdColor3(idColor) }.also {
            it.bindToFlow(lifecycleOwner, lifecycleScope, idEntryEditor.selectedIdColor3)
        }

        // Setup ID Location Spinners

        idLocation1Presenter = idLocationSelectionPresenter(
            fragmentResultListenerRegistrar,
            button = binding.inputAnimalId1.spinnerIdLocation,
            requestKey = REQUEST_SELECT_ID_LOCATION_1,
            itemDisplayTextProvider = AbbreviationDisplayTextProvider
        ) { idLocation -> idEntryEditor.selectIdLocation1(idLocation) }.also {
            it.bindToFlow(lifecycleOwner, lifecycleScope, idEntryEditor.selectedIdLocation1)
        }
        idLocation2Presenter = optionalIdLocationSelectionPresenter(
            fragmentResultListenerRegistrar,
            button = binding.inputAnimalId2.spinnerIdLocation,
            requestKey = REQUEST_SELECT_ID_LOCATION_2,
            itemDisplayTextProvider = AbbreviationDisplayTextProvider
        ) { idLocation -> idEntryEditor.selectIdLocation2(idLocation) }.also {
            it.bindToFlow(lifecycleOwner, lifecycleScope, idEntryEditor.selectedIdLocation2)
        }
        idLocation3Presenter = optionalIdLocationSelectionPresenter(
            fragmentResultListenerRegistrar,
            button = binding.inputAnimalId3.spinnerIdLocation,
            requestKey = REQUEST_SELECT_ID_LOCATION_3,
            itemDisplayTextProvider = AbbreviationDisplayTextProvider
        ) { idLocation -> idEntryEditor.selectIdLocation3(idLocation) }.also {
            it.bindToFlow(lifecycleOwner, lifecycleScope, idEntryEditor.selectedIdLocation3)
        }

        lifecycleOwner.collectLatestOnStart(idEntryEditor.isEditable) { isEditable ->
            listOf(
                binding.inputAnimalId1,
                binding.inputAnimalId2,
                binding.inputAnimalId3
            ).forEach { input ->
                with(input) {
                    inputIdNumber.isEnabled = isEditable
                    spinnerIdType.isEnabled = isEditable
                    spinnerIdColor.isEnabled = isEditable
                    spinnerIdLocation.isEnabled = isEditable
                }
            }
        }
        lifecycleOwner.observeOneTimeEventsOnStart(idEntryEditor.events, ::handleEvent)
    }

    private fun handleEvent(event: Event) {
        when (event) {
            is InputEvent -> {
                handleInputEvent(event)
            }
        }
    }

    private fun handleInputEvent(inputEvent: InputEvent) {
        when (inputEvent) {
            InputEvent.IdNumber1Changed -> {
                binding.inputAnimalId1.inputIdNumber.setText(idEntryEditor.idNumber1)
            }

            InputEvent.IdNumber2Changed -> {
                binding.inputAnimalId2.inputIdNumber.setText(idEntryEditor.idNumber2)
            }

            InputEvent.IdNumber3Changed -> {
                binding.inputAnimalId3.inputIdNumber.setText(idEntryEditor.idNumber3)
            }
        }
    }
}
