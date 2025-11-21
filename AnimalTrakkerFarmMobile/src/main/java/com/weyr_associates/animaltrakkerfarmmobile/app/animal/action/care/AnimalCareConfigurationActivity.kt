package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAnimalCareConfigurationBinding

class AnimalCareConfigurationActivity : AppCompatActivity() {

    //This is a simple activity so we are avoiding a view model for now.

    companion object {
        fun newIntent(context: Context, animalCareConfiguration: AnimalCareConfiguration?) =
            Intent(context, AnimalCareConfigurationActivity::class.java).apply {
                putExtra(ConfigureAnimalCare.EXTRA_ANIMAL_CARE_CONFIG, animalCareConfiguration)
            }
    }

    private val binding by lazy {
        ActivityAnimalCareConfigurationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            setupAnimalCareSelection()
        }
        binding.buttonDone.setOnClickListener {
            finishWithResult()
        }
    }

    private fun setupAnimalCareSelection() {
        val animalCareConfiguration = intent?.getParcelableExtra(
            ConfigureAnimalCare.EXTRA_ANIMAL_CARE_CONFIG
        ) ?: AnimalCareConfiguration()
        binding.checkBoxHooves.isChecked = animalCareConfiguration.hooves
        binding.checkBoxHorns.isChecked = animalCareConfiguration.horns
        binding.checkBoxShoe.isChecked = animalCareConfiguration.shoe
        binding.checkBoxShear.isChecked = animalCareConfiguration.shear
        binding.checkBoxWean.isChecked = animalCareConfiguration.wean
        binding.checkBoxWeigh.isChecked = animalCareConfiguration.weight
    }

    private fun finishWithResult() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(
                ConfigureAnimalCare.EXTRA_ANIMAL_CARE_CONFIG,
                AnimalCareConfiguration(
                    hooves = binding.checkBoxHooves.isChecked,
                    horns = binding.checkBoxHorns.isChecked,
                    shoe = binding.checkBoxShoe.isChecked,
                    shear = binding.checkBoxShear.isChecked,
                    wean = binding.checkBoxWean.isChecked,
                    weight = binding.checkBoxWeigh.isChecked
                )
            )
        })
        finish()
    }
}
