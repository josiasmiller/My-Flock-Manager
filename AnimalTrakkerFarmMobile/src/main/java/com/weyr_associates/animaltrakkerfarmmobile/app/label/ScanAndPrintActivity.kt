package com.weyr_associates.animaltrakkerfarmmobile.app.label

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Utilities
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.getAnnotatedText
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceConnectionState
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.ErrorReportDialog
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalIdColorSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalIdTypeSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityScanAndPrintBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType

class ScanAndPrintActivity : AppCompatActivity() {

    private lateinit var dbh: DatabaseHandler

    //TODO need to fix the type of tags based on defaults to handle multiple species No trich tags for rams
    private var readyToPrint = false
    private var autoPrint = false
    private var labelText: String = ""
    private var lastEID: String? = null

    private var selectedOtherIdType: IdType? = null
    private var selectedOtherIdColor: IdColor? = null

    private lateinit var binding: ActivityScanAndPrintBinding
    private lateinit var idTypeSelection: ItemSelectionPresenter<IdType>
    private lateinit var idColorSelection: ItemSelectionPresenter<IdColor>

    private lateinit var eidReaderConnection: EIDReaderConnection

    private val requiredPermissionsWatcher = RequiredPermissionsWatcher(this)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanAndPrintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idTypeSelection = optionalIdTypeSelectionPresenter(binding.otherTagTypeSpinner) { item ->
            selectedOtherIdType = item
            //TODO: Remove UNIDIRECTIONAL
            idTypeSelection.displaySelectedItem(item)
        }

        idColorSelection = optionalIdColorSelectionPresenter(binding.otherTagColorSpinner) { item ->
            selectedOtherIdColor = item
            //TODO: Remove UNIDIRECTIONAL
            idColorSelection.displaySelectedItem(item)
        }

        dbh = DatabaseManager.getInstance(this).createDatabaseHandler()
        loadPreferences()
        
        binding.buttonPanelTop.scanEIDButton.setOnClickListener { toggleScanEid() }
        binding.buttonPanelTop.mainActionButton.setOnClickListener { printLabel() }

        binding.buttonPanelTop.show(
            TopButtonBar.UI_SCANNER_STATUS or
            TopButtonBar.UI_SCAN_EID or
            TopButtonBar.UI_ACTION_PRINT_LABEL
        )

