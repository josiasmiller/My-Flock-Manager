package com.weyr_associates.animaltrakkerfarmmobile.app.permissions

import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.bluetooth.BluetoothAdapter.EXTRA_PREVIOUS_STATE
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_ON
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.weyr_associates.animaltrakkerfarmmobile.app.main.MainActivity

class RequiredPermissionsWatcher(private val context: Context) : DefaultLifecycleObserver {

    var onRequiredPermissionsChecked: ((Boolean) -> Boolean)? = null

    private val intentFilter = IntentFilter().apply {
        addAction(ACTION_STATE_CHANGED)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_STATE_CHANGED) {
                val currentState = requireNotNull(intent.extras)
                    .getInt(EXTRA_STATE)
                val previousState = requireNotNull(intent.extras)
                    .getInt(EXTRA_PREVIOUS_STATE)
                if (currentState != previousState) {
                    when (currentState) {
                        STATE_ON, STATE_OFF -> {
                            checkRequiredPermissions()
                        }
                    }
                }
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        startWatchingBluetoothState()
        checkRequiredPermissions()
    }

    override fun onStop(owner: LifecycleOwner) {
        stopWatchingBluetoothState()
    }

    private fun checkRequiredPermissions() {
        val requiredPermissionsGranted = RequiredPermissions.areFulfilled(context)
        val isHandled = onRequiredPermissionsChecked?.invoke(requiredPermissionsGranted) ?: false
        val shouldReturnToMainActivity = !requiredPermissionsGranted && !isHandled
        if (shouldReturnToMainActivity) {
            MainActivity.returnFrom(context)
        }
    }

    private fun startWatchingBluetoothState() {
        context.registerReceiver(receiver, intentFilter)
    }

    private fun stopWatchingBluetoothState() {
        context.unregisterReceiver(receiver)
    }
}
