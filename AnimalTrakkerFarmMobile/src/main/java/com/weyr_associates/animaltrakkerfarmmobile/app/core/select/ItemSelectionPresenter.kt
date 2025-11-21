package com.weyr_associates.animaltrakkerfarmmobile.app.core.select

import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.weyr_associates.animaltrakkerfarmmobile.app.core.FragmentResultListenerRegistrar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.Serializable
import java.lang.ref.WeakReference

class ItemSelectionPresenter<T>(button: Button? = null) {

    private var _button: WeakReference<Button?> = WeakReference(button)

    init {
        updateSelectItemHandler()
    }

    var button: Button?
        get() = _button.get()
        set(value) {
            _button.get()?.apply {
                setOnClickListener(null)
                setOnLongClickListener(null)
                isLongClickable = false
            }
            _button = WeakReference(value)
            updateSelectItemHandler()
            updateClearItemHandler()
        }

    var onSelectItem: () -> Unit = {}
        set(value) {
            field = value
            updateSelectItemHandler()
        }

    var onClearItem: (() -> Unit)? = null
        set(value) {
            field = value
            updateClearItemHandler()
        }

    var itemDisplayTextProvider: ItemDisplayTextProvider<T> = ToStringItemDisplayTextProvider()
    var noItemDisplayTextProvider: NoItemDisplayTextProvider = EmptyNoItemDisplayTextProvider

    fun displaySelectedItem(item: T?) {
        val button = _button.get() ?: return
        button.text = when {
            item == null -> noItemDisplayTextProvider.displayTextForNoItem()
            else -> itemDisplayTextProvider.displayTextForItem(item)
        }
    }

    fun bindToFlow(
        lifecycleOwner: LifecycleOwner,
        scope: LifecycleCoroutineScope,
        flow: Flow<T?>): Job {
        return scope.launch {
            return@launch lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                return@repeatOnLifecycle flow.collect { displaySelectedItem(it) }
            }
        }
    }

    private fun updateSelectItemHandler() {
        _button.get()?.setOnClickListener { onSelectItem.invoke() }
    }

    private fun updateClearItemHandler() {
        _button.get()?.apply {
             when (val clearItemHandler = onClearItem) {
                null -> {
                    setOnLongClickListener(null)
                    false
                }
                else -> {
                    setOnLongClickListener {
                        clearItemHandler.invoke()
                        true
                    }
                    true
                }
            }.also {
                isLongClickable = it
                isHapticFeedbackEnabled = it
            }
        }
    }
}

fun intItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    requestKey: String,
    button: Button?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Int>,
    onItemSelected: (Int) -> Unit,
    dialogFactory: () -> DialogFragment
): ItemSelectionPresenter<Int> {
    return itemSelectionPresenter(
        registrar,
        requestKey,
        button,
        hintText,
        itemDisplayTextProvider,
        onItemSelected = { _, data ->
            onItemSelected.invoke(
                extractRequiredInt(data)
            )
        },
        makeItemSelectionOptional(
            dialogFactory,
            isOptional = false
        )
    )
}

fun optionalIntSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    requestKey: String,
    button: Button?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Int>,
    onItemSelected: (Int?) -> Unit,
    dialogFactory: () -> DialogFragment
): ItemSelectionPresenter<Int> {
    return itemSelectionPresenter(
        registrar,
        requestKey,
        button,
        hintText,
        itemDisplayTextProvider,
        onItemSelected = { _, data ->
            onItemSelected.invoke(
                extractOptionalInt(data)
            )
        },
        makeItemSelectionOptional(
            dialogFactory,
            isOptional = true
        )
    ).apply {
        onClearItem = { onItemSelected.invoke(null) }
    }
}