        eidReaderConnection = EIDReaderConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }

        collectLatestOnStart(eidReaderConnection.deviceConnectionState) { connectionState ->
            binding.buttonPanelTop.updateEIDReaderConnectionState(connectionState)
        }
        collectLatestOnStart(eidReaderConnection.isScanningForEID) { isScanning ->
            binding.buttonPanelTop.showScanningEID = isScanning
        }
        observeOneTimeEventsOnStart(eidReaderConnection.onEIDScanned, ::gotEID)

        lifecycle.addObserver(requiredPermissionsWatcher)

        binding.textDataDisclaimer.text = getAnnotatedText(
            R.string.text_scan_and_print_data_disclaimer
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("Scan & Print", " requestCode ")
        if (requestCode == 899) {
            Log.i("Scan & Print", " 899 xxx $resultCode")
            if (resultCode == 444) {
                Log.i("Scan & Print", " Result OK ")
                readyToPrint = true
            }
        } else if (requestCode == 799) {
            Log.i("Scan & Print", "799 xxx $resultCode")
            if (resultCode == 445) {
                Log.i("Scan & Print", "Result OK ")
            }
        } else if (resultCode == RESULT_OK) {
            Log.i("Scan & Print", " zzz ")
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.i("Scan & Print", " OnResume")
        checkForPrint()
        clearData()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbh.close()
    }

    private fun loadPreferences() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        try {
            labelText = preferences.getString("label", "text") ?: "text"
            autoPrint = preferences.getBoolean("autop", false)
        } catch (nfe: NumberFormatException) {
            //NO-OP
        }
    }

    private fun toggleScanEid() {
        if (eidReaderConnection.isScanningForEID.value) {
            eidReaderConnection.cancelEIDScan()
        } else {
            clearData()
            eidReaderConnection.scanEID()
        }
    }

    private fun gotEID(scannedEID: String) {
        lastEID = scannedEID
        binding.eidNewTagNumber.setText(lastEID)
        Log.i("Scan & Print", " with LastEID of $lastEID")
        //  Set Print Label button Green
        binding.buttonPanelTop.mainActionButton.isEnabled = true
    }

    private fun printLabel() {

        val otherTagTypeName = selectedOtherIdType?.name
        Log.i("Scan & Print", " type is $otherTagTypeName")

        val otherTagColorName = selectedOtherIdColor?.name
        Log.i("Scan & Print", " Color is $otherTagColorName")

        val otherTagNumber = binding.otherNewTagNumber.text.toString()
            .takeIf { it.isNotBlank() }
        Log.i("Scan & Print", " Number is /$otherTagNumber/")

        val hasOtherTag = otherTagTypeName != null &&
                otherTagColorName != null &&
                otherTagNumber != null

        val hasNoOtherTag = otherTagTypeName == null &&
                otherTagColorName == null &&
                otherTagNumber == null

        if (!hasOtherTag && !hasNoOtherTag) {
            showPartialIdEntryError()
            return
        }

        val contents = lastEID?.takeIf { 16 <= it.length }?.let {
            it.substring(0, 3) + it.substring(4, 16)
        } ?: return
        Log.i("Scan & Print", " contents $contents")

        try {
            // commented out for no other tag version
            // contents = contents + "_" + ThirdTag;
            val encodeIntent = Intent("weyr.LT.ENCODE")
            encodeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            encodeIntent.addCategory(Intent.CATEGORY_DEFAULT)
            encodeIntent.putExtra("ENCODE_FORMAT", "CODE_128")
            encodeIntent.putExtra("ENCODE_SHOW_CONTENTS", false)
            encodeIntent.putExtra("ENCODE_DATA", contents)
            encodeIntent.putExtra("ENCODE_AUTOPRINT", "false")
            if (autoPrint) {
                encodeIntent.putExtra("ENCODE_AUTOPRINT", "true")
                Log.i("Scan & Print", " autoprint is true ")
            }
            Log.i("Scan & Print", "labeltext $labelText")
            encodeIntent.putExtra("ENCODE_DATA1", labelText)
            encodeIntent.putExtra("ENCODE_DATE", Utilities.TodayIs() + "  " + Utilities.TimeIs())
            Log.i("Scan & Print", " before put extra sheepName ")
            if (otherTagNumber != null) {
                encodeIntent.putExtra(
                    "ENCODE_SHEEPNAME",
                    "$otherTagTypeName = $otherTagNumber $otherTagColorName"
                )
            }
            Log.i("Scan & Print", " after put extra sheepName $otherTagNumber $otherTagColorName")
            encodeIntent.setFlags(0)
            startActivityForResult(encodeIntent, 899)
            Log.i("Scan & Print", " after start activity encode ")
        } catch (ex: Exception) {
            Log.v("PrintLabel ", " in ScanAndPrint RunTimeException: $ex")
            ErrorReportDialog.show(
                context = this,
                errorReport = ErrorReport(
                    action = "Scan and Print Label",
                    summary = buildString {
                        append("contents=${contents}, ")
                        append("otherTagType=${otherTagTypeName}, ")
                        append("otherTagColor=${otherTagColorName}, ")
                        append("otherTagNumber=${otherTagNumber}")
                    },
                    error = ex
                )
            )
        }
        Log.i("Scan & Print", " Ready to print")
    }

    private fun checkForPrint() {
        if (readyToPrint) {
            val print = Intent(this, StartMenu::class.java)
            print.putExtra(Intent.EXTRA_STREAM, StartMenu.tempFileForBarcodePrinting(this))
            print.setType("image/png")
            if (autoPrint) {
                print.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            }
            startActivityForResult(print, 799)
            readyToPrint = false
            binding.buttonPanelTop.mainActionButton.isEnabled = false
        }
    }

    private fun showPartialIdEntryError() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_partial_tag_entry_error)
            .setMessage(R.string.dialog_message_partial_tag_entry_error)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    private fun clearData() {
        // clear out the display of everything
        binding.eidNewTagNumber.setText("")
        binding.otherNewTagNumber.setText("")
    }

    private fun updateEidReaderConnectionState(connectionState: DeviceConnectionState) {
        binding.buttonPanelTop.updateEIDReaderConnectionState(connectionState)
    }
}
