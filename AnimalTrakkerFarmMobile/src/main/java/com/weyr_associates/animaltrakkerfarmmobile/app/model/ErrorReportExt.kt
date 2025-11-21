package com.weyr_associates.animaltrakkerfarmmobile.app.model

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.ActionSet
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug.DrugAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationEntries
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawalSpec
import com.weyr_associates.animaltrakkerfarmmobile.model.EvaluationConfiguration
import com.weyr_associates.animaltrakkerfarmmobile.model.HoofCheck
import com.weyr_associates.animaltrakkerfarmmobile.model.HornCheck
import com.weyr_associates.animaltrakkerfarmmobile.model.OffLabelDrugSpec

fun EvaluationConfiguration.summarizeForErrorReport(): String {
    return buildString {
        append("name=${name}, ")
        append("saveSummaryAsAlert=${saveSummaryAsAlert}, ")
        append("trait01={traitTypeId=${trait01.trait?.typeId}, traitUnitsId=${trait01.units?.id}}, isOptional=${trait01.isOptional}, isDeferred=${trait01.isDeferred}}, ")
        append("trait02={traitTypeId=${trait02.trait?.typeId}, traitUnitsId=${trait02.units?.id}}, isOptional=${trait02.isOptional}, isDeferred=${trait02.isDeferred}}, ")
        append("trait03={traitTypeId=${trait03.trait?.typeId}, traitUnitsId=${trait03.units?.id}}, isOptional=${trait03.isOptional}, isDeferred=${trait03.isDeferred}}, ")
        append("trait04={traitTypeId=${trait04.trait?.typeId}, traitUnitsId=${trait04.units?.id}}, isOptional=${trait04.isOptional}, isDeferred=${trait04.isDeferred}}, ")
        append("trait05={traitTypeId=${trait05.trait?.typeId}, traitUnitsId=${trait05.units?.id}}, isOptional=${trait05.isOptional}, isDeferred=${trait05.isDeferred}}, ")
        append("trait06={traitTypeId=${trait06.trait?.typeId}, traitUnitsId=${trait06.units?.id}}, isOptional=${trait06.isOptional}, isDeferred=${trait06.isDeferred}}, ")
        append("trait07={traitTypeId=${trait07.trait?.typeId}, traitUnitsId=${trait07.units?.id}}, isOptional=${trait07.isOptional}, isDeferred=${trait07.isDeferred}}, ")
        append("trait08={traitTypeId=${trait08.trait?.typeId}, traitUnitsId=${trait08.units?.id}}, isOptional=${trait08.isOptional}, isDeferred=${trait08.isDeferred}}, ")
        append("trait09={traitTypeId=${trait09.trait?.typeId}, traitUnitsId=${trait09.units?.id}}, isOptional=${trait09.isOptional}, isDeferred=${trait09.isDeferred}}, ")
        append("trait10={traitTypeId=${trait10.trait?.typeId}, traitUnitsId=${trait10.units?.id}}, isOptional=${trait10.isOptional}, isDeferred=${trait10.isDeferred}}, ")
        append("trait11={traitTypeId=${trait11.trait?.typeId}, traitUnitsId=${trait11.units?.id}}, isOptional=${trait11.isOptional}, isDeferred=${trait11.isDeferred}}, ")
        append("trait12={traitTypeId=${trait12.trait?.typeId}, traitUnitsId=${trait12.units?.id}}, isOptional=${trait12.isOptional}, isDeferred=${trait12.isDeferred}}, ")
        append("trait13={traitTypeId=${trait13.trait?.typeId}, traitUnitsId=${trait13.units?.id}}, isOptional=${trait13.isOptional}, isDeferred=${trait13.isDeferred}}, ")
        append("trait14={traitTypeId=${trait14.trait?.typeId}, traitUnitsId=${trait14.units?.id}}, isOptional=${trait14.isOptional}, isDeferred=${trait14.isDeferred}}, ")
        append("trait15={traitTypeId=${trait15.trait?.typeId}, traitUnitsId=${trait15.units?.id}}, isOptional=${trait15.isOptional}, isDeferred=${trait15.isDeferred}}, ")
        append("trait16={traitTypeId=${trait16.trait?.typeId}, traitUnitsId=${trait16.units?.id}}, isOptional=${trait16.isOptional}, isDeferred=${trait16.isDeferred}}, ")
        append("trait17={traitTypeId=${trait17.trait?.typeId}, traitUnitsId=${trait17.units?.id}}, isOptional=${trait17.isOptional}, isDeferred=${trait17.isDeferred}}, ")
        append("trait18={traitTypeId=${trait18.trait?.typeId}, traitUnitsId=${trait18.units?.id}}, isOptional=${trait18.isOptional}, isDeferred=${trait18.isDeferred}}, ")
        append("trait19={traitTypeId=${trait19.trait?.typeId}, traitUnitsId=${trait19.units?.id}}, isOptional=${trait19.isOptional}, isDeferred=${trait19.isDeferred}}, ")
        append("trait20={traitTypeId=${trait20.trait?.typeId}, traitUnitsId=${trait20.units?.id}}, isOptional=${trait20.isOptional}, isDeferred=${trait20.isDeferred}}")
    }
}

