package com.weyr_associates.animaltrakkerfarmmobile.app.animal

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.AddAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.SimpleAddAnimalActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AnimalAlertItem
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AnimalAlertsAdapter
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.databinding.DialogAnimalAlertsBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalLocationEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.BirthEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.DeathEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.Gap
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.MovementEvent

object AnimalDialogs {

    fun showAnimalNotFound(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_animal_not_found)
            .setMessage(R.string.dialog_message_animal_not_found)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .create()
            .show()
    }

    fun showAnimalAlert(context: Context, alerts: List<AnimalAlert>) {
        val binding = DialogAnimalAlertsBinding.inflate(LayoutInflater.from(context))
        with(binding.recyclerAnimalAlerts) {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = AnimalAlertsAdapter().apply { submitList(alerts.map { AnimalAlertItem(it) }) }
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.alert_warning)
            .setView(binding.root)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    fun promptToAddAnimalWithEID(activity: Activity, eidNumber: String, requestCode: Int) {
        promptToAddAnimalWithEID(activity, eidNumber) {
            SimpleAddAnimalActivity.startToAddAndSelect(
                activity,
                IdType.ID_TYPE_ID_EID,
                eidNumber,
                requestCode
            )
        }
    }

    fun promptToAddAnimalWithEID(context: Context, eidNumber: String, launcher: ActivityResultLauncher<AddAnimal.Request>) {
        promptToAddAnimalWithEID(context, eidNumber) {
            launcher.launch(AddAnimal.Request(IdType.ID_TYPE_ID_EID, eidNumber))
        }
    }

    private fun promptToAddAnimalWithEID(context: Context, eidNumber: String, confirmationHandler: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_add_animal_from_unknown_eid)
            .setMessage(context.getString(R.string.dialog_message_add_animal_from_unknown_eid, eidNumber))
            .setPositiveButton(R.string.yes_label) { _, _ -> confirmationHandler.invoke() }
            .setNegativeButton(R.string.no_label) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    @SuppressLint("DefaultLocale")
    fun manuallyEnterAnimalWeight(context: Context, currentWeight: Float?, onWeightEntered: (Float?) -> Unit) {

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_manual_weight_entry, null)
        val editTextWeight = dialogView.findViewById<EditText>(R.id.edit_text_weight)?.also { weightInput ->
            currentWeight?.let { weightInput.setText(String.format("%.2f", it)) }
        }

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle(R.string.text_enter_weight)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                val weightText = editTextWeight?.text.toString()
                val weight = weightText.toFloatOrNull()
                onWeightEntered(weight)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.button_cancel) { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()

        dialog.setOnShowListener {
            editTextWeight?.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editTextWeight, InputMethodManager.SHOW_IMPLICIT)
        }

        dialog.show()
    }

    fun showAnimalLocationEventIssues(context: Context, locationEvent: AnimalLocationEvent) {
        when (locationEvent) {
            is BirthEvent -> showAnimalLocationBirthEventIssues(context, locationEvent)
            is DeathEvent -> showAnimalLocationDeathEventIssues(context, locationEvent)
            is MovementEvent -> showAnimalLocationMovementEventIssues(context, locationEvent)
            is Gap -> showAnimalLocationGapEventIssues(context, locationEvent)
        }
    }

    fun showAnimalLocationBirthEventIssues(context: Context, birthEvent: BirthEvent) {
        if (birthEvent.premise != null) {
            return
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_location_birth_event_missing_premise)
            .setMessage(R.string.dialog_message_location_birth_event_missing_premise)
            .setPositiveButton(R.string.ok) { _, _ -> /* NO OP */ }
            .create()
            .show()
    }

    fun showAnimalLocationDeathEventIssues(context: Context, deathEvent: DeathEvent) {
        if (deathEvent.premise != null) {
            return
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_location_death_event_missing_premise)
            .setMessage(R.string.dialog_message_location_death_event_missing_premise)
            .setPositiveButton(R.string.ok) { _, _ -> /* NO OP */ }
            .create()
            .show()
    }

    fun showAnimalLocationMovementEventIssues(context: Context, movementEvent: MovementEvent) {
        if (!movementEvent.hasIssues) {
            return
        }
        val movementEventIssuesMessage = buildString {
            appendLine(context.getString(R.string.dialog_message_location_movement_event_header))
            appendLine()
            if (!movementEvent.isInAnimalLifetime) {
                if (movementEvent.chronology == MovementEvent.Chronology.BEFORE_BIRTH) {
                    appendLine(context.getString(R.string.dialog_message_location_movement_event_item_before_birth))
                }
                if (movementEvent.chronology == MovementEvent.Chronology.AFTER_DEATH) {
                    appendLine(context.getString(R.string.dialog_message_location_movement_event_item_after_death))
                }
            }
            if (movementEvent.isNonPhysicalPremise) {
                appendLine(context.getString(R.string.dialog_message_location_movement_event_item_non_physical_premise))
            }
            if (movementEvent.isMissingPremise) {
                appendLine(context.getString(R.string.dialog_message_location_movement_event_item_missing_premise))
            }
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_location_movement_event_issues)
            .setMessage(movementEventIssuesMessage)
            .setPositiveButton(R.string.ok) { _, _ -> /* NO OP */ }
            .create()
            .show()
    }

    fun showAnimalLocationGapEventIssues(context: Context, gapEvent: Gap) {
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_location_gap_event)
            .setMessage(
                context.getString(
                    R.string.dialog_message_location_gap_event,
                    gapEvent.previousMovement.movementDate.formatForDisplay(),
                    gapEvent.nextMovement.movementDate.formatForDisplay()
                )
            )
            .setPositiveButton(R.string.ok) { _, _ -> /* NO OP */ }
            .create()
            .show()
    }
}
