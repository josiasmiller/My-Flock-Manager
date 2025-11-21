package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.combine22
import com.weyr_associates.animaltrakkerfarmmobile.app.model.summarizeForErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.EvaluationRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultUserInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitConfig
import com.weyr_associates.animaltrakkerfarmmobile.model.EvaluationConfiguration
import com.weyr_associates.animaltrakkerfarmmobile.model.Trait
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class ConfigureEvaluationViewModel(
    private val evaluationRepo: EvaluationRepository,
    private val loadDefaultUserInfo: LoadDefaultUserInfo
) : ViewModel() {

    sealed interface Event

    data object EvaluationNameChanged : Event
    data object UpdateDatabaseSuccess : Event

    private val _name = MutableStateFlow("")

    var name: String
        get() = _name.value
        set(value) { _name.update { value } }

    private val _summarizeInAlert = MutableStateFlow(false)
    val summarizeInAlert = _summarizeInAlert.asStateFlow()

    private val _trait01 = MutableStateFlow(EvalTraitConfig())
    private val _trait02 = MutableStateFlow(EvalTraitConfig())
    private val _trait03 = MutableStateFlow(EvalTraitConfig())
    private val _trait04 = MutableStateFlow(EvalTraitConfig())
    private val _trait05 = MutableStateFlow(EvalTraitConfig())
    private val _trait06 = MutableStateFlow(EvalTraitConfig())
    private val _trait07 = MutableStateFlow(EvalTraitConfig())
    private val _trait08 = MutableStateFlow(EvalTraitConfig())
    private val _trait09 = MutableStateFlow(EvalTraitConfig())
    private val _trait10 = MutableStateFlow(EvalTraitConfig())
    private val _trait11 = MutableStateFlow(EvalTraitConfig())
    private val _trait12 = MutableStateFlow(EvalTraitConfig())
    private val _trait13 = MutableStateFlow(EvalTraitConfig())
    private val _trait14 = MutableStateFlow(EvalTraitConfig())
    private val _trait15 = MutableStateFlow(EvalTraitConfig())
    private val _trait16 = MutableStateFlow(EvalTraitConfig())
    private val _trait17 = MutableStateFlow(EvalTraitConfig())
    private val _trait18 = MutableStateFlow(EvalTraitConfig())
    private val _trait19 = MutableStateFlow(EvalTraitConfig())
    private val _trait20 = MutableStateFlow(EvalTraitConfig())

    val trait01 = _trait01.asStateFlow()
    val trait02 = _trait02.asStateFlow()
    val trait03 = _trait03.asStateFlow()
    val trait04 = _trait04.asStateFlow()
    val trait05 = _trait05.asStateFlow()
    val trait06 = _trait06.asStateFlow()
    val trait07 = _trait07.asStateFlow()
    val trait08 = _trait08.asStateFlow()
    val trait09 = _trait09.asStateFlow()
    val trait10 = _trait10.asStateFlow()
    val trait11 = _trait11.asStateFlow()
    val trait12 = _trait12.asStateFlow()
    val trait13 = _trait13.asStateFlow()
    val trait14 = _trait14.asStateFlow()
    val trait15 = _trait15.asStateFlow()
    val trait16 = _trait16.asStateFlow()
    val trait17 = _trait17.asStateFlow()
    val trait18 = _trait18.asStateFlow()
    val trait19 = _trait19.asStateFlow()
    val trait20 = _trait20.asStateFlow()

    private val configuration = combine22(
        _name, summarizeInAlert,
        trait01, trait02, trait03, trait04, trait05,
        trait06, trait07, trait08, trait09, trait10,
        trait11, trait12, trait13, trait14, trait15,
        trait16, trait17, trait18, trait19, trait20,
    ) { configName, shouldSummarizeInAlert,
        config01, config02, config03, config04, config05,
        config06, config07, config08, config09, config10,
        config11, config12, config13, config14, config15,
        config16, config17, config18, config19, config20 ->
        EvaluationConfiguration(
            name = configName,
            saveSummaryAsAlert = shouldSummarizeInAlert,
            trait01 = config01, trait02 = config02, trait03 = config03, trait04 = config04,
            trait05 = config05, trait06 = config06, trait07 = config07, trait08 = config08,
            trait09 = config09, trait10 = config10, trait11 = config11, trait12 = config12,
            trait13 = config13, trait14 = config14, trait15 = config15, trait16 = config16,
            trait17 = config17, trait18 = config18, trait19 = config19, trait20 = config20,
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, EvaluationConfiguration())

    private val _isUpdatingDatabase = MutableStateFlow(false)

    val canSaveToDatabase = combine(
        _isUpdatingDatabase,
        configuration.map { evalConfiguration ->
            evalConfiguration.isConfigurationComplete
        }
    ) { isUpdatingDatabase, isConfigurationComplete ->
        !isUpdatingDatabase && isConfigurationComplete
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canClearData = configuration.map { evalConfiguration ->
        evalConfiguration.isConfigurationStarted
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    fun updateSummarizeInAlert(shouldSummarize: Boolean) {
        _summarizeInAlert.update { shouldSummarize }
    }

    //Trait01
    
    fun updateTrait01(trait: Trait?) {
        _trait01.update { it.copy(trait = trait) }
    }

    fun updateTrait01Optional(optional: Boolean) {
        _trait01.update { it.copy(isOptional = optional) }
    }

    fun updateTrait01Deferred(deferred: Boolean) {
        _trait01.update { it.copy(isDeferred = deferred) }
    }

    //Trait02

    fun updateTrait02(trait: Trait?) {
        _trait02.update { it.copy(trait = trait) }
    }

    fun updateTrait02Optional(optional: Boolean) {
        _trait02.update { it.copy(isOptional = optional) }
    }

    fun updateTrait02Deferred(deferred: Boolean) {
        _trait02.update { it.copy(isDeferred = deferred) }
    }

    //Trait03

    fun updateTrait03(trait: Trait?) {
        _trait03.update { it.copy(trait = trait) }
    }

    fun updateTrait03Optional(optional: Boolean) {
        _trait03.update { it.copy(isOptional = optional) }
    }

    fun updateTrait03Deferred(deferred: Boolean) {
        _trait03.update { it.copy(isDeferred = deferred) }
    }

    //Trait04

    fun updateTrait04(trait: Trait?) {
        _trait04.update { it.copy(trait = trait) }
    }

    fun updateTrait04Optional(optional: Boolean) {
        _trait04.update { it.copy(isOptional = optional) }
    }

    fun updateTrait04Deferred(deferred: Boolean) {
        _trait04.update { it.copy(isDeferred = deferred) }
    }

    //Trait05

    fun updateTrait05(trait: Trait?) {
        _trait05.update { it.copy(trait = trait) }
    }

    fun updateTrait05Optional(optional: Boolean) {
        _trait05.update { it.copy(isOptional = optional) }
    }

    fun updateTrait05Deferred(deferred: Boolean) {
        _trait05.update { it.copy(isDeferred = deferred) }
    }

    //Trait06

    fun updateTrait06(trait: Trait?) {
        _trait06.update { it.copy(trait = trait) }
    }

    fun updateTrait06Optional(optional: Boolean) {
        _trait06.update { it.copy(isOptional = optional) }
    }

    fun updateTrait06Deferred(deferred: Boolean) {
        _trait06.update { it.copy(isDeferred = deferred) }
    }

    //Trait07

    fun updateTrait07(trait: Trait?) {
        _trait07.update { it.copy(trait = trait) }
    }

    fun updateTrait07Optional(optional: Boolean) {
        _trait07.update { it.copy(isOptional = optional) }
    }

    fun updateTrait07Deferred(deferred: Boolean) {
        _trait07.update { it.copy(isDeferred = deferred) }
    }

    //Trait08

    fun updateTrait08(trait: Trait?) {
        _trait08.update { it.copy(trait = trait) }
    }

    fun updateTrait08Optional(optional: Boolean) {
        _trait08.update { it.copy(isOptional = optional) }
    }

    fun updateTrait08Deferred(deferred: Boolean) {
        _trait08.update { it.copy(isDeferred = deferred) }
    }

    //Trait09

    fun updateTrait09(trait: Trait?) {
        _trait09.update { it.copy(trait = trait) }
    }

    fun updateTrait09Optional(optional: Boolean) {
        _trait09.update { it.copy(isOptional = optional) }
    }

    fun updateTrait09Deferred(deferred: Boolean) {
        _trait09.update { it.copy(isDeferred = deferred) }
    }

    //Trait10

    fun updateTrait10(trait: Trait?) {
        _trait10.update { it.copy(trait = trait) }
    }

    fun updateTrait10Optional(optional: Boolean) {
        _trait10.update { it.copy(isOptional = optional) }
    }

    fun updateTrait10Deferred(deferred: Boolean) {
        _trait10.update { it.copy(isDeferred = deferred) }
    }

    //Trait11

    fun updateTrait11(trait: Trait?) {
        _trait11.update { it.copy(trait = trait, units = null) }
    }

    fun updateUnits11(units: UnitOfMeasure?) {
        _trait11.update { it.copy(units = units) }
    }

    fun updateTrait11Optional(optional: Boolean) {
        _trait11.update { it.copy(isOptional = optional) }
    }

    fun updateTrait11Deferred(deferred: Boolean) {
        _trait11.update { it.copy(isDeferred = deferred) }
    }

    //Trait12

    fun updateTrait12(trait: Trait?) {
        _trait12.update { it.copy(trait = trait, units = null) }
    }

    fun updateUnits12(units: UnitOfMeasure?) {
        _trait12.update { it.copy(units = units) }
    }

    fun updateTrait12Optional(optional: Boolean) {
        _trait12.update { it.copy(isOptional = optional) }
    }

    fun updateTrait12Deferred(deferred: Boolean) {
        _trait12.update { it.copy(isDeferred = deferred) }
    }

    //Trait13

    fun updateTrait13(trait: Trait?) {
        _trait13.update { it.copy(trait = trait, units = null) }
    }

    fun updateUnits13(units: UnitOfMeasure?) {
        _trait13.update { it.copy(units = units) }
    }

    fun updateTrait13Optional(optional: Boolean) {
        _trait13.update { it.copy(isOptional = optional) }
    }

    fun updateTrait13Deferred(deferred: Boolean) {
        _trait13.update { it.copy(isDeferred = deferred) }
    }

    //Trait14

    fun updateTrait14(trait: Trait?) {
        _trait14.update { it.copy(trait = trait, units = null) }
    }

    fun updateUnits14(units: UnitOfMeasure?) {
        _trait14.update { it.copy(units = units) }
    }

    fun updateTrait14Optional(optional: Boolean) {
        _trait14.update { it.copy(isOptional = optional) }
    }

    fun updateTrait14Deferred(deferred: Boolean) {
        _trait14.update { it.copy(isDeferred = deferred) }
    }

    //Trait15

    fun updateTrait15(trait: Trait?) {
        _trait15.update { it.copy(trait = trait, units = null) }
    }

    fun updateUnits15(units: UnitOfMeasure?) {
        _trait15.update { it.copy(units = units) }
    }

    fun updateTrait15Optional(optional: Boolean) {
        _trait15.update { it.copy(isOptional = optional) }
    }

    fun updateTrait15Deferred(deferred: Boolean) {
        _trait15.update { it.copy(isDeferred = deferred) }
    }

    //Trait16

    fun updateTrait16(trait: Trait?) {
        _trait16.update { it.copy(trait = trait) }
    }

    fun updateTrait16Optional(optional: Boolean) {
        _trait16.update { it.copy(isOptional = optional) }
    }

    fun updateTrait16Deferred(deferred: Boolean) {
        _trait16.update { it.copy(isDeferred = deferred) }
    }

    //Trait17

    fun updateTrait17(trait: Trait?) {
        _trait17.update { it.copy(trait = trait) }
    }

    fun updateTrait17Optional(optional: Boolean) {
        _trait17.update { it.copy(isOptional = optional) }
    }

    fun updateTrait17Deferred(deferred: Boolean) {
        _trait17.update { it.copy(isDeferred = deferred) }
    }

    //Trait18

    fun updateTrait18(trait: Trait?) {
        _trait18.update { it.copy(trait = trait) }
    }

    fun updateTrait18Optional(optional: Boolean) {
        _trait18.update { it.copy(isOptional = optional) }
    }

    fun updateTrait18Deferred(deferred: Boolean) {
        _trait18.update { it.copy(isDeferred = deferred) }
    }

    //Trait19

    fun updateTrait19(trait: Trait?) {
        _trait19.update { it.copy(trait = trait) }
    }

    fun updateTrait19Optional(optional: Boolean) {
        _trait19.update { it.copy(isOptional = optional) }
    }

    fun updateTrait19Deferred(deferred: Boolean) {
        _trait19.update { it.copy(isDeferred = deferred) }
    }

    //Trait20

    fun updateTrait20(trait: Trait?) {
        _trait20.update { it.copy(trait = trait) }
    }

    fun updateTrait20Optional(optional: Boolean) {
        _trait20.update { it.copy(isOptional = optional) }
    }

    fun updateTrait20Deferred(deferred: Boolean) {
        _trait20.update { it.copy(isDeferred = deferred) }
    }

    fun saveToDatabase() {
        if (canSaveToDatabase.value) {
            viewModelScope.launch {
                executeSaveToDatabase()
            }
        }
    }

    fun clearData() {
        if (canClearData.value) {
            executeClearData()
        }
    }

    private fun executeClearData() {

        name = ""

        _summarizeInAlert.update { false }

        _trait01.update { EvalTraitConfig() }
        _trait02.update { EvalTraitConfig() }
        _trait03.update { EvalTraitConfig() }
        _trait04.update { EvalTraitConfig() }
        _trait05.update { EvalTraitConfig() }
        _trait06.update { EvalTraitConfig() }
        _trait07.update { EvalTraitConfig() }
        _trait08.update { EvalTraitConfig() }
        _trait09.update { EvalTraitConfig() }
        _trait10.update { EvalTraitConfig() }
        _trait11.update { EvalTraitConfig() }
        _trait12.update { EvalTraitConfig() }
        _trait13.update { EvalTraitConfig() }
        _trait14.update { EvalTraitConfig() }
        _trait15.update { EvalTraitConfig() }
        _trait16.update { EvalTraitConfig() }
        _trait17.update { EvalTraitConfig() }
        _trait18.update { EvalTraitConfig() }
        _trait19.update { EvalTraitConfig() }
        _trait20.update { EvalTraitConfig() }

        viewModelScope.launch {
            eventsChannel.send(EvaluationNameChanged)
        }
    }

    private suspend fun executeSaveToDatabase() {

        val configuration = configuration.value

        if (!configuration.isConfigurationComplete) {
            return
        }

        try {

            _isUpdatingDatabase.update { true }

            withContext(Dispatchers.IO) {
                val userInfo = loadDefaultUserInfo.invoke()
                evaluationRepo.saveEvaluationConfigurationForUser(
                    userInfo.userId,
                    userInfo.userType,
                    configuration,
                    LocalDateTime.now()
                )
            }
            executeClearData()
            eventsChannel.send(UpdateDatabaseSuccess)
        } catch (ex: Exception) {
            errorReportChannel.send(
                ErrorReport(
                    action = "Configure Saved Evaluation",
                    summary = configuration.summarizeForErrorReport(),
                    error = ex
                )
            )
        } finally {
            _isUpdatingDatabase.update { false }
        }
    }
}
