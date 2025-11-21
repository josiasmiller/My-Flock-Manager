package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResult
import com.weyr_associates.animaltrakkerfarmmobile.app.core.FragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.asFragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDisplayTextProvider
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItem
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.optionalSerializableItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.serializableItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import java.time.LocalDate

class SelectDateDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_DATE) =
            SelectDateDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_DATE = "REQUEST_KEY_SELECT_DATE"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return DatePickerDialog(requireContext()).apply {
            setOnDateSetListener { _, year, month, day ->
                //Add one to the month because month is 0 based to the dialog,
                //but 1 based to the LocalDate class.
                dismissWithSelection(LocalDate.of(year, month + 1, day))
            }
        }
    }

    private fun dismissWithSelection(date: LocalDate) {
        val resultKey = SelectItem.EXTRA_SELECTED_ITEM
        setFragmentResult(
            requireNotNull(requireArguments().getString(SelectItem.EXTRA_REQUEST_KEY)) {
                "Unable to obtain request key from arguments."
            },
            Bundle().apply {
                putInt(
                    SelectItem.EXTRA_RESULT,
                    SelectItem.RESULT_ITEM_SELECTED
                )
                putSerializable(resultKey, date)
                requireArguments().getBundle(SelectItem.EXTRA_ASSOCIATED_DATA)?.let { associatedData ->
                    putBundle(SelectItem.EXTRA_ASSOCIATED_DATA, associatedData)
                }
            }
        )
        dismiss()
    }
}

// region Launch Helpers

fun FragmentActivity.dateSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<LocalDate>? = null,
    onItemSelected: (LocalDate) -> Unit
): ItemSelectionPresenter<LocalDate> {
    return dateSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun FragmentActivity.optionalDateSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<LocalDate>? = null,
    onItemSelected: (LocalDate?) -> Unit
): ItemSelectionPresenter<LocalDate> {
    return optionalDateSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.dateSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<LocalDate>? = null,
    onItemSelected: (LocalDate) -> Unit
): ItemSelectionPresenter<LocalDate> {
    return dateSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun dateSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<LocalDate>?,
    onItemSelected: (LocalDate) -> Unit
): ItemSelectionPresenter<LocalDate> {
    val requestKeyActual = requestKey ?: SelectDateDialogFragment.REQUEST_KEY_SELECT_DATE
    return serializableItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider
            ?: ItemDisplayTextProvider { item -> item.formatForDisplay() },
        onItemSelected
    ) { SelectDateDialogFragment.newInstance(requestKeyActual) }
}

private fun optionalDateSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<LocalDate>?,
    onItemSelected: (LocalDate?) -> Unit
): ItemSelectionPresenter<LocalDate> {
    val requestKeyActual = requestKey ?: SelectDateDialogFragment.REQUEST_KEY_SELECT_DATE
    return optionalSerializableItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider
            ?: ItemDisplayTextProvider { item -> item.formatForDisplay() },
        onItemSelected
    ) { SelectDateDialogFragment.newInstance(requestKeyActual) }
}

// endregion
