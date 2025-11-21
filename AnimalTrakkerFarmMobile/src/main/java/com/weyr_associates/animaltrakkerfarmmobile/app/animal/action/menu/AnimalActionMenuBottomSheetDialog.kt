package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.menu

import android.content.Context
import android.os.Bundle
import androidx.core.view.postDelayed
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.databinding.DialogAnimalActionMenuBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionMenuOptionBinding
import java.text.Normalizer.Form

class AnimalActionMenuBottomSheetDialog(
    context: Context,
    private val title: String,
    private val subtitle: String,
    private val menuOptions: List<MenuOption>,
    private val onMenuOptionSelected: (MenuOption) -> Unit
) : BottomSheetDialog(
    context, R.style.Theme_AnimalTrakkerMobile_BottomSheet_AnimalActionMenu
) {

    private val binding by lazy {
        DialogAnimalActionMenuBinding.inflate(layoutInflater)
    }

    private var isDismissed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.textTitle.text = title
        binding.textSubtitle.text = subtitle
        binding.buttonClose.setOnClickListener {
            dismissDelayed()
        }
        setupViews()
    }

    private fun setupViews() {
        menuOptions.forEach { menuOption ->
            ItemAnimalActionMenuOptionBinding.inflate(layoutInflater).apply {
                setupViewForOption(this, menuOption)
            }.also { binding.menuOptions.addView(it.root) }
        }
    }

    private fun setupViewForOption(binding: ItemAnimalActionMenuOptionBinding, menuOption: MenuOption) {
        with(binding) {
            text.setText(menuOption.titleResId)
            icon.setImageResource(menuOption.iconResId)
            root.setOnClickListener {
                onOptionSelected(menuOption)
            }
        }
    }

    private fun onOptionSelected(menuOption: MenuOption) {
        if (!isDismissed) {
            dismissDelayed {
                onMenuOptionSelected(menuOption)
            }
        }
    }

    private fun dismissDelayed(onDelayed: (() -> Unit)? = null) {
        isDismissed = true
        binding.root.postDelayed(150) {
            dismiss()
            onDelayed?.invoke()
        }
    }
}
