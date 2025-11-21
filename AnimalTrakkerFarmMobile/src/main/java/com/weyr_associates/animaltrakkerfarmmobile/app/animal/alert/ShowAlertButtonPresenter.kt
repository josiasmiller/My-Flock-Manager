package com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert

import android.content.Context
import android.widget.Button
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo

class ShowAlertButtonPresenter(private val context: Context, button: Button? = null) {

    var button: Button? = button
        set(value) {
            field = value
            updateButtonDisplay()
        }

    var animalInfoState: LookupAnimalInfo.AnimalInfoState? = null
        set(value) {
            field = value
            updateButtonDisplay()
        }

    var onShowAlertClicked: ((AnimalBasicInfo) -> Unit)? = null

    private fun updateButtonDisplay() {
        val button = button ?: return
        when (val animalInfoState = animalInfoState) {
            is LookupAnimalInfo.AnimalInfoState.Loaded -> {
                val alerts = animalInfoState.animalBasicInfo.alerts
                when {
                    alerts.isEmpty() -> {
                        button.isEnabled = false
                        button.setOnClickListener(null)
                    }
                    else -> {
                        button.isEnabled = true
                        button.setOnClickListener {
                            onShowAlertClicked?.invoke(animalInfoState.animalBasicInfo)
                                ?: AnimalDialogs.showAnimalAlert(context, alerts)
                        }
                    }
                }
            }
            else -> {
                button.isEnabled = false
                button.setOnClickListener(null)
            }
        }

    }
}
