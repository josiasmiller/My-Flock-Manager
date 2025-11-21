package com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.SexPresentation
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.SexStandard
import kotlinx.coroutines.flow.StateFlow

interface LookupAnimalInfo {

    sealed interface Lookup {
        data class ByAnimalId(val animalId: EntityId) : Lookup
        data class ByScannedEID(val eidNumber: String) : Lookup
    }

    sealed interface AnimalInfoState {
        data object Initial : AnimalInfoState

        data class Loaded(
            val animalBasicInfo: AnimalBasicInfo,
            val eidNumber: String?,
            val loadOccurrence: Int = 0
        ) : AnimalInfoState

        data class NotFound(val lookup: Lookup) : AnimalInfoState
    }

    val animalInfoState: StateFlow<AnimalInfoState>

    fun lookupAnimalInfoById(animalId: EntityId)
    fun lookupAnimalInfoByEIDNumber(eidNumber: String)
    fun resetAnimalInfo()

    object Dialogs {
        fun showAnimalRequiredToBeAlive(context: Context, lookupAnimalInfo: LookupAnimalInfo) {
            AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_animal_required_alive_to_perform_action)
                .setMessage(R.string.dialog_message_animal_required_alive_to_perform_action)
                .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
                .setOnDismissListener {
                    lookupAnimalInfo.resetAnimalInfo()
                }
                .create()
                .show()
        }

        fun showAnimalSexMismatch(context: Context, requiredSex: SexStandard, lookupAnimalInfo: LookupAnimalInfo) {
            AlertDialog.Builder(context)
                .setTitle(
                    context.getString(
                        R.string.dialog_title_simple_evaluation_sex_standard_mismatch,
                        SexPresentation.sexNameFor(context, requiredSex)
                    )
                )
                .setMessage(
                    context.getString(
                        R.string.dialog_message_simple_evaluation_sex_standard_mismatch,
                        SexPresentation.sexNameFor(context, requiredSex)
                    )
                )
                .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
                .setOnDismissListener {
                    lookupAnimalInfo.resetAnimalInfo()
                }
                .create()
                .show()
        }

        fun showAnimalSexOrSpeciesMismatch(
            context: Context,
            requiredSexId: EntityId,
            sexName: String,
            speciesName: String,
            lookupAnimalInfo: LookupAnimalInfo
        ) {
            AlertDialog.Builder(context)
                .setTitle(
                    context.getString(
                        R.string.dialog_title_simple_evaluation_species_sex_mismatch,
                        SexPresentation.sexNameFor(context, requiredSexId)
                    )
                )
                .setMessage(
                    context.getString(
                        R.string.dialog_message_simple_evaluation_species_sex_mismatch,
                        speciesName,
                        sexName,
                        SexPresentation.sexNameFor(context, requiredSexId)
                    )
                )
                .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
                .setOnDismissListener {
                    lookupAnimalInfo.resetAnimalInfo()
                }
                .create()
                .show()
        }
    }
}