fun stringItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    requestKey: String,
    button: Button?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<String>,
    onItemSelected: (String) -> Unit,
    dialogFactory: () -> DialogFragment
): ItemSelectionPresenter<String> {
    return itemSelectionPresenter(
        registrar,
        requestKey,
        button,
        hintText,
        itemDisplayTextProvider,
        onItemSelected = { _, data ->
            onItemSelected.invoke(
                extractRequiredString(data)
            )
        },
        makeItemSelectionOptional(
            dialogFactory,
            isOptional = false
        )
    )
}

fun optionalStringItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    requestKey: String,
    button: Button?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<String>,
    onItemSelected: (String?) -> Unit,
    dialogFactory: () -> DialogFragment
): ItemSelectionPresenter<String> {
    return itemSelectionPresenter(
        registrar,
        requestKey,
        button,
        hintText,
        itemDisplayTextProvider,
        onItemSelected = { _, data ->
            onItemSelected.invoke(
                extractOptionalString(data)
            )
        },
        makeItemSelectionOptional(
            dialogFactory,
            isOptional = true
        )
    ).apply {
        onClearItem = { onItemSelected.invoke(null) }
    }
}

fun <T> serializableItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    requestKey: String,
    button: Button?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<T>,
    onItemSelected: (T) -> Unit,
    dialogFactory: () -> DialogFragment
): ItemSelectionPresenter<T> where T : Serializable {
    return itemSelectionPresenter(
        registrar,
        requestKey,
        button,
        hintText,
        itemDisplayTextProvider,
        onItemSelected = { _, data ->
            onItemSelected.invoke(
                extractRequiredSerializableItem(data)
            )
        },
        makeItemSelectionOptional(
            dialogFactory,
            isOptional = false
        )
    )
}

fun <T> optionalSerializableItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    requestKey: String,
    button: Button?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<T>,
    onItemSelected: (T?) -> Unit,
    dialogFactory: () -> DialogFragment
): ItemSelectionPresenter<T> where T : Serializable {
    return itemSelectionPresenter(
        registrar,
        requestKey,
        button,
        hintText,
        itemDisplayTextProvider,
        onItemSelected = { _, data ->
            onItemSelected.invoke(
                extractOptionalSerializableItem(data)
            )
        },
        makeItemSelectionOptional(
            dialogFactory,
            isOptional = true
        )
    ).apply {
        onClearItem = { onItemSelected.invoke(null) }
    }
}

fun <T> itemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    requestKey: String,
    button: Button?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<T>,
    onItemSelected: (T) -> Unit,
    dialogFactory: () -> DialogFragment
): ItemSelectionPresenter<T> where T : Parcelable {
    return itemSelectionPresenter(
        registrar,
        requestKey,
        button,
        hintText,
        itemDisplayTextProvider,
        onItemSelected = { _, data ->
            onItemSelected.invoke(
                extractRequiredItem(data)
            )
        },
        makeItemSelectionOptional(
            dialogFactory,
            isOptional = false
        )
    )
}

fun <T> optionalItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    requestKey: String,
    button: Button?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<T>,
    onItemSelected: (T?) -> Unit,
    dialogFactory: () -> DialogFragment
): ItemSelectionPresenter<T> where T : Parcelable {
    return itemSelectionPresenter(
        registrar,
        requestKey,
        button,
        hintText,
        itemDisplayTextProvider,
        onItemSelected = { _, data ->
            onItemSelected.invoke(
                extractOptionalItem(data)
            )
        },
        makeItemSelectionOptional(
            dialogFactory,
            isOptional = true
        )
    ).apply {
        onClearItem = { onItemSelected.invoke(null) }
    }
}

private fun extractRequiredInt(data: Bundle): Int {
    if (data.getInt(SelectItem.EXTRA_RESULT) != SelectItem.RESULT_ITEM_SELECTED) {
        throw IllegalStateException("Selection dialog is required to provide a result.")
    }
    return data.selectedIntItem()
}

private fun extractOptionalInt(data: Bundle): Int? {
    return when (data.getInt(SelectItem.EXTRA_RESULT)) {
        SelectItem.RESULT_ITEM_SELECTED -> data.selectedIntItem()
        else -> null
    }
}

