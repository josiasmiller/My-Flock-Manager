package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.weyr_associates.animaltrakkerfarmmobile.R

fun BottomSheetDialog.makeFullScreenModal() {
    setOnShowListener {
        with(behavior) {
            isHideable = false
            isDraggable = false
            isFitToContents = false
            saveFlags = BottomSheetBehavior.SAVE_ALL
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_CREATE) {
                    with(requireViewById<FrameLayout>(R.id.design_bottom_sheet)) {
                        layoutParams = layoutParams.apply {
                            height = WindowManager.LayoutParams.MATCH_PARENT
                        }
                    }
                } else if (event == Lifecycle.Event.ON_DESTROY) {
                    lifecycle.removeObserver(this)
                }
            }
        })
    }
}
