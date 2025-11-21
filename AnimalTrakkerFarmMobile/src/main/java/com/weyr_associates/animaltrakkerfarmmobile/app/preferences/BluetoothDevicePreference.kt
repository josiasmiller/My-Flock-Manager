package com.weyr_associates.animaltrakkerfarmmobile.app.preferences

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.util.AttributeSet
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresFeature
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Sdk
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.checkAllSelfPermission

class BluetoothDevicePreference(context: Context, attrs: AttributeSet)
    : ListPreference(context, attrs), HasPrerequisites {

    private var enableBluetoothLauncher: ActivityResultLauncher<Intent>? = null
    private var requestBluetoothPermissionsLauncher: ActivityResultLauncher<String>? = null

    init {
        isEnabled = getContext().packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        tryLoadBondedDevicesAsEntries()
    }

    override fun registerPrerequisiteFulfillment(caller: ActivityResultCaller) {
        enableBluetoothLauncher =
            caller.registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
                onBluetoothEnableResult(result)
            }
        requestBluetoothPermissionsLauncher =
            caller.registerForActivityResult(RequestPermission()) { permissionsGranted: Boolean ->
                onBluetoothPermissionResult(permissionsGranted)
            }
    }

    override fun onClick() {
        if (!checkLoadBondedDeviceListPermission()) {
            requestBluetoothPermissions()
        }
        else if (!checkBluetoothEnabled()) {
            requestEnableBluetooth()
        } else {
            loadBondedDevicesAndShowDialog()
        }
    }

    private fun requestEnableBluetooth() {
        enableBluetoothLauncher?.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            ?: throwPrerequisiteRegistrationError()
    }

    private fun requestBluetoothPermissions() {
        requestBluetoothPermissionsLauncher?.launch(BLUETOOTH_CONNECT)
            ?: throwPrerequisiteRegistrationError()
    }

    private fun throwPrerequisiteRegistrationError() {
        throw IllegalStateException("Prerequisites must be registered with host ActivityResultCaller")
    }

    private fun onBluetoothEnableResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            if (checkLoadBondedDeviceListPermission()) {
                loadBondedDevicesAndShowDialog()
            } else {
                requestBluetoothPermissions()
            }
        } else {
            AlertDialog.Builder(context)
                .setTitle(R.string.alert_title_bt_device_pref_enable_bluetooth)
                .setMessage(R.string.alert_message_bt_device_pref_enable_bluetooth)
                .setPositiveButton(R.string.ok) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                .create()
                .show()
        }
    }

    private fun onBluetoothPermissionResult(permissionsGranted: Boolean) {
        if (permissionsGranted) {
            if (checkBluetoothEnabled()) {
                loadBondedDevicesAndShowDialog()
            } else {
                requestEnableBluetooth()
            }
        } else {
            AlertDialog.Builder(context)
                .setTitle(R.string.alert_title_bt_device_pref_grant_bt_connect)
                .setMessage(R.string.alert_message_bt_device_pref_grant_bt_connect)
                .setPositiveButton(R.string.ok) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                .create()
                .show()
        }
    }

    private fun loadBondedDevicesAndShowDialog() {
        tryLoadBondedDevicesAsEntries()
        preferenceManager.showDialog(this)
    }

    private fun tryLoadBondedDevicesAsEntries() {
        if (checkBluetoothEnabled() && checkLoadBondedDeviceListPermission()) {
            loadBondedDevicesAsEntries()
        } else {
            loadEmptyEntries()
        }
    }

    @RequiresPermission(BLUETOOTH_CONNECT)
    @RequiresFeature(
        name = PackageManager.FEATURE_BLUETOOTH,
        enforcement = "android.content.pm.PackageManager#hasSystemFeature"
    )
    private fun loadBondedDevicesAsEntries() {
        val bta = context.getSystemService(BluetoothManager::class.java)?.adapter
        if (bta != null) { //We have bluetooth support, we can continue
            val pairedDevices = bta.bondedDevices
            val entries = arrayOfNulls<CharSequence>(pairedDevices.size)
            val entryValues = arrayOfNulls<CharSequence>(pairedDevices.size)
            for ((i, dev) in pairedDevices.withIndex()) {
                entries[i] = dev.name
                entryValues[i] = dev.address
            }
            setEntries(entries)
            setEntryValues(entryValues)
        }
    }

    private fun loadEmptyEntries() {
        entries = emptyArray()
        entryValues = emptyArray()
    }

    private fun checkBluetoothEnabled(): Boolean {
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            val btAdapter = context.getSystemService(BluetoothManager::class.java)?.adapter
            return btAdapter?.isEnabled ?: false
        }
        return false
    }

    private fun checkLoadBondedDeviceListPermission(): Boolean {
        return !Sdk.requiresRuntimeBluetoothPermissions() ||
                context.checkAllSelfPermission(BLUETOOTH_CONNECT)
    }
}
