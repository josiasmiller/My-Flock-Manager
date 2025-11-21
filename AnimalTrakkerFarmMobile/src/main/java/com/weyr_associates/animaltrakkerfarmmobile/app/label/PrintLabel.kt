package com.weyr_associates.animaltrakkerfarmmobile.app.label

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

object PrintLabel {

    const val ACTION_ENCODE_LABEL_DATA = "weyr.LT.ENCODE"
    const val EXTRA_ENCODE_FORMAT = "ENCODE_FORMAT"
    const val EXTRA_ENCODE_SHOW_CONTENTS = "ENCODE_SHOW_CONTENTS"
    const val EXTRA_ENCODE_AUTOPRINT = "ENCODE_AUTOPRINT"
    const val EXTRA_ENCODE_SHEEPNAME = "ENCODE_SHEEPNAME"
    const val EXTRA_ENCODE_DATA = "ENCODE_DATA"
    const val EXTRA_ENCODE_DATA1 = "ENCODE_DATA1"
    const val EXTRA_ENCODE_DATE = "ENCODE_DATE"

    const val ENCODE_FORMAT_CODE_128 = "CODE_128"

    const val PREFS_KEY_PRINT_LABEL_TEXT = "label"
    const val DEFAULT_PRINT_LABEL_TEXT = "text"

    const val RESULT_CODE_SUCCESS = 444

    fun newIntentToEncode(printLabelData: PrintLabelData, autoPrint: Boolean): Intent {
        return Intent().apply {
            action = ACTION_ENCODE_LABEL_DATA
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra(EXTRA_ENCODE_FORMAT, ENCODE_FORMAT_CODE_128)
            putExtra(EXTRA_ENCODE_SHOW_CONTENTS, false)
            putExtra(EXTRA_ENCODE_AUTOPRINT, if (autoPrint) "true" else "false")
            with(PrintLabelFormatter(printLabelData)) {
                putExtra(EXTRA_ENCODE_DATA, formatData())
                putExtra(EXTRA_ENCODE_DATA1, formatData1())
                putExtra(EXTRA_ENCODE_SHEEPNAME, formatSheepName())
                putExtra(EXTRA_ENCODE_DATE, formatDateTime())
            }
        }
    }

    fun newIntentToPrint(context: Context, autoPrint: Boolean): Intent {
        return Intent(context, StartMenu::class.java).apply {
            putExtra(Intent.EXTRA_STREAM, StartMenu.tempFileForBarcodePrinting(context))
            type = "image/png"
            if (autoPrint) {
                addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            }
        }
    }

    data class Request(
        val printLabelData: PrintLabelData,
        val autoPrint: Boolean
    )

    class Contract : ActivityResultContract<Request, Boolean>() {
        override fun createIntent(context: Context, input: Request): Intent {
            return newIntentToEncode(input.printLabelData, input.autoPrint)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == RESULT_CODE_SUCCESS
        }
    }
}
