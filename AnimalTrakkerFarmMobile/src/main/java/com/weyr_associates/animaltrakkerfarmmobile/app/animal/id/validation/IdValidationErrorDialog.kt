package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdEntry

object IdValidationErrorDialog {
    @JvmStatic
    fun showIdEntryIsRequiredError(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_id_entry_required_error)
            .setMessage(R.string.dialog_message_id_entry_entry_required_error)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    @JvmStatic
    fun showPartialIdEntryError(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_partial_tag_entry_error)
            .setMessage(R.string.dialog_message_partial_tag_entry_error)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    @JvmStatic
    fun showIdNumberFormatError(context: Context, idEntry: IdEntry) {
        //TODO: Flush out the error message based on specific ID type and its required formatting.
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_id_number_format_error_generic)
            .setMessage(
                context.getString(
                    R.string.dialog_message_id_number_format_error_generic,
                    idEntry.number,
                    idEntry.type.name
                )
            )
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    @JvmStatic
    fun showIdCombinationError(context: Context, error: IdsValidationError) {
        when (error) {
            IdsValidationError.NameIdsNotSupported -> showNameIdsNotSupportedError(context)
            is IdsValidationError.ExceededIdLimitForIdType -> showIdLimitForIdTypeError(context, error)
            is IdsValidationError.RequiredOfficialIdsNotMet -> showRequiredOfficialIdsNotMetError(context, error)
            is EIDNumberAlreadyInUse -> showEIDAlreadyInUseError(context, error)
            is DuplicationOfEIDs -> showDuplicatingEIDError(context, error)
        }
    }

    @JvmStatic
    fun showNameIdsNotSupportedError(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_name_id_type_not_supported)
            .setMessage(R.string.dialog_message_name_id_type_not_supported)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    @JvmStatic
    private fun showIdLimitForIdTypeError(context: Context, error: IdsValidationError.ExceededIdLimitForIdType) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.dialog_title_id_limit_exceeded_for_type, error.idType.name))
            .setMessage(context.getString(R.string.dialog_message_id_limit_exceeded_for_type, error.limit, error.idType.name))
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    @JvmStatic
    fun showRequiredOfficialIdsNotMetError(context: Context, error: IdsValidationError.RequiredOfficialIdsNotMet) {
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_required_official_id_number_not_met)
            .setMessage(context.getString(R.string.dialog_message_required_official_id_number_not_met, error.requiredOfficialIds))
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    fun showEIDAlreadyInUseError(context: Context, error: EIDNumberAlreadyInUse) {
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_eid_number_already_in_use)
            .setMessage(
                context.getString(
                    R.string.dialog_message_eid_number_already_in_use,
                    error.eidNumber
                )
            )
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    @JvmStatic
    fun showDuplicatingEIDError(context: Context, error: DuplicationOfEIDs) {
        val duplicateEIDsFormatted = error.duplicates.joinToString("\n") { it.number }
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_duplicating_eids)
            .setMessage(context.getString(R.string.dialog_message_duplicating_eids, duplicateEIDsFormatted))
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }
}
