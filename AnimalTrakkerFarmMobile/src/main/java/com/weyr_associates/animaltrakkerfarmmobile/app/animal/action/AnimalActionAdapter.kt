package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.shear.ShearAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.shoe.ShoeAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.wean.WeanAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.hooves.HoofCheckAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.horns.HornCheckAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug.DrugAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.menu.MenuOption
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.weight.WeightAction
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionCheckoffBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionDrugBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionHoofCheckBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionHornCheckBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionWeightBinding

class AnimalActionAdapter(
    private val onActionActivated: (AnimalAction) -> Unit,
    private val onActionMenuActivated: (AnimalAction) -> Unit,
    private val onActionMenuOptionTriggered: (AnimalAction, MenuOption) -> Unit
) : RecyclerView.Adapter<AnimalActionViewHolder>() {

    companion object {

        private const val VIEW_TYPE_DRUG_ACTION = 0
        private const val VIEW_TYPE_WEIGHT_ACTION = 1
        private const val VIEW_TYPE_HOOF_CHECK_ACTION = 2
        private const val VIEW_TYPE_HORN_CHECK_ACTION = 3
        private const val VIEW_TYPE_WEAN_ACTION = 4
        private const val VIEW_TYPE_SHOE_ACTION = 5
        private const val VIEW_TYPE_SHEAR_ACTION = 6

        private val ITEM_CALLBACK = object : DiffUtil.ItemCallback<AnimalAction>() {
            override fun areItemsTheSame(oldItem: AnimalAction, newItem: AnimalAction): Boolean {
                return oldItem.actionId == newItem.actionId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: AnimalAction, newItem: AnimalAction): Boolean {
                return oldItem == newItem
            }
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, ITEM_CALLBACK)

    fun updateActionSet(actionSet: ActionSet?, callback: () -> Unit = {}) {
        val listToSubmit = actionSet?.let {
            it.vaccines + it.dewormers + it.otherDrugs +
                    it.hoofCheck + it.hornCheck + it.weaning +
                    it.shoeing + it.shearing + it.weight
        }?.filterNotNull() ?: emptyList()
        asyncListDiffer.submitList(listToSubmit, callback)
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (asyncListDiffer.currentList[position]) {
            is DrugAction -> VIEW_TYPE_DRUG_ACTION
            is HoofCheckAction -> VIEW_TYPE_HOOF_CHECK_ACTION
            is HornCheckAction -> VIEW_TYPE_HORN_CHECK_ACTION
            is WeightAction -> VIEW_TYPE_WEIGHT_ACTION
            is ShoeAction -> VIEW_TYPE_SHOE_ACTION
            is ShearAction -> VIEW_TYPE_SHEAR_ACTION
            is WeanAction -> VIEW_TYPE_WEAN_ACTION
            else -> throw IllegalStateException("Unsupported action type.")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalActionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DRUG_ACTION -> DrugActionViewHolder(
                binding = ItemAnimalActionDrugBinding.inflate(layoutInflater),
                onActionActivated = onActionActivated,
                onActionMenuActivated = onActionMenuActivated
            )
            VIEW_TYPE_WEIGHT_ACTION -> WeightActionViewHolder(
                binding = ItemAnimalActionWeightBinding.inflate(layoutInflater),
                onActionActivated = onActionActivated,
                onActionMenuActivated = onActionMenuActivated,
                onActionMenuOptionTriggered = onActionMenuOptionTriggered
            )
            VIEW_TYPE_HOOF_CHECK_ACTION -> HoofCheckActionViewHolder(
                binding = ItemAnimalActionHoofCheckBinding.inflate(layoutInflater),
                onActionActivated = onActionActivated,
                onActionMenuActivated = onActionMenuActivated
            )
            VIEW_TYPE_HORN_CHECK_ACTION -> HornCheckActionViewHolder(
                binding = ItemAnimalActionHornCheckBinding.inflate(layoutInflater),
                onActionActivated = onActionActivated,
                onActionMenuActivated = onActionMenuActivated
            )
            VIEW_TYPE_SHOE_ACTION -> ShoeActionViewHolder(
                binding = ItemAnimalActionCheckoffBinding.inflate(layoutInflater),
                onActionActivated = onActionActivated,
                onActionMenuActivated = onActionMenuActivated
            )
            VIEW_TYPE_SHEAR_ACTION -> ShearActionViewHolder(
                binding = ItemAnimalActionCheckoffBinding.inflate(layoutInflater),
                onActionActivated = onActionActivated,
                onActionMenuActivated = onActionMenuActivated
            )
            VIEW_TYPE_WEAN_ACTION -> WeanActionViewHolder(
                binding = ItemAnimalActionCheckoffBinding.inflate(layoutInflater),
                onActionActivated = onActionActivated,
                onActionMenuActivated = onActionMenuActivated
            )
            else -> throw IllegalStateException("Unknown View Type")
        }
    }

    override fun onBindViewHolder(holder: AnimalActionViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_DRUG_ACTION -> holder.takeAs<DrugActionViewHolder>()?.let { drugActionVH ->
                asyncListDiffer.currentList[position].takeAs<DrugAction>()?.let { drugAction ->
                    drugActionVH.bind(drugAction)
                }
            }
            VIEW_TYPE_HOOF_CHECK_ACTION -> holder.takeAs<HoofCheckActionViewHolder>()?.let { hoofCheckActionVH ->
                asyncListDiffer.currentList[position].takeAs<HoofCheckAction>()?.let { hoofCheckAction ->
                    hoofCheckActionVH.bind(hoofCheckAction)
                }
            }
            VIEW_TYPE_HORN_CHECK_ACTION -> holder.takeAs<HornCheckActionViewHolder>()?.let { hornCheckActionVM ->
                asyncListDiffer.currentList[position].takeAs<HornCheckAction>()?.let { hornCheckAction ->
                    hornCheckActionVM.bind(hornCheckAction)
                }
            }
            VIEW_TYPE_WEIGHT_ACTION -> holder.takeAs<WeightActionViewHolder>()?.let { weightActionVH ->
                asyncListDiffer.currentList[position]?.takeAs<WeightAction>()?.let { weightAction ->
                    weightActionVH.bind(weightAction)
                }
            }
            VIEW_TYPE_SHOE_ACTION -> holder.takeAs<ShoeActionViewHolder>()?.let { shoeActionVH ->
                asyncListDiffer.currentList[position]?.takeAs<ShoeAction>()?.let { shoeAction ->
                    shoeActionVH.bind(shoeAction)
                }
            }
            VIEW_TYPE_SHEAR_ACTION -> holder.takeAs<ShearActionViewHolder>()?.let { shearActionVH ->
                asyncListDiffer.currentList[position]?.takeAs<ShearAction>()?.let { shearAction ->
                    shearActionVH.bind(shearAction)
                }
            }
            VIEW_TYPE_WEAN_ACTION -> holder.takeAs<WeanActionViewHolder>()?.let { weanActionVH ->
                asyncListDiffer.currentList[position]?.takeAs<WeanAction>()?.let { weanAction ->
                    weanActionVH.bind(weanAction)
                }
            }
        }
    }
}
