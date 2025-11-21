package com.weyr_associates.animaltrakkerfarmmobile.app.core.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import androidx.annotation.LongDef
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ScannerButtonPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceConnectionState
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceConnectionStatePresenter
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewTopButtonBarBinding

class TopButtonBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs, R.attr.atrkkrTopButtonBarStyle) {

    companion object {

        const val UI_SCANNER_STATUS: Long = 1L
        const val UI_SCAN_EID: Long = 1L shl 1
        const val UI_LOOKUP_ANIMAL: Long = 1L shl 2
        const val UI_SHOW_ALERT: Long = 1L shl 3
        const val UI_CLEAR_DATA: Long = 1L shl 5
        const val UI_ACTION_UPDATE_DATABASE: Long = 1L shl 33
        const val UI_ACTION_PRINT_LABEL: Long = 1L shl 34

        const val UI_ALL = UI_SCANNER_STATUS or
                UI_SCAN_EID or
                UI_LOOKUP_ANIMAL or
                UI_SHOW_ALERT or
                UI_CLEAR_DATA

        @LongDef(
            flag = true,
            value = [
                UI_SCANNER_STATUS,
                UI_SCAN_EID,
                UI_LOOKUP_ANIMAL,
                UI_SHOW_ALERT,
                UI_CLEAR_DATA,
                UI_ACTION_UPDATE_DATABASE,
                UI_ACTION_PRINT_LABEL
            ]
        )
        annotation class TopButtonBarUI
    }

    private val binding: ViewTopButtonBarBinding by lazy {
        ViewTopButtonBarBinding.bind(this)
    }

    private val deviceConnectionStatePresenter: DeviceConnectionStatePresenter by lazy {
        DeviceConnectionStatePresenter(context, binding.imageScannerStatus)
    }

    private val eidScanningButtonPresenter: ScannerButtonPresenter = ScannerButtonPresenter()

    init {
        LayoutInflater.from(context).inflate(R.layout.view_top_button_bar, this, true)
        eidScanningButtonPresenter.binding = binding.btnScanEid
        eidScanningButtonPresenter.buttonText = context.getString(R.string.btn_scan_eid)
    }

    var showScanningEID: Boolean
        get() = eidScanningButtonPresenter.isScanning
        set(value) { eidScanningButtonPresenter.isScanning = value }

    val scanEIDButton: Button
        get() = binding.btnScanEid.btnScan

    val lookupAnimalButton: Button
        get() = binding.btnLookUpAnimal

    val showAlertButton: Button
        get() = binding.btnShowAlert

    val clearDataButton: Button
        get() = binding.btnClearData

    val mainActionButton: Button
        get() = binding.btnMainAction

    fun show(@TopButtonBarUI uiMask: Long) {

        //We use invisible here to keep views in their positions even
        //if some of their neighbors are hidden.

        //Configure speciality indicators and buttons
        binding.imageScannerStatus.isInvisible = 0L == uiMask and UI_SCANNER_STATUS
        binding.btnScanEid.root.isInvisible = 0L == uiMask and UI_SCAN_EID
        binding.btnLookUpAnimal.isInvisible = 0L == uiMask and UI_LOOKUP_ANIMAL
        binding.btnShowAlert.isInvisible = 0L == uiMask and UI_SHOW_ALERT
        binding.btnClearData.isInvisible = 0L == uiMask and UI_CLEAR_DATA

        //Configure the main action button
        with (binding.btnMainAction) {
            isInvisible = 0L == uiMask shr 32
            when {
                0L < uiMask and UI_ACTION_UPDATE_DATABASE -> {
                    setText(R.string.btn_update_database)
                }
                0L < uiMask and UI_ACTION_PRINT_LABEL -> {
                    setText(R.string.btn_print_label)
                }
                else -> {
                    text = ""
                    isInvisible = true
                }
            }
        }
    }

    fun updateEIDReaderConnectionState(deviceConnectionState: DeviceConnectionState) {
        deviceConnectionStatePresenter.connectionState = deviceConnectionState
    }
}
