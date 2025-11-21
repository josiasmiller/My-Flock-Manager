package com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAddAnimalAlertBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AddAnimalAlertActivity : AppCompatActivity() {

    //This activity is super simple, so we are not
    //bothering with a view model here.

    companion object {
        fun newIntent(context: Context, animalId: EntityId, animalName: String) =
            Intent(context, AddAnimalAlertActivity::class.java).apply {
                putExtra(AddAnimalAlert.EXTRA_ANIMAL_ID, animalId)
                putExtra(AddAnimalAlert.EXTRA_ANIMAL_NAME, animalName)
            }
    }

    private val binding by lazy {
        ActivityAddAnimalAlertBinding.inflate(layoutInflater)
    }

    private val animalId: EntityId by lazy {
        requireNotNull(intent).getParcelableExtra<EntityId>(AddAnimalAlert.EXTRA_ANIMAL_ID)
            ?: throw IllegalArgumentException("No animal id specified.")
    }

    private val animalName by lazy {
        requireNotNull(intent).getStringExtra(AddAnimalAlert.EXTRA_ANIMAL_NAME)
    }

    private val alertText = MutableStateFlow("")
    private val isUpdatingDatabase = MutableStateFlow(false)
    private val canUpdateDatabase = combine(alertText, isUpdatingDatabase) { alertText, isUpdatingDatabase ->
        alertText.isNotBlank() && !isUpdatingDatabase
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            binding.inputAnimalAlert.requestFocus()
        }
        title = "$title - $animalName"
        binding.buttonPanelTop.show(TopButtonBar.UI_ACTION_UPDATE_DATABASE)
        binding.buttonPanelTop.mainActionButton.setOnClickListener { updateDatabase() }
        binding.inputAnimalAlert.addTextChangedListener { alertText.value = it.toString() }
        collectLatestOnStart(canUpdateDatabase) { binding.buttonPanelTop.mainActionButton.isEnabled = it }
    }

    private fun updateDatabase() {
        lifecycleScope.launch {
            isUpdatingDatabase.value = true
            try {
                DatabaseManager.getInstance(this@AddAnimalAlertActivity)
                    .createDatabaseHandler().use { databaseHandler ->
                        val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                            databaseHandler, ActiveDefaultSettings.from(this@AddAnimalAlertActivity)
                        )
                        AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo).run {
                            addAlertForAnimal(
                                animalId,
                                binding.inputAnimalAlert.text.toString().trim(),
                                LocalDateTime.now())
                        }
                    }
                setResult(
                    Activity.RESULT_OK,
                    Intent().apply {
                        putExtra(
                            AddAnimalAlert.EXTRA_ANIMAL_ID,
                            animalId
                        )
                    }
                )
                finish()
            } catch(ex: Exception) {
                Toast.makeText(
                    this@AddAnimalAlertActivity,
                    R.string.text_add_animal_alert_failed,
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isUpdatingDatabase.value = false
            }
        }
    }
}
