package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.info.AnimalBasicInfoPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview.OutlineDividerDecoration
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivitySelectAnimalBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalBasicInfoBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.SexStandard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectAnimalActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        @JvmOverloads
        fun newIntent(context: Context, sexStandard: SexStandard? = null) =
            Intent(context, SelectAnimalActivity::class.java).apply {
                if (sexStandard != null) {
                    putExtra(
                        SelectAnimal.EXTRA_SEX_STANDARD,
                        sexStandard as Parcelable
                    )
                }
            }
    }

    private val viewModel: SelectAnimalViewModel
        by viewModels<SelectAnimalViewModel> {
            ViewModelProviderFactory(
                this@SelectAnimalActivity,
                intent?.extras?.getParcelable(SelectAnimal.EXTRA_SEX_STANDARD)
            )
        }

    private val binding: ActivitySelectAnimalBinding by lazy {
        ActivitySelectAnimalBinding.inflate(layoutInflater)
    }

    private lateinit var idTypeSelectionPresenter: ItemSelectionPresenter<IdType>
    private lateinit var animalInfoAdapter: AnimalBasicInfoAdapter

    private val requiredPermissionsWatcher = RequiredPermissionsWatcher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            binding.inputAnimalSearch.requestFocus()
        }
        supportActionBar?.apply {
            setTitle(R.string.title_select_animal)
            setDisplayHomeAsUpEnabled(true)
        }
        idTypeSelectionPresenter = idTypeSelectionForSearchPresenter(
            button = binding.spinnerIdType,
            onItemSelected = { idType ->
                viewModel.updateSearchIdType(idType)
            }
        )
        binding.inputAnimalSearch.addTextChangedListener {
            viewModel.updateSearchTerm(it.toString())
        }
        with(binding.recyclerAnimals) {
            adapter = AnimalBasicInfoAdapter(::onItemClicked).also { animalInfoAdapter = it }
            layoutManager = LinearLayoutManager(
                this@SelectAnimalActivity,
                RecyclerView.VERTICAL,
                false
            )
            itemAnimator = null
            addItemDecoration(
                OutlineDividerDecoration(this@SelectAnimalActivity)
            )
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collectLatest(::renderViewState)
            }
        }
        observeErrorReports(viewModel.errorReportFlow)
        lifecycle.addObserver(requiredPermissionsWatcher)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onItemClicked(item: AnimalBasicInfo) {
        val data = Intent().putExtra(SelectAnimal.EXTRA_SELECTED_ANIMAL_ID, item.id)
        setResult(RESULT_OK, data)
        finish()
    }

    private fun renderViewState(viewState: SelectAnimalViewModel.ViewState) {
        binding.progressSpinnerLoading.isVisible = viewState.isLoading
        idTypeSelectionPresenter.displaySelectedItem(viewState.idType)
        animalInfoAdapter.submitList(viewState.animalInfo) {
            binding.recyclerAnimals.isGone = viewState.animalInfo.isNullOrEmpty()
            binding.textNoResults.isVisible = viewState.animalInfo?.isEmpty() == true
        }
    }
}

private class ViewModelProviderFactory(
    context: Context, private val sexStandard: SexStandard?
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(
        modelClass: Class<VM>
    ): VM = when (modelClass) {
        SelectAnimalViewModel::class.java -> {
            val databaseHandler = DatabaseManager.getInstance(appContext)
                .createDatabaseHandler()
            val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                databaseHandler, ActiveDefaultSettings.from(appContext)
            )
            SelectAnimalViewModel(
                databaseHandler,
                sexStandard,
                AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo),
                IdTypeRepositoryImpl(databaseHandler),
                LoadActiveDefaultSettings(
                    ActiveDefaultSettings(
                        PreferenceManager.getDefaultSharedPreferences(appContext)
                    ),
                    defaultSettingsRepo
                )
            ) as VM
        }
        else -> throw IllegalArgumentException(
            "Cannot create view model of type ${modelClass.simpleName}"
        )
    }
}

private class AnimalBasicInfoAdapter(
    private val onItemClicked: (AnimalBasicInfo) -> Unit
) : ListAdapter<AnimalBasicInfo, ViewHolder>(AnimalBasicInfoDiff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAnimalBasicInfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClicked
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}

private class ViewHolder(
    private val binding: ItemAnimalBasicInfoBinding,
    private val onItemClicked: (AnimalBasicInfo) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val animalBasicInfoPresenter = AnimalBasicInfoPresenter(binding.animalBasicInfo)

    fun bind(item: AnimalBasicInfo) {
        binding.root.setOnClickListener { onItemClicked.invoke(item) }
        animalBasicInfoPresenter.animalBasicInfo = item
    }
}

private object AnimalBasicInfoDiff: DiffUtil.ItemCallback<AnimalBasicInfo>() {
    override fun areItemsTheSame(oldItem: AnimalBasicInfo, newItem: AnimalBasicInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AnimalBasicInfo, newItem: AnimalBasicInfo): Boolean {
        return oldItem == newItem
    }
}
