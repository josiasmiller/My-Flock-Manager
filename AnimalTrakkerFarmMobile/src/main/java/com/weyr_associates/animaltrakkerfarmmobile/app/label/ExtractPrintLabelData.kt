package com.weyr_associates.animaltrakkerfarmmobile.app.label

import android.content.SharedPreferences
import android.os.Parcelable
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationEntries
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultIdTypeIds
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.mostRecentDateOnOfType
import com.weyr_associates.animaltrakkerfarmmobile.model.oldestDateOnOfType
import kotlinx.parcelize.Parcelize

@Parcelize
data class PrintLabelData(
    val labelText: String,
    val eidNumber: String,
    val secondaryIdInfo: IdInfo? = null
): Parcelable

sealed interface ExtractPrintLabelDataError

data object NoEIDFound : ExtractPrintLabelDataError

class ExtractPrintLabelData(
    private val preferences: SharedPreferences,
    private val loadDefaultIdTypeIds: LoadDefaultIdTypeIds
) {
    suspend fun forStandardLabel(
        idInfoItems: List<IdInfo>
    ): Result<PrintLabelData, ExtractPrintLabelDataError> {
        val defaultIdTypeIds = loadDefaultIdTypeIds()
        return forStandardLabel(
            defaultIdTypeIds.secondaryIdTypeId,
            idInfoItems
        )
    }

    fun forStandardLabel(
        secondaryIdType: EntityId?,
        idInfoItems: List<IdInfo>
    ): Result<PrintLabelData, ExtractPrintLabelDataError> {

        val eidInfo = idInfoItems.mostRecentDateOnOfType(IdType.ID_TYPE_ID_EID)
            ?: return Result.Failure(NoEIDFound)

        val secondaryIdInfo = secondaryIdType?.let {
            idInfoItems.mostRecentDateOnOfType(it)
        }

        val labelText = preferences.getString(
            PrintLabel.PREFS_KEY_PRINT_LABEL_TEXT,
            PrintLabel.DEFAULT_PRINT_LABEL_TEXT
        ) ?: PrintLabel.DEFAULT_PRINT_LABEL_TEXT

        return Result.Success(
            PrintLabelData(
                labelText = labelText,
                eidNumber = eidInfo.number,
                secondaryIdInfo = secondaryIdInfo
            )
        )
    }

    suspend fun forStandardLabel(
        eidNumber: String,
        idInfoItems: List<IdInfo>
    ): Result<PrintLabelData, ExtractPrintLabelDataError> {
        val defaultIdTypeIds = loadDefaultIdTypeIds()
        return forStandardLabel(
            eidNumber,
            defaultIdTypeIds.secondaryIdTypeId,
            idInfoItems
        )
    }

    fun forStandardLabel(
        eidNumber: String,
        secondaryIdType: EntityId?,
        idInfoItems: List<IdInfo>
    ): Result<PrintLabelData, ExtractPrintLabelDataError> {

        val secondaryIdInfo = secondaryIdType?.let {
            idInfoItems.mostRecentDateOnOfType(it)
        }

        val labelText = preferences.getString(
            PrintLabel.PREFS_KEY_PRINT_LABEL_TEXT,
            PrintLabel.DEFAULT_PRINT_LABEL_TEXT
        ) ?: PrintLabel.DEFAULT_PRINT_LABEL_TEXT

        return Result.Success(
            PrintLabelData(
                labelText = labelText,
                eidNumber = eidNumber,
                secondaryIdInfo = secondaryIdInfo
            )
        )
    }

    suspend fun forOptimalAgRamBSETissueSamples(
        eidString: String?,
        animalBasicInfo: AnimalBasicInfo,
        evaluationEntries: EvaluationEntries
    ): Result<PrintLabelData, ExtractPrintLabelDataError> {
        val eidNumber = extractEidNumber(eidString, animalBasicInfo.ids)
            ?: return Result.Failure(NoEIDFound)
        val ageInYears = animalBasicInfo.ageInYears()
        val breedAbbr = animalBasicInfo.breedAbbreviation
        val scrotalCircScore = evaluationEntries.extractScoreForUnitTraitId(
            EvalTrait.UNIT_TRAIT_ID_SCROTAL_CIRCUMFERENCE
        )
        val bodyConditionScore = evaluationEntries.extractScoreForUnitTraitId(
            EvalTrait.UNIT_TRAIT_ID_BODY_CONDITION_SCORE
        )
        val labelText = "${eidNumber.takeLast(3)}-${ageInYears}-${breedAbbr}-${scrotalCircScore}-${bodyConditionScore}"
        val secondaryIdInfo = with(animalBasicInfo.ids) {
            oldestDateOnOfType(IdType.ID_TYPE_ID_FED) ?:
            oldestDateOnOfType(IdType.ID_TYPE_ID_FARM)
        }
        return Result.Success(
            PrintLabelData(
                labelText = labelText,
                eidNumber = eidNumber,
                secondaryIdInfo = secondaryIdInfo
            )
        )
    }

    private fun extractEidNumber(eidString: String?, idInfoItems: List<IdInfo>): String? {
        return eidString ?: idInfoItems.mostRecentDateOnOfType(IdType.ID_TYPE_ID_EID)?.number
    }
}
