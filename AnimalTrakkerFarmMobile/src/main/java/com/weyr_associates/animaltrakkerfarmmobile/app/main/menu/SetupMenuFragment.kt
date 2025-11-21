package com.weyr_associates.animaltrakkerfarmmobile.app.main.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.checkDatabaseValidityThen
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.selectedItem
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.ErrorReportDialog
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectDefaultSettingsDialogFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettingsInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.SaveActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentMenuSetupBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SetupMenuFragment : Fragment(R.layout.fragment_menu_setup) {

    //If this screen becomes more complicated, move to a
    //view model, but until then, these one off actions are here.

    companion object {
        private const val TAG_FRAGMENT_SELECT_DEFAULT_SETTINGS = "TAG_FRAGMENT_SELECT_DEFAULT_SETTINGS"
    }

    private val binding by viewBinding<FragmentMenuSetupBinding>()
    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var loadActiveDefaultSettingsInfo: LoadActiveDefaultSettingsInfo
    private lateinit var saveActiveDefaultSettings: SaveActiveDefaultSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseHandler = DatabaseManager.getInstance(requireContext())
            .createDatabaseHandler()
        val activeDefaultSettings = ActiveDefaultSettings(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
        loadActiveDefaultSettingsInfo = LoadActiveDefaultSettingsInfo(
            activeDefaultSettings,
            DefaultSettingsRepositoryImpl(
                databaseHandler, ActiveDefaultSettings.from(requireContext())
            )
        )
        saveActiveDefaultSettings = SaveActiveDefaultSettings(
            activeDefaultSettings
        )
        setupActiveDefaultsSelectionListener()
        setupActiveDefaultsInfoRefresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            btnSelectDefaults.setOnClickListener {
                checkDatabaseValidityThen {
                    SelectDefaultSettingsDialogFragment.newInstance()
                        .show(childFragmentManager, TAG_FRAGMENT_SELECT_DEFAULT_SETTINGS)
                }
            }
            btnManageDrugs.setOnClickListener {
                findNavController().navigate(R.id.nav_dst_menu_setup_drugs)
            }
            listOf(
                btnAddContacts,
                btnDefineNotes,
                btnCreateDefaults,
                btnAddVeterinarian,
                btnAddLaboratory
            ).forEach { it.deactivate() }
        }
    }

    override fun onDestroy() {
        databaseHandler.close()
        super.onDestroy()
    }

    private fun setupActiveDefaultsSelectionListener() {
        childFragmentManager.setFragmentResultListener(
            SelectDefaultSettingsDialogFragment.REQUEST_KEY_SELECT_DEFAULT_SETTINGS,
            this,
        ) { _, data -> onDefaultSettingsSelected(data.selectedItem<ItemEntry>().id)}
    }

    private fun setupActiveDefaultsInfoRefresh() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                refreshDefaultsInfo()
            }
        }
    }

    private fun onDefaultSettingsSelected(defaultSettingsId: EntityId) {
        try {
            saveActiveDefaultSettings(defaultSettingsId)
            lifecycleScope.launch { refreshDefaultsInfo() }
        } catch(ex: Exception) {
            ErrorReportDialog.show(requireContext(), ErrorReport(
                action = "Save Default Settings",
                summary = "defaultSettingsId=${defaultSettingsId}",
                error = ex
            ))
        }
    }

    private suspend fun refreshDefaultsInfo() {
        try {
            val activeDefSettingsInfo = loadActiveDefaultSettingsInfo()
            binding.textActiveDefaults.text = getString(
                R.string.text_active_defaults_format,
                activeDefSettingsInfo.name
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            binding.textActiveDefaults.text = getString(
                R.string.text_no_active_defaults_found
            )
        }
    }
}
