package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.hooves

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityHoofCheckBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.HoofCheck

class EditHoofCheckActivity : AppCompatActivity() {

    //This activity is quite simple, so we're avoiding a view model for this one.

    companion object {
        fun newIntent(context: Context, hoofCheck: HoofCheck? = null) =
            Intent(context, EditHoofCheckActivity::class.java).apply {
                putExtra(EditHoofCheck.EXTRA_HOOF_CHECK, hoofCheck)
            }
    }

    private val binding: ActivityHoofCheckBinding by lazy {
        ActivityHoofCheckBinding.inflate(layoutInflater)
    }

    private val trimmedHoovesSelectionPresenter = HoofSelectionPresenter()
    private val footRotHoovesSelectionPresenter = HoofSelectionPresenter()
    private val footScaldHoovesSelectionPresenter = HoofSelectionPresenter()

    private lateinit var hoofCheck: HoofCheck

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        hoofCheck = savedInstanceState?.getParcelable(EditHoofCheck.EXTRA_HOOF_CHECK)
            ?: intent.getParcelableExtra(EditHoofCheck.EXTRA_HOOF_CHECK)
                    ?: HoofCheck()
        trimmedHoovesSelectionPresenter.binding = binding.hoofSelectionTrimming
        trimmedHoovesSelectionPresenter.hooves = hoofCheck.trimmed
        trimmedHoovesSelectionPresenter.onHoofSelectionChanged = { trimmedHooves ->
            hoofCheck = hoofCheck.copy(trimmed = trimmedHooves)
        }
        footRotHoovesSelectionPresenter.binding = binding.hoofSelectionFootRot
        footRotHoovesSelectionPresenter.hooves = hoofCheck.withFootRotObserved
        footRotHoovesSelectionPresenter.onHoofSelectionChanged = { rottenHooves ->
            hoofCheck = hoofCheck.copy(withFootRotObserved = rottenHooves)
        }
        footScaldHoovesSelectionPresenter.binding = binding.hoofSelectionFootScald
        footScaldHoovesSelectionPresenter.hooves = hoofCheck.withFootScaldObserved
        footScaldHoovesSelectionPresenter.onHoofSelectionChanged = { scaldedHooves ->
            hoofCheck = hoofCheck.copy(withFootScaldObserved = scaldedHooves)
        }
        binding.buttonDone.setOnClickListener {
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(
                    EditHoofCheck.EXTRA_HOOF_CHECK,
                    hoofCheck
                )
            )
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EditHoofCheck.EXTRA_HOOF_CHECK, hoofCheck)
    }
}
