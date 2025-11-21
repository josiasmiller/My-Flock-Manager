package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.LifecycleOwner

interface FragmentResultListenerRegistrar {

    val context: Context
    val lifeCycleOwner: LifecycleOwner
    val fragmentManager: FragmentManager

    fun registerForFragmentResult(requestKey: String, resultListener: FragmentResultListener) {
        fragmentManager.setFragmentResultListener(requestKey, lifeCycleOwner, resultListener)
    }
}

fun FragmentActivity.asFragmentResultListenerRegistrar(): FragmentResultListenerRegistrar {
    return object : FragmentResultListenerRegistrar {
        override val context: Context
            get() = this@asFragmentResultListenerRegistrar
        override val lifeCycleOwner: LifecycleOwner
            get() = this@asFragmentResultListenerRegistrar
        override val fragmentManager: FragmentManager
            get() = supportFragmentManager
    }
}

fun Fragment.asFragmentResultListenerRegistrar(): FragmentResultListenerRegistrar {
    return object : FragmentResultListenerRegistrar {
        override val context: Context
            get() = requireContext()
        override val lifeCycleOwner: LifecycleOwner
            get() = this@asFragmentResultListenerRegistrar
        override val fragmentManager: FragmentManager
            get() = childFragmentManager
    }
}