private fun extractRequiredString(data: Bundle): String {
    if (data.getInt(SelectItem.EXTRA_RESULT) != SelectItem.RESULT_ITEM_SELECTED) {
        throw IllegalStateException("Selection dialog is required to provide a result.")
    }
    return data.selectedStringItem()
}

private fun extractOptionalString(data: Bundle): String? {
    return when (data.getInt(SelectItem.EXTRA_RESULT)) {
        SelectItem.RESULT_ITEM_SELECTED -> data.selectedStringItem()
        else -> null
    }
}

private fun <T> extractRequiredSerializableItem(data: Bundle): T where T : Serializable {
    if (data.getInt(SelectItem.EXTRA_RESULT) != SelectItem.RESULT_ITEM_SELECTED) {
        throw IllegalStateException("Selection dialog is required to provide a result.")
    }
    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    return requireNotNull(data.getSerializable(SelectItem.EXTRA_SELECTED_ITEM) as T)
}

private fun <T> extractOptionalSerializableItem(data: Bundle): T? where T : Serializable {
    @Suppress("UNCHECKED_CAST")
    return when (data.getInt(SelectItem.EXTRA_RESULT)) {
        SelectItem.RESULT_ITEM_SELECTED -> requireNotNull(
            @Suppress("DEPRECATION")
            data.getSerializable(SelectItem.EXTRA_SELECTED_ITEM) as T
        )
        else -> null
    }
}

private fun <T> extractRequiredItem(data: Bundle): T where T : Parcelable {
    if (data.getInt(SelectItem.EXTRA_RESULT) != SelectItem.RESULT_ITEM_SELECTED) {
        throw IllegalStateException("Selection dialog is required to provide a result.")
    }
    @Suppress("DEPRECATION")
    return requireNotNull(data.getParcelable(SelectItem.EXTRA_SELECTED_ITEM))
}

private fun <T> extractOptionalItem(data: Bundle): T? {
    return when (data.getInt(SelectItem.EXTRA_RESULT)) {
        SelectItem.RESULT_ITEM_SELECTED -> requireNotNull(
            @Suppress("DEPRECATION")
            data.getParcelable(SelectItem.EXTRA_SELECTED_ITEM)
        )
        else -> null
    }
}

private fun makeItemSelectionOptional(dialogFactory: () -> DialogFragment, isOptional: Boolean): () -> DialogFragment {
    return { dialogFactory.invoke().apply {
            requireArguments().putBoolean(SelectItem.EXTRA_IS_OPTIONAL, isOptional)
        }
    }
}

fun <T> itemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    requestKey: String,
    button: Button?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<T>,
    onItemSelected: (String, Bundle) -> Unit,
    dialogFactory: () -> DialogFragment
): ItemSelectionPresenter<T> {
    return createItemSelectionPresenter(
        button, hintText, itemDisplayTextProvider
    ).also {
        registerToShowForFragmentResult(registrar, requestKey, it, onItemSelected) {
            dialogFactory.invoke()
        }
    }
}

private fun <T> createItemSelectionPresenter(
    button: Button?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<T>
): ItemSelectionPresenter<T> {
    return ItemSelectionPresenter<T>(button).apply {
        this.itemDisplayTextProvider = itemDisplayTextProvider
        this.noItemDisplayTextProvider = NoItemDisplayTextProvider { hintText ?: "" }
    }
}

private fun registerToShowForFragmentResult(
    registrar: FragmentResultListenerRegistrar,
    requestKey: String,
    presenter: ItemSelectionPresenter<*>,
    resultListener: FragmentResultListener,
    dialogFactory: () -> DialogFragment
) {
    registrar.registerForFragmentResult(
        requestKey, resultListener
    )
    presenter.onSelectItem = {
        dialogFactory.invoke().show(
            registrar.fragmentManager, requestKey
        )
    }
}
