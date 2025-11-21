package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.hooves

import android.widget.CheckBox
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewHoofSelectionBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.Hoof
import com.weyr_associates.animaltrakkerfarmmobile.model.Hooves
import com.weyr_associates.animaltrakkerfarmmobile.model.hasAll
import com.weyr_associates.animaltrakkerfarmmobile.model.hasNone
import com.weyr_associates.animaltrakkerfarmmobile.model.withHoof
import com.weyr_associates.animaltrakkerfarmmobile.model.withoutHoof

class HoofSelectionPresenter(binding: ViewHoofSelectionBinding? = null) {

    var binding = binding
        set(value) {
            field = value
            bindViews()
        }

    var hooves: Hooves = Hoof.none()
        set(value) {
            if (field != value) {
                field = value
                bindViews()
            }
        }

    var onHoofSelectionChanged: ((Hooves) -> Unit)? = null

    private fun bindViews() {
        val binding = binding ?: return
        unbindCheckStateListeners(binding)
        when {
            hooves.hasNone() -> {
                setAllChecked(binding, isChecked = false)
                binding.checkBoxNone.isChecked = true
            }
            hooves.hasAll() -> {
                setAllChecked(binding, isChecked = true)
                binding.checkBoxNone.isChecked = false
            }
            else -> {
                binding.checkBoxNone.isChecked = false
                binding.checkBoxAll.isChecked = false
                setHoovesChecked(binding, hooves)
            }
        }
        bindCheckStateListeners(binding)
    }

    private fun setAllChecked(binding: ViewHoofSelectionBinding, isChecked: Boolean) {
        binding.checkBoxNone.isChecked = isChecked
        binding.checkBoxAll.isChecked = isChecked
        binding.checkBoxFrontLeft.isChecked = isChecked
        binding.checkBoxFrontRight.isChecked = isChecked
        binding.checkBoxBackLeft.isChecked = isChecked
        binding.checkBoxBackRight.isChecked = isChecked
    }

    private fun setHoovesChecked(binding: ViewHoofSelectionBinding, hooves: Hooves) {
        Hoof.entries.forEach { hoof ->
            checkBoxForHoof(binding, hoof).apply {
                isChecked = hooves.contains(hoof)
            }
        }
    }

    private fun checkBoxForHoof(binding: ViewHoofSelectionBinding, hoof: Hoof): CheckBox {
        return when (hoof) {
            Hoof.FRONT_LEFT -> binding.checkBoxFrontLeft
            Hoof.FRONT_RIGHT -> binding.checkBoxFrontRight
            Hoof.BACK_LEFT -> binding.checkBoxBackLeft
            Hoof.BACK_RIGHT -> binding.checkBoxBackRight
        }
    }

    private fun bindCheckStateListeners(binding: ViewHoofSelectionBinding) {
        binding.checkBoxNone.setOnCheckedChangeListener { _, checked -> onToggleNoneTo(checked) }
        binding.checkBoxAll.setOnCheckedChangeListener { _, checked -> onToggleAllTo(checked) }
        binding.checkBoxFrontLeft.setOnCheckedChangeListener { _, checked -> onToggleHoofTo(Hoof.FRONT_LEFT, checked) }
        binding.checkBoxFrontRight.setOnCheckedChangeListener { _, checked -> onToggleHoofTo(Hoof.FRONT_RIGHT, checked) }
        binding.checkBoxBackLeft.setOnCheckedChangeListener { _, checked -> onToggleHoofTo(Hoof.BACK_LEFT, checked) }
        binding.checkBoxBackRight.setOnCheckedChangeListener { _, checked -> onToggleHoofTo(Hoof.BACK_RIGHT, checked) }
    }

    private fun unbindCheckStateListeners(binding: ViewHoofSelectionBinding) {
        binding.checkBoxNone.setOnCheckedChangeListener(null)
        binding.checkBoxAll.setOnCheckedChangeListener(null)
        binding.checkBoxFrontLeft.setOnCheckedChangeListener(null)
        binding.checkBoxFrontRight.setOnCheckedChangeListener(null)
        binding.checkBoxBackLeft.setOnCheckedChangeListener(null)
        binding.checkBoxBackRight.setOnCheckedChangeListener(null)
    }

    private fun onToggleNoneTo(checked: Boolean) {
        hooves = if (checked) Hoof.none() else Hoof.all()
        onHoofSelectionChanged?.invoke(hooves)
    }

    private fun onToggleAllTo(checked: Boolean) {
        hooves = if (checked) Hoof.all() else Hoof.none()
        onHoofSelectionChanged?.invoke(hooves)
    }

    private fun onToggleHoofTo(hoof: Hoof, checked: Boolean) {
        hooves = if (checked) hooves.withHoof(hoof) else hooves.withoutHoof(hoof)
        onHoofSelectionChanged?.invoke(hooves)
    }
}
