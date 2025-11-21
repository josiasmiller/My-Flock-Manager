package com.weyr_associates.animaltrakkerfarmmobile.app.core.select

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.FullScreenBottomSheetDialogFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.DialogFragmentSelectItemBinding
import kotlinx.coroutines.launch

abstract class SelectItemDialogFragment<T : Any>(
    itemType: Class<T>,
    @StringRes private val titleResId: Int,
    private val provideFilter: Boolean = true
) : FullScreenBottomSheetDialogFragment(R.layout.dialog_fragment_select_item){

    private lateinit var itemDelegateFactory: ItemDelegateFactory<T>
    private lateinit var itemListAdapter: ItemListAdapter<T>

    private val viewModel: SelectItemViewModel<T> by viewModels<SelectItemViewModel<T>>() {
        ViewModelProviderFactory(itemDelegateFactory)
    }

    private val binding: DialogFragmentSelectItemBinding by viewBinding()
    private val resultType: ResultType

    private val isOptional: Boolean by lazy {
        requireArguments().getBoolean(SelectItem.EXTRA_IS_OPTIONAL, false)
    }

    private enum class ResultType {
        INT,
        STRING,
        PARCELABLE
    }

    init {
        resultType = when {
            itemType == Int::class.java -> ResultType.INT
            itemType == String::class.java -> ResultType.STRING
            Parcelable::class.java.isAssignableFrom(itemType) -> ResultType.PARCELABLE
            else -> throw IllegalArgumentException("Only Ints, Strings, and Parcelables can be selected.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemDelegateFactory = createItemDelegateFactory(
            requireContext().applicationContext
        )
    }

    override fun getTheme(): Int {
        return R.style.Theme_AnimalTrakkerMobile_ItemSelectionDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            setTitle(titleResId)
            setNavigationIcon(R.drawable.ic_close)
            setNavigationOnClickListener { dismiss() }
            if (isOptional) {
                inflateMenu(R.menu.menu_item_selection)
                setOnMenuItemClickListener { menuItem ->
                    if (menuItem.itemId == R.id.action_clear) {
                        dismissWithSelection(null)
                        true
                    } else false
                }
            }
        }
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = ItemListAdapter(
                itemDelegateFactory.createItemDiffCallback()
            ) {
                dismissWithSelection(it)
            }.also {
                itemListAdapter = it
            }
            itemAnimator = null
        }
        with(binding.filterEditText) {
            isVisible = provideFilter
            if (provideFilter) {
                addTextChangedListener {
                    viewModel.updateFilterText(it?.toString() ?: "")
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.options.collect { itemOptions ->
                    updateDisplay(itemOptions)
                }
            }
        }
    }

    protected fun setTitle(title: String) {
        binding.toolbar.title = title
    }

    /**
     * Called to acquire delegates for serving item data.
     *
     * Implementations should avoid capturing references
     * to the calling fragment to avoid temporary memory
     * leaks as some delegate references may be shared
     * with the associated view model.
     *
     * @param context The Application's Context.
     */
    protected abstract fun createItemDelegateFactory(context: Context): ItemDelegateFactory<T>

    private fun updateDisplay(itemOptions: List<ItemOption<T>>?) {
        when {
            itemOptions == null -> {
                binding.progressSpinner.isVisible = true
                binding.recyclerView.isVisible = false
                binding.textNoItemOptions.isVisible = false
                binding.textNoFilteredItemOptions.isVisible = false
            }
            else -> {
                itemListAdapter.submitList(itemOptions)
                when {
                    itemOptions.isNotEmpty() -> {
                        binding.recyclerView.isVisible = true
                        binding.progressSpinner.isVisible = false
                        binding.textNoItemOptions.isVisible = false
                        binding.textNoFilteredItemOptions.isVisible = false
                    }
                    else -> {
                        val isFiltered = binding.filterEditText.text.isNotEmpty()
                        binding.recyclerView.isVisible = false
                        binding.progressSpinner.isVisible = false
                        binding.textNoItemOptions.isVisible = !isFiltered
                        binding.textNoFilteredItemOptions.isVisible = isFiltered
                    }
                }
            }
        }
    }

    private fun dismissWithSelection(item: T?) {
        if (!isOptional && item == null) {
            throw IllegalStateException("Cannot emit a null result when selection does not allow clear.")
        }
        val resultKey = SelectItem.EXTRA_SELECTED_ITEM
        setFragmentResult(
            requireNotNull(requireArguments().getString(SelectItem.EXTRA_REQUEST_KEY)) {
                "Unable to obtain request key from arguments."
            },
            Bundle().apply {
                putInt(
                    SelectItem.EXTRA_RESULT,
                    if (item == null) SelectItem.RESULT_ITEM_CLEARED
                    else SelectItem.RESULT_ITEM_SELECTED
                )
                if (item != null) {
                    when (resultType) {
                        ResultType.INT -> putInt(resultKey, item as Int)
                        ResultType.STRING -> putString(resultKey, item as String)
                        ResultType.PARCELABLE -> putParcelable(resultKey, item as Parcelable)
                    }
                }
                requireArguments().getBundle(SelectItem.EXTRA_ASSOCIATED_DATA)?.let { associatedData ->
                    putBundle(SelectItem.EXTRA_ASSOCIATED_DATA, associatedData)
                }
            }
        )
        dismiss()
    }

    private class ViewModelProviderFactory<T>(
        private val itemDelegateFactory: ItemDelegateFactory<T>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(
            modelClass: Class<VM>
        ): VM = when (modelClass) {
            SelectItemViewModel::class.java -> SelectItemViewModel(
                itemDelegateFactory.createDataSource(),
                itemDelegateFactory.createDisplayTextProvider()
            ) as VM
            else -> throw IllegalArgumentException(
                "Cannot create view model of type ${modelClass.simpleName}"
            )
        }
    }
}