fun EvaluationEntries.summarizeForErrorReport(): String {
    return buildString {
        append("trait01={traitId=${trait01Id}, score=${trait01Score}}, ")
        append("trait02={traitId=${trait02Id}, score=${trait02Score}}, ")
        append("trait03={traitId=${trait03Id}, score=${trait03Score}}, ")
        append("trait04={traitId=${trait04Id}, score=${trait04Score}}, ")
        append("trait05={traitId=${trait05Id}, score=${trait05Score}}, ")
        append("trait06={traitId=${trait06Id}, score=${trait06Score}}, ")
        append("trait07={traitId=${trait07Id}, score=${trait07Score}}, ")
        append("trait08={traitId=${trait08Id}, score=${trait08Score}}, ")
        append("trait09={traitId=${trait09Id}, score=${trait09Score}}, ")
        append("trait10={traitId=${trait10Id}, score=${trait10Score}}, ")
        append("trait11={traitId=${trait11Id}, score=${trait11Score}, unitsId=${trait11UnitsId}}, ")
        append("trait12={traitId=${trait12Id}, score=${trait12Score}, unitsId=${trait12UnitsId}}, ")
        append("trait13={traitId=${trait13Id}, score=${trait13Score}, unitsId=${trait13UnitsId}}, ")
        append("trait14={traitId=${trait14Id}, score=${trait14Score}, unitsId=${trait14UnitsId}}, ")
        append("trait15={traitId=${trait15Id}, score=${trait15Score}, unitsId=${trait15UnitsId}}, ")
        append("trait16={traitId=${trait16Id}, optionId=${trait16OptionId}}, ")
        append("trait17={traitId=${trait17Id}, optionId=${trait17OptionId}}, ")
        append("trait18={traitId=${trait18Id}, optionId=${trait18OptionId}}, ")
        append("trait19={traitId=${trait19Id}, optionId=${trait19OptionId}}, ")
        append("trait20={traitId=${trait20Id}, optionId=${trait20OptionId}}")
    }
}

fun DrugWithdrawalSpec.summarizeForErrorReport(): String {
    return buildString {
        append("withdrawal=${withdrawal}, ")
        append("userWithdrawal=${userWithdrawal}, ")
        append("unitsId=${withdrawalUnitsId}")
    }
}

fun OffLabelDrugSpec.summarizeForErrorReport(): String {
    return buildString {
        append("veterinarianContactId=${veterinarianContactId}, ")
        append("speciesId=${speciesId}, ")
        append("drugDosage=${drugDosage}, ")
        append("useStartDate=${useStartDate}, ")
        append("useEndDate=${useEndDate}, ")
        append("note=${note}")
    }
}

fun ActionSet.summarizeForErrorReport(): String {
    return buildString {
        append("targetSpeciesId=${targetSpeciesId}, ")
        append("weightAction={weight=${weight?.weight}, units=${weight?.units?.id}}, ")
        append("vaccines=[${vaccines.joinToString(", ") { "{${it.summarizeForErrorReport()}}" }}], ")
        append("dewormers=[${dewormers.joinToString(", ") { "{${it.summarizeForErrorReport()}}" }}], ")
        append("otherDrugs=[${otherDrugs.joinToString(", ") { "{${it.summarizeForErrorReport()}}" }}], ")
        append("hoofCheck={${hoofCheck?.hoofCheck?.summarizeForErrorReport()}}, ")
        append("hornCheck={${hornCheck?.hornCheck?.summarizeForErrorReport()}}, ")
        append("shoeAction={isComplete=${shoeing?.isComplete}}, ")
        append("weanAction={isComplete=${weaning?.isComplete}, isActionable=${weaning?.isActionable}}, ")
        append("shearAction={isComplete=${shearing?.isComplete}}")
    }
}

fun DrugAction.summarizeForErrorReport(): String {
    return buildString {
        append("targetSpeciesId=${targetSpeciesId}, ")
        append("drugId=${configuration.drugApplicationInfo.drugId}, ")
        append("drugLotId=${configuration.drugApplicationInfo.drugLotId}, ")
        append("drugLocation=${configuration.location.id}, ")
        append("isDrugApplied=${isDrugApplied}")
    }
}

fun HoofCheck.summarizeForErrorReport(): String {
    return buildString {
        append("trimmed=[${trimmed.joinToString()}}], ")
        append("footRotObserved=[${withFootRotObserved.joinToString()}], ")
        append("footScaldObserved=[${withFootScaldObserved.joinToString()}]")
    }
}

fun HornCheck.summarizeForErrorReport(): String {
    return buildString {
        append("badHorns=[${badHorns.joinToString()}], ")
        append("sawedHorns=[${sawedHorns.joinToString()}]")
    }
}
