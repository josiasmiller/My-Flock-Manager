package com.weyr_associates.animaltrakkerfarmmobile.app.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.awaitAll
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.channel.sendIn
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.mapToResult
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.SexStandard
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectAnimalViewModel(
    private val databaseHandler: DatabaseHandler,
    private val sexStandard: SexStandard?,
    private val animalRepository: AnimalRepository,
    private val idTypesRepository: IdTypeRepository,
    private val loadDefaultSettings: LoadActiveDefaultSettings
) : ViewModel() {

    companion object {

        //NOTE: If these values change, update the error report
        //summary for when loading these values from settings fails.
        private val ID_SPECIES_DEFAULT = Species.ID_SHEEP
        private val ID_TYPE_DEFAULT = IdType.ID_TYPE_ID_NAME

        private val ID_TYPE_PLACEHOLDER = IdType(
            id = ID_TYPE_DEFAULT,
            name = "",
            abbreviation = "",
            order = 0
        )
        //TODO: Discuss this value. Is it appropriate to make it larger?
        private const val MINIMUM_SEARCH_TERM_LENGTH = 1

        private val ERROR_REPORT_ACTION_LOAD_DEFAULTS = "Load Defaults"
        private val ERROR_REPORT_SUMMARY_LOAD_DEFAULTS by lazy {
            "Failed to load default species and default id type. Continuing with species id $ID_SPECIES_DEFAULT and id type id $ID_TYPE_DEFAULT."
        }
    }

    data class ViewState(
        val isLoading: Boolean = false,
        val searchTerm: String = "",
        val idType: IdType = ID_TYPE_PLACEHOLDER,
        val animalInfo: List<AnimalBasicInfo>? = null
    )

    private data class Defaults(
        val speciesId: EntityId,
        val defaultIdType: IdType
    )

    private data class Search(
        val searchTerm: String,
        val idType: IdType,
        val speciesId: EntityId,
    )

    private data class SearchResults(
        val search: Search,
        val animalInfo: List<AnimalBasicInfo>?
    )

    private data class SearchError(
        val search: Search,
        val error: Throwable
    )

    private val defaultsFlow: Flow<Defaults> = flow { emit(loadDefaults()) }
        .catch {
            notifyOfError(
                ErrorReport(
                    action = ERROR_REPORT_ACTION_LOAD_DEFAULTS,
                    summary = ERROR_REPORT_SUMMARY_LOAD_DEFAULTS,
                    it
                )
            )
            emit(Defaults(ID_SPECIES_DEFAULT, ID_TYPE_PLACEHOLDER))
        }
        .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    private val isLoadingDefaults = MutableStateFlow(false)
    private val isLoadingSearchResults = MutableStateFlow(false)

    private val searchTermChannel = Channel<String>()
    private val searchTermFlow = searchTermChannel.receiveAsFlow()
        .onStart { emit("") }
        .debounce(500L)
        .shareIn(viewModelScope, SharingStarted.Lazily)

    private val searchIdTypeChannel = Channel<IdType>()
    private val searchIdTypeFlow = searchIdTypeChannel.receiveAsFlow()
        .onStart { emit(defaultsFlow.map { it.defaultIdType }.first()) }
        .shareIn(viewModelScope, SharingStarted.Lazily)

    private val isLoadingFlow = combine(
        isLoadingDefaults,
        isLoadingSearchResults
    ) { isLoadingDefaults, isLoadingSearchResults ->
        isLoadingDefaults || isLoadingSearchResults
    }

    private val searchFlow = combine(
        defaultsFlow,
        searchTermFlow,
        searchIdTypeFlow
    ) { defaults, term, idType ->
        Search(term, idType, defaults.speciesId)
    }

    private val searchResultsFlow: Flow<Result<SearchResults, SearchError>> = searchFlow.flatMapLatest { search ->
        performSearch(search).mapToResult { SearchError(search, it) }
    }

    private val searchErrorFlow: Flow<ErrorReport> =
        searchResultsFlow.filterIsInstance<Result.Failure<SearchResults, SearchError>>()
            .map { createSearchErrorReport(it.error) }

    private val animalInfoFlow = searchResultsFlow.map {
        when (it) {
            is Result.Success -> it.data.animalInfo
            else -> null
        }
    }

    val viewStateFlow: StateFlow<ViewState> = combine(
        isLoadingFlow, searchTermFlow, searchIdTypeFlow, animalInfoFlow
    ) { isLoading, searchTerm, searchIdType, animalInfo ->
        ViewState(
            isLoading = isLoading,
            searchTerm = searchTerm,
            idType = searchIdType,
            animalInfo = animalInfo
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, ViewState(isLoading = true))

    private val _errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = _errorReportChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            searchErrorFlow.collectLatest { notifyOfError(it) }
        }
    }

    fun updateSearchTerm(term: String) {
        searchTermChannel.sendIn(viewModelScope, term)
    }

    fun updateSearchIdType(idType: IdType) {
        searchIdTypeChannel.sendIn(viewModelScope, idType)
    }

    override fun onCleared() {
        super.onCleared()
        databaseHandler.close()
    }

    private suspend fun loadDefaults(): Defaults {

        isLoadingDefaults.update { true }

        //TODO: Remove explicit dispatcher here once underlying repositories use suspend and are made main safe.
        val defaultSettingsAsync = viewModelScope.async(Dispatchers.IO) {
            loadDefaultSettings()
        }
        //TODO: Remove explicit dispatcher here once underlying repositories use suspend and are made main safe.
        val defaultIdTypeAsync = viewModelScope.async(Dispatchers.IO) {
            idTypesRepository.queryForIdType(ID_TYPE_DEFAULT)
        }
        val defaults = awaitAll(defaultSettingsAsync, defaultIdTypeAsync) { defaultSettings, defaultIdType ->
            Defaults(defaultSettings.speciesId, requireNotNull(defaultIdType))
        }

        isLoadingDefaults.update { false }

        return defaults
    }

    private fun performSearch(search: Search): Flow<SearchResults> = flow {
        try {
            isLoadingSearchResults.update { true }
            if (search.searchTerm.length < MINIMUM_SEARCH_TERM_LENGTH) {
                emit(SearchResults(search, null))
            } else {
                val results = when (search.idType.id) {
                    IdType.ID_TYPE_ID_NAME -> withContext(Dispatchers.IO) {
                        animalRepository.searchAnimalsByName(
                            partialName = search.searchTerm,
                            speciesId = search.speciesId,
                            sexStandard = sexStandard
                        )
                    }

                    else -> animalRepository.searchAnimalsByIdType(
                        partialId = search.searchTerm,
                        idTypeId = search.idType.id,
                        speciesId = search.speciesId,
                        sexStandard = sexStandard
                    )
                }
                emit(SearchResults(search, results))
            }
        } finally {
            isLoadingSearchResults.update { false }
        }
    }

    private fun createSearchErrorReport(searchError: SearchError): ErrorReport {
        val search = searchError.search
        val summary = """
            |The search for an animal failed.
            |searchTerm=${search.searchTerm}, speciesId=${search.speciesId}, idTypeId=${search.idType.id}
            """.trimMargin()
        return ErrorReport(
            action = "Lookup Animal",
            summary = summary,
            error = searchError.error
        )
    }

    private suspend fun notifyOfError(errorReport: ErrorReport) {
        _errorReportChannel.send(errorReport)
    }
}
