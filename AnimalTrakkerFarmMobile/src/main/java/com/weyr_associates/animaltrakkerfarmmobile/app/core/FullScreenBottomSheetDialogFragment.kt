package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs

open class FullScreenBottomSheetDialogFragment(
    @LayoutRes layoutRes: Int
) : BottomSheetDialogFragment(layoutRes) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
            .requireAs<BottomSheetDialog>()
            .apply { makeFullScreenModal() }
    }
}
