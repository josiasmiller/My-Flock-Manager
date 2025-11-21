package com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.AnimalInfoState
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.Lookup
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

class AnimalInfoLookup(
    private val coroutineScope: CoroutineScope,
    private val animalRepo: AnimalRepository
) : LookupAnimalInfo {

    private val loadOccurrence: AtomicInteger = AtomicInteger(0)

    private data object ResetAnimalInfo

    private val onLoadAnimalInfoChannel = Channel<AnimalBasicInfo>()
    private val onLookupAnimalChannel = Channel<Lookup>()
    private val onResetAnimalChannel = Channel<ResetAnimalInfo>()

    override val animalInfoState: StateFlow<AnimalInfoState> = merge(
        onLoadAnimalInfoChannel.receiveAsFlow(),
        onLookupAnimalChannel.receiveAsFlow(),
        onResetAnimalChannel.receiveAsFlow()
    ).flatMapLatest { event ->
        when (event) {
            is AnimalBasicInfo -> {
                val animalBasicInfo: AnimalBasicInfo = event
                flowOf(
                    AnimalInfoState.Loaded(
                        animalBasicInfo,
                        null,
                        loadOccurrence.incrementAndGet()
                    )
                )
            }
            is Lookup -> {
                val lookup: Lookup = event
                val (animalInfoFlow, scannedEID) = when (lookup) {
                    is Lookup.ByAnimalId -> {
                        Pair(animalRepo.animalBasicInfoByAnimalId(lookup.animalId), null)
                    }

                    is Lookup.ByScannedEID -> {
                        Pair(animalRepo.animalBasicInfoByEID(lookup.eidNumber), lookup.eidNumber)
                    }
                }
                animalInfoFlow.map { animalInfo ->
                    animalInfo?.let {
                        val nowDate = LocalDate.now()
                        val prunedAlerts = animalRepo.queryAnimalAlerts(it.id)
                            .filter { alert ->
                                alert !is DrugWithdrawalAlert ||
                                        alert.drugWithdrawal.withdrawalDate.isAfter(nowDate)
                            }
                        AnimalInfoState.Loaded(
                            it.copy(alerts = prunedAlerts),
                            scannedEID,
                            loadOccurrence.incrementAndGet()
                        )
                    } ?: AnimalInfoState.NotFound(lookup)
                }.flowOn(Dispatchers.IO)
            }
            else -> flowOf(AnimalInfoState.Initial)
        }
    }.stateIn(coroutineScope, SharingStarted.Lazily, AnimalInfoState.Initial)

    fun loadAnimalInfo(animalBasicInfo: AnimalBasicInfo) {
        coroutineScope.launch {
            onLoadAnimalInfoChannel.send(animalBasicInfo)
        }
    }

    override fun lookupAnimalInfoById(animalId: EntityId) {
        coroutineScope.launch {
            onLookupAnimalChannel.send(Lookup.ByAnimalId(animalId))
        }
    }

    override fun lookupAnimalInfoByEIDNumber(eidNumber: String) {
        coroutineScope.launch {
            onLookupAnimalChannel.send(Lookup.ByScannedEID(eidNumber))
        }
    }

    override fun resetAnimalInfo() {
        coroutineScope.launch {
            onResetAnimalChannel.send(ResetAnimalInfo)
        }
    }
}
