package com.weyr_associates.animaltrakkerfarmmobile.app.core

import androidx.core.view.isGone
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewScannerButtonBinding

class ScannerButtonPresenter(binding: ViewScannerButtonBinding? = null) {

    var binding: ViewScannerButtonBinding? = binding
        set(value) {
            field = value
            updateDisplay()
        }

    var buttonText: String? = null
        set(value) {
            field = value
            updateDisplay()
        }

    var isScanning: Boolean = false
        set(value) {
            field = value
            updateDisplay()
        }

    private fun updateDisplay() {
        val binding = binding ?: return
        binding.btnScan.text = if (isScanning) ""
        else buttonText
        binding.progressSpinnerScanning.isGone = !isScanning
    }
}
