package com.weyr_associates.animaltrakkerfarmmobile.app.animal.info

import android.annotation.SuppressLint
import androidx.core.view.isVisible
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewAnimalInfoSectionPremiseBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise

class AnimalPremisePresenter(binding: ViewAnimalInfoSectionPremiseBinding? = null) {

    var binding: ViewAnimalInfoSectionPremiseBinding? = binding
        set(value) {
            if (field != value) {
                field = value
                bindViews()
            }
        }

    var animalPremise: Premise? = null
        set(value) {
            if (field != value) {
                field = value
                bindViews()
            }
        }

    @SuppressLint("SetTextI18n")
    private fun bindViews() {
        val binding = binding ?: return
        val premise = animalPremise
        if (premise == null) {
            binding.textPremiseNickname.text = ""
            binding.textPremiseNickname.isVisible = true
            binding.textPremiseAddress.text = ""
            binding.textPremiseAddress.isVisible = false
            binding.textPremiseNumber.text = ""
            binding.textPremiseNumber.isVisible = false
        } else {
            binding.textPremiseNickname.text = premise.nickname
            binding.textPremiseNickname.isVisible = !premise.nickname.isNullOrBlank()
            val premiseAddress = premise.address
            val premiseGeoLocation = premise.geoLocation
            if (premiseAddress != null) {
                binding.textPremiseAddress.text = buildString {
                    appendLine(premiseAddress.address1)
                    premiseAddress.address2?.let { appendLine(it) }
                    appendLine("${premiseAddress.city}, ${premiseAddress.state} ${premiseAddress.postCode}")
                    append(premiseAddress.country)
                }
                binding.textPremiseAddress.isVisible = true
            } else if (premiseGeoLocation != null) {
                binding.textPremiseAddress.text = "(${premiseGeoLocation.latitude}, ${premiseGeoLocation.longitude})"
                binding.textPremiseAddress.isVisible = true
            } else {
                binding.textPremiseAddress.text = ""
                binding.textPremiseAddress.isVisible = false
            }
            binding.textPremiseNumber.text = premise.number?.let { number ->
                premise.jurisdiction?.let { jurisdiction -> "$number - ${jurisdiction.name}" } ?: number
            }
            binding.textPremiseNumber.isVisible = !premise.number.isNullOrBlank()
        }
    }
}
