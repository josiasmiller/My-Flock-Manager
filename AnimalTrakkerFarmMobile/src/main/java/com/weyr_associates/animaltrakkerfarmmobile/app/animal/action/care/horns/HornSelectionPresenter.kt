package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.horns

import android.widget.CheckBox
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewHornSelectionBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.Horn
import com.weyr_associates.animaltrakkerfarmmobile.model.Horns
import com.weyr_associates.animaltrakkerfarmmobile.model.hasAll
import com.weyr_associates.animaltrakkerfarmmobile.model.hasNone
import com.weyr_associates.animaltrakkerfarmmobile.model.withHorn
import com.weyr_associates.animaltrakkerfarmmobile.model.withoutHorn

class HornSelectionPresenter(binding: ViewHornSelectionBinding? = null) {

    var binding = binding
        set(value) {
            field = value
            bindViews()
        }

    var horns: Horns = Horn.none()
        set(value) {
            if (field != value) {
                field = value
                bindViews()
            }
        }

    var onHornSelectionChanged: ((Horns) -> Unit)? = null

    private fun bindViews() {
        val binding = binding ?: return
        unbindCheckStateListeners(binding)
        when {
            horns.hasNone() -> {
                setAllChecked(binding, isChecked = false)
                binding.checkBoxNone.isChecked = true
            }
            horns.hasAll() -> {
                setAllChecked(binding, isChecked = true)
                binding.checkBoxNone.isChecked = false
            }
            else -> {
                binding.checkBoxNone.isChecked = false
                binding.checkBoxBoth.isChecked = false
                setHoovesChecked(binding, horns)
            }
        }
        bindCheckStateListeners(binding)
    }

    private fun setAllChecked(binding: ViewHornSelectionBinding, isChecked: Boolean) {
        binding.checkBoxNone.isChecked = isChecked
        binding.checkBoxBoth.isChecked = isChecked
        binding.checkBoxLeft.isChecked = isChecked
        binding.checkBoxRight.isChecked = isChecked
    }

    private fun setHoovesChecked(binding: ViewHornSelectionBinding, horns: Horns) {
        Horn.entries.forEach { horn ->
            checkBoxForHorn(binding, horn).apply {
                isChecked = horns.contains(horn)
            }
        }
    }

    private fun checkBoxForHorn(binding: ViewHornSelectionBinding, horn: Horn): CheckBox {
        return when (horn) {
            Horn.LEFT -> binding.checkBoxLeft
            Horn.RIGHT -> binding.checkBoxRight
        }
    }

    private fun bindCheckStateListeners(binding: ViewHornSelectionBinding) {
        binding.checkBoxNone.setOnCheckedChangeListener { _, checked -> onToggleNoneTo(checked) }
        binding.checkBoxBoth.setOnCheckedChangeListener { _, checked -> onToggleAllTo(checked) }
        binding.checkBoxLeft.setOnCheckedChangeListener { _, checked -> onToggleHoofTo(Horn.LEFT, checked) }
        binding.checkBoxRight.setOnCheckedChangeListener { _, checked -> onToggleHoofTo(Horn.RIGHT, checked) }
    }

    private fun unbindCheckStateListeners(binding: ViewHornSelectionBinding) {
        binding.checkBoxNone.setOnCheckedChangeListener(null)
        binding.checkBoxBoth.setOnCheckedChangeListener(null)
        binding.checkBoxLeft.setOnCheckedChangeListener(null)
        binding.checkBoxRight.setOnCheckedChangeListener(null)
    }

    private fun onToggleNoneTo(checked: Boolean) {
        horns = if (checked) Horn.none() else Horn.all()
        onHornSelectionChanged?.invoke(horns)
    }

    private fun onToggleAllTo(checked: Boolean) {
        horns = if (checked) Horn.all() else Horn.none()
        onHornSelectionChanged?.invoke(horns)
    }

    private fun onToggleHoofTo(horn: Horn, checked: Boolean) {
        horns = if (checked) horns.withHorn(horn) else horns.withoutHorn(horn)
        onHornSelectionChanged?.invoke(horns)
    }
}
