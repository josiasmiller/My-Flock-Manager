package com.weyr_associates.animaltrakkerfarmmobile.app.permissions

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityRequiredPermissionsBinding

class RequiredPermissionsActivity : AppCompatActivity() {

    private val viewBinding: ActivityRequiredPermissionsBinding by lazy {
        ActivityRequiredPermissionsBinding.inflate(layoutInflater)
    }

    private lateinit var requestBluetoothPermissions: ActivityResultLauncher<Array<String>>
    private lateinit var requestEnableBluetooth: ActivityResultLauncher<Intent>
    private lateinit var requestRevokePermissionWhitelist: ActivityResultLauncher<Intent>

    private val requiredPermissionsWatcher = RequiredPermissionsWatcher(this).apply {
        onRequiredPermissionsChecked = {
            checkRequiredPermissions()
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        // Button click handlers

        viewBinding.prerequisiteNearbyDevices.setOnClickListener {
            grantNearbyDevicesPrerequisite()
        }
        viewBinding.prerequisiteEnableBluetooth.setOnClickListener {
            grantEnableBluetooth()
        }
        viewBinding.prerequisiteRevokePermissionsWhitelist.setOnClickListener {
            grantRevokePermissionsWhiteListing()
        }

        // Activity Result Launchers

        requestBluetoothPermissions = registerForActivityResult(RequestMultiplePermissions()) {
            onBluetoothPermissionsResult()
        }
        requestEnableBluetooth = registerForActivityResult(StartActivityForResult()) {
            //NO-OP here. Let the bluetooth watch pick up this change since blue can be enabled from
            //most system notification drawers.
        }
        requestRevokePermissionWhitelist = registerForActivityResult(StartActivityForResult()) {
            checkRequiredPermissions()
        }

        lifecycle.addObserver(requiredPermissionsWatcher)
    }

    private fun onBluetoothPermissionsResult() {
        if (!RequiredPermissions.isBluetoothConnectFulfilled(this) ||
            !RequiredPermissions.isBluetoothScanFulfilled(this)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.require_permissions_title_bluetooth_permissions_required)
                .setMessage(R.string.required_permissions_message_bluetooth_permissions_required)
                .setPositiveButton(R.string.yes_label) { _, _ ->
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    })
                }
                .setNegativeButton(R.string.no_label) { _, _ ->
                    //NO-OP
                }
                .create()
                .show()
        } else {
            checkRequiredPermissions()
        }
    }

    private fun checkRequiredPermissions() {
        if (RequiredPermissions.areFulfilled(this)) {
            setResult(RESULT_OK)
            finish()
        } else {
            updatePermissionsDisplay()
        }
    }

    private fun updatePermissionsDisplay() {
        showStateFulfilled(
            viewBinding.imageNearbyDevicesState,
            RequiredPermissions.isBluetoothConnectFulfilled(this) &&
            RequiredPermissions.isBluetoothScanFulfilled(this)
        )
        showStateFulfilled(
            viewBinding.imageEnableBluetoothState,
            RequiredPermissions.isBluetoothEnabled(this)
        )
        showStateFulfilled(
            viewBinding.imageRevokePermissionsState,
            RequiredPermissions.isUnusedAppRestrictionDisabled(this)
        )
    }

    private fun showStateFulfilled(stateView: ImageView, fulfilled: Boolean) {
        with(stateView) {
            setImageResource(
                if (fulfilled) R.drawable.ic_check_mark
                else R.drawable.ic_error_indicator
            )
            imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this@RequiredPermissionsActivity,
                    if (fulfilled) R.color.status_ok
                    else R.color.status_error
                )
            )
        }
    }

    private fun grantNearbyDevicesPrerequisite() {
        if (RequiredPermissions.isBluetoothConnectFulfilled(this) &&
            RequiredPermissions.isBluetoothScanFulfilled(this)) {
            showPermissionAlreadyGrantedToast()
        } else {
            requestBluetoothPermissions.launch(arrayOf(BLUETOOTH_CONNECT, BLUETOOTH_SCAN))
        }
    }

    private fun grantEnableBluetooth() {
        if (RequiredPermissions.isBluetoothEnabled(this)) {
            showPermissionAlreadyGrantedToast()
        } else if (!checkAllSelfPermission(BLUETOOTH_CONNECT)) {
            grantNearbyDevicesPrerequisite()
        } else {
            requestEnableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun grantRevokePermissionsWhiteListing() {
        if (RequiredPermissions.isUnusedAppRestrictionDisabled(this)) {
            showPermissionAlreadyGrantedToast()
        } else {
            requestRevokePermissionWhitelist.launch(
                IntentCompat.createManageUnusedAppRestrictionsIntent(this, packageName)
            )
        }
    }

    private fun showPermissionAlreadyGrantedToast() {
        Toast.makeText(
            this,
            R.string.required_permissions_already_granted,
            Toast.LENGTH_SHORT
        ).show()
    }
}
