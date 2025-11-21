package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id

import android.view.LayoutInflater
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalBasicInfoIdBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewAnimalInfoSectionIdsBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo

class AnimalIdsPresenter(binding: ViewAnimalInfoSectionIdsBinding? = null) {

    private val idInfoViewCache = mutableListOf<ItemAnimalBasicInfoIdBinding>()

    var binding: ViewAnimalInfoSectionIdsBinding? = binding
        set(value) {
            if (value != field) {
                field?.let { removeIdViewsFrom(it) }
                field = value?.also { addIdViewsTo(it) }
                bindAnimalIdViews()
            }
        }

    var idInfoItems: List<IdInfo>? = null
        set(value) {
            field = value
            bindAnimalIdViews()
        }

    private fun bindAnimalIdViews() {
        val binding = binding ?: return
        binding.textNoIdsFound.isVisible = idInfoItems.isNullOrEmpty()
        while (idInfoViewCache.size < (idInfoItems?.size ?: 0)) {
            idInfoViewCache.add(
                ItemAnimalBasicInfoIdBinding.inflate(
                    LayoutInflater.from(binding.root.context),
                    binding.root,
                    true
                )
            )
        }
        idInfoViewCache.forEachIndexed { index, idViewBinding ->
            val idInfo = index.takeIf { it < (idInfoItems?.size ?: 0) }
                ?.let { idInfoItems?.get(index) }
            with(idViewBinding) {
                textIdNumber.text = idInfo?.number ?: ""
                textIdTypeName.text = idInfo?.type?.abbreviation ?: ""
                textIdColorName.text = idInfo?.color?.abbreviation ?: ""
                textIdLocationName.text = idInfo?.location?.abbreviation ?: ""
                root.isGone = idInfo == null
            }
        }
    }

    private fun addIdViewsTo(binding: ViewAnimalInfoSectionIdsBinding) {
        idInfoViewCache.forEach { idViewBinding ->
            binding.root.addView(idViewBinding.root)
        }
    }

    private fun removeIdViewsFrom(binding: ViewAnimalInfoSectionIdsBinding) {
        idInfoViewCache.forEach { idViewBinding ->
            binding.root.removeView(idViewBinding.root)
        }
    }
}
