package com.weyr_associates.animaltrakkerfarmmobile.app.core.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class SelectItemViewModel<T>(
    private val itemDataSource: ItemDataSource<T>,
    private val itemDisplayTextProvider: ItemDisplayTextProvider<T>
) : ViewModel() {

    private val filterTextChanges = MutableStateFlow("")

    val options: StateFlow<List<ItemOption<T>>?> = filterTextChanges
        .debounce(500L)
        .onStart { emit("") }
        .distinctUntilChanged { old, new -> old == new }
        .mapLatest { filterText ->
            itemDataSource.queryItems(filterText).map { item ->
                ItemOption(
                    displayText = itemDisplayTextProvider.displayTextForItem(item),
                    data = item
                )
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun updateFilterText(filterText: String) {
        val cleanFilterText = cleanFilterText(filterText)
        filterTextChanges.value = cleanFilterText
    }

    override fun onCleared() {
        super.onCleared()
        itemDataSource.close()
    }

    private fun cleanFilterText(filterText: String): String {
        return filterText.trim()
    }
}
