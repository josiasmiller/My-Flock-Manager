package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.horns

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityHornCheckBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.HornCheck

class EditHornCheckActivity : AppCompatActivity() {

    //This activity is quite simple, so we're avoiding a view model for this one.

    companion object {
        fun newIntent(context: Context, hornCheck: HornCheck? = null) =
            Intent(context, EditHornCheckActivity::class.java).apply {
                putExtra(EditHornCheck.EXTRA_HORN_CHECK, hornCheck)
            }
    }

    private val binding: ActivityHornCheckBinding by lazy {
        ActivityHornCheckBinding.inflate(layoutInflater)
    }

    private val badHornsSelectionPresenter = HornSelectionPresenter()
    private val sawedHornsSelectionPresenter = HornSelectionPresenter()

    private lateinit var hornCheck: HornCheck

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        hornCheck = savedInstanceState?.getParcelable(EditHornCheck.EXTRA_HORN_CHECK)
            ?: intent.getParcelableExtra(EditHornCheck.EXTRA_HORN_CHECK)
                    ?: HornCheck()
        badHornsSelectionPresenter.binding = binding.hornSelectionBad
        badHornsSelectionPresenter.horns = hornCheck.badHorns
        badHornsSelectionPresenter.onHornSelectionChanged = { selectedBadHorns ->
            hornCheck = hornCheck.copy(badHorns = selectedBadHorns)
        }
        sawedHornsSelectionPresenter.binding = binding.hornSelectionSawed
        sawedHornsSelectionPresenter.horns = hornCheck.sawedHorns
        sawedHornsSelectionPresenter.onHornSelectionChanged = { selectedSawedHorns ->
            hornCheck = hornCheck.copy(sawedHorns = selectedSawedHorns)
        }
        binding.buttonDone.setOnClickListener {
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(
                    EditHornCheck.EXTRA_HORN_CHECK,
                    hornCheck
                )
            )
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EditHornCheck.EXTRA_HORN_CHECK, hornCheck)
    }
}
