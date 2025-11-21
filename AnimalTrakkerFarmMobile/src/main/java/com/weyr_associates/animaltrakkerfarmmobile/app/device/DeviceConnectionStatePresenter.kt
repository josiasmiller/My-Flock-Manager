package com.weyr_associates.animaltrakkerfarmmobile.app.device

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class DeviceConnectionStatePresenter(private val context: Context, view: View? = null) {

    private var _view: WeakReference<View?> = WeakReference(view)
    private val colorCache = mutableMapOf<DeviceConnectionState, ColorStateList>()
    private val iconCache = mutableMapOf<DeviceConnectionState, Drawable>()

    var connectionState: DeviceConnectionState = DeviceConnectionState.NONE
        set(value) {
            if (value != field) {
                field = value
                updateDisplay()
            }
        }

    init {
        updateDisplay()
    }

    var view: View?
        get() = _view.get()
        set(value) {
            _view = WeakReference(value)
            updateDisplay()
        }

    fun bindToFlow(
        lifecycleOwner: LifecycleOwner,
        scope: LifecycleCoroutineScope,
        flow: Flow<DeviceConnectionState>
    ): Job {
        return scope.launch {
            return@launch lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                return@repeatOnLifecycle flow.collect { connectionState = it }
            }
        }
    }

    private fun updateDisplay() {
        val view = _view.get() ?: return
        view.backgroundTintList = colorCache.computeIfAbsent(connectionState) { state ->
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    DeviceConnectionState.colorForState(state)
                )
            )
        }
        if (view is ImageView) {
            view.setImageDrawable(
                iconCache.computeIfAbsent(connectionState) { state ->
                    requireNotNull(
                        DeviceConnectionState.iconForState(state).let {
                            AppCompatResources.getDrawable(context, it)
                        }
                    )
                }
            )
        }
    }
}
