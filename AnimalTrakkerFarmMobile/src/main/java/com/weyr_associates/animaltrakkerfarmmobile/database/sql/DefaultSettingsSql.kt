package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getBoolean
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getFloat
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DefaultSettingsTable
import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry

fun Cursor.readDefaultSettingsEntry(colQualifier: String? = null): ItemEntry {
    return ItemEntry(
        id = getEntityId(DefaultSettingsTable.Columns.ID.qualifiedBy(colQualifier)),
        name = getString(DefaultSettingsTable.Columns.NAME.qualifiedBy(colQualifier))
    )
}

fun Cursor.readDefaultSettings(colQualifier: String? = null): DefaultSettings {
    return DefaultSettings(
        id = getEntityId(DefaultSettingsTable.Columns.ID.qualifiedBy(colQualifier)),
        name = getString(DefaultSettingsTable.Columns.NAME.qualifiedBy(colQualifier)),
        ownerContactId = getOptEntityId(DefaultSettingsTable.Columns.OWNER_CONTACT_ID.qualifiedBy(colQualifier)),
        ownerCompanyId = getOptEntityId(DefaultSettingsTable.Columns.OWNER_COMPANY_ID.qualifiedBy(colQualifier)),
        ownerPremiseId = getEntityId(DefaultSettingsTable.Columns.OWNER_PREMISE_ID.qualifiedBy(colQualifier)),
        breederContactId = getOptEntityId(DefaultSettingsTable.Columns.BREEDER_CONTACT_ID.qualifiedBy(colQualifier)),
        breederCompanyId = getOptEntityId(DefaultSettingsTable.Columns.BREEDER_COMPANY_ID.qualifiedBy(colQualifier)),
        breederPremiseId = getEntityId(DefaultSettingsTable.Columns.BREEDER_PREMISE_ID.qualifiedBy(colQualifier)),
        vetContactId = getOptEntityId(DefaultSettingsTable.Columns.VET_CONTACT_ID.qualifiedBy(colQualifier)),
        vetPremiseId = getOptEntityId(DefaultSettingsTable.Columns.VET_PREMISE_ID.qualifiedBy(colQualifier)),
        labCompanyId = getOptEntityId(DefaultSettingsTable.Columns.LAB_COMPANY_ID.qualifiedBy(colQualifier)),
        labPremiseId = getOptEntityId(DefaultSettingsTable.Columns.LAB_PREMISE_ID.qualifiedBy(colQualifier)),
        registryCompanyId = getOptEntityId(DefaultSettingsTable.Columns.REGISTRY_COMPANY_ID.qualifiedBy(colQualifier)),
        registryPremiseId = getOptEntityId(DefaultSettingsTable.Columns.REGISTRY_PREMISE_ID.qualifiedBy(colQualifier)),
        flockPrefixId = getOptEntityId(DefaultSettingsTable.Columns.FLOCK_PREFIX_ID.qualifiedBy(colQualifier)),
        breedId = getEntityId(DefaultSettingsTable.Columns.BREED_ID.qualifiedBy(colQualifier)),
        sexId = getEntityId(DefaultSettingsTable.Columns.SEX_ID.qualifiedBy(colQualifier)),
        idTypeIdPrimary = getEntityId(DefaultSettingsTable.Columns.ID_TYPE_ID_PRIMARY.qualifiedBy(colQualifier)),
        idTypeIdSecondary = getOptEntityId(DefaultSettingsTable.Columns.ID_TYPE_ID_SECONDARY.qualifiedBy(colQualifier)),
        idTypeIdTertiary = getOptEntityId(DefaultSettingsTable.Columns.ID_TYPE_ID_TERTIARY.qualifiedBy(colQualifier)),
        eidKeepMaleFemaleIdColorSame = getBoolean(DefaultSettingsTable.Columns.EID_TAG_MALE_COLOR_FEMALE_COLOR_SAME.qualifiedBy(colQualifier)),
        eidColorMale = getOptEntityId(DefaultSettingsTable.Columns.EID_TAG_COLOR_MALE.qualifiedBy(colQualifier)),
        eidColorFemale = getOptEntityId(DefaultSettingsTable.Columns.EID_TAG_COLOR_FEMALE.qualifiedBy(colQualifier)),
        eidIdLocation = getOptEntityId(DefaultSettingsTable.Columns.EID_TAG_LOCATION.qualifiedBy(colQualifier)),
        farmKeepMaleFemaleIdColorSame = getBoolean(DefaultSettingsTable.Columns.FARM_TAG_MALE_COLOR_FEMALE_COLOR_SAME.qualifiedBy(colQualifier)),
        farmIdBasedOnEid = getBoolean(DefaultSettingsTable.Columns.FARM_TAG_BASED_ON_EID_TAG.qualifiedBy(colQualifier)),
        farmIdNumberDigitsFromEid = getOptInt(DefaultSettingsTable.Columns.FARM_TAG_NUMBER_DIGITS_FROM_EID.qualifiedBy(colQualifier)) ?: 0,
        farmIdColorMale = getOptEntityId(DefaultSettingsTable.Columns.FARM_TAG_COLOR_MALE.qualifiedBy(colQualifier)),
        farmIdColorFemale = getOptEntityId(DefaultSettingsTable.Columns.FARM_TAG_COLOR_FEMALE.qualifiedBy(colQualifier)),
        farmIdLocation = getOptEntityId(DefaultSettingsTable.Columns.FARM_TAG_LOCATION.qualifiedBy(colQualifier)),
        fedKeepMaleFemaleIdColorSame = getBoolean(DefaultSettingsTable.Columns.FED_TAG_MALE_COLOR_FEMALE_COLOR_SAME.qualifiedBy(colQualifier)),
        fedIdColorMale = getOptEntityId(DefaultSettingsTable.Columns.FED_TAG_COLOR_MALE.qualifiedBy(colQualifier)),
        fedIdColorFemale = getOptEntityId(DefaultSettingsTable.Columns.FED_TAG_COLOR_FEMALE.qualifiedBy(colQualifier)),
        fedIdLocation = getOptEntityId(DefaultSettingsTable.Columns.FED_TAG_LOCATION.qualifiedBy(colQualifier)),
        nuesMaleFemaleKeepIdColorSame = getBoolean(DefaultSettingsTable.Columns.NUES_TAG_MALE_COLOR_FEMALE_COLOR_SAME.qualifiedBy(colQualifier)),
        nuesIdColorMale = getOptEntityId(DefaultSettingsTable.Columns.NUES_TAG_COLOR_MALE.qualifiedBy(colQualifier)),
        nuesIdColorFemale = getOptEntityId(DefaultSettingsTable.Columns.NUES_TAG_COLOR_FEMALE.qualifiedBy(colQualifier)),
        nuesIdLocation = getOptEntityId(DefaultSettingsTable.Columns.NUES_TAG_LOCATION.qualifiedBy(colQualifier)),
        trichKeepMaleFemaleIdColorSame = getBoolean(DefaultSettingsTable.Columns.TRICH_TAG_MALE_COLOR_FEMALE_COLOR_SAME.qualifiedBy(colQualifier)),
        trichIdColorMale = getOptEntityId(DefaultSettingsTable.Columns.TRICH_TAG_COLOR_MALE.qualifiedBy(colQualifier)),
        trichIdColorFemale = getOptEntityId(DefaultSettingsTable.Columns.TRICH_TAG_COLOR_FEMALE.qualifiedBy(colQualifier)),
        trichIdLocation = getOptEntityId(DefaultSettingsTable.Columns.TRICH_TAG_LOCATION.qualifiedBy(colQualifier)),
        trichIdAutoIncrement = getBoolean(DefaultSettingsTable.Columns.TRICH_TAG_AUTO_INCREMENT.qualifiedBy(colQualifier)),
        trichNextIdNumber = getOptInt(DefaultSettingsTable.Columns.TRICH_TAG_NEXT_TAG_NUMBER.qualifiedBy(colQualifier)),
        bangsIdKeepMaleFemaleIdColorSame = getBoolean(DefaultSettingsTable.Columns.BANGS_TAG_MALE_COLOR_FEMALE_COLOR_SAME.qualifiedBy(colQualifier)),
        bangsIdColorMale = getOptEntityId(DefaultSettingsTable.Columns.BANGS_TAG_COLOR_MALE.qualifiedBy(colQualifier)),
        bangsIdColorFemale = getOptEntityId(DefaultSettingsTable.Columns.BANGS_TAG_COLOR_FEMALE.qualifiedBy(colQualifier)),
        bangsIdLocation = getOptEntityId(DefaultSettingsTable.Columns.BANGS_TAG_LOCATION.qualifiedBy(colQualifier)),
        saleOrderKeepMaleFemaleIdColorSame = getBoolean(DefaultSettingsTable.Columns.SALE_ORDER_TAG_MALE_COLOR_FEMALE_COLOR_SAME.qualifiedBy(colQualifier)),
        saleOrderIdColorMale = getOptEntityId(DefaultSettingsTable.Columns.SALE_ORDER_TAG_COLOR_MALE.qualifiedBy(colQualifier)),
        saleOrderIdColorFemale = getOptEntityId(DefaultSettingsTable.Columns.SALE_ORDER_TAG_COLOR_FEMALE.qualifiedBy(colQualifier)),
        saleOrderIdLocation = getOptEntityId(DefaultSettingsTable.Columns.SALE_ORDER_TAG_LOCATION.qualifiedBy(colQualifier)),
        usePaintMarks = getBoolean(DefaultSettingsTable.Columns.USE_PAINT_MARKS.qualifiedBy(colQualifier)),
        paintMarkColor = getOptEntityId(DefaultSettingsTable.Columns.PAINT_MARK_COLOR.qualifiedBy(colQualifier)),
        paintMarkLocation = getOptEntityId(DefaultSettingsTable.Columns.PAINT_MARK_LOCATION.qualifiedBy(colQualifier)),
        tattooColor = getOptEntityId(DefaultSettingsTable.Columns.TATTOO_COLOR.qualifiedBy(colQualifier)),
        tattooLocation = getOptEntityId(DefaultSettingsTable.Columns.TATTOO_LOCATION.qualifiedBy(colQualifier)),
        freezeBrandLocation = getOptEntityId(DefaultSettingsTable.Columns.FREEZE_BRAND_LOCATION.qualifiedBy(colQualifier)),
        removeReasonId = getEntityId(DefaultSettingsTable.Columns.REMOVE_REASON_ID.qualifiedBy(colQualifier)),
        tissueSampleTypeId = getOptEntityId(DefaultSettingsTable.Columns.TISSUE_SAMPLE_TYPE_ID.qualifiedBy(colQualifier)),
        tissueTestId = getOptEntityId(DefaultSettingsTable.Columns.TISSUE_TEST_ID.qualifiedBy(colQualifier)),
        tissueSampleContainerTypeId = getEntityId(DefaultSettingsTable.Columns.TISSUE_CONTAINER_TYPE_ID.qualifiedBy(colQualifier)),
        birthTypeId = getEntityId(DefaultSettingsTable.Columns.BIRTH_TYPE.qualifiedBy(colQualifier)),
        rearTypeId = getOptEntityId(DefaultSettingsTable.Columns.REAR_TYPE.qualifiedBy(colQualifier)),
        minimumBirthWeight = getFloat(DefaultSettingsTable.Columns.MINIMUM_BIRTH_WEIGHT.qualifiedBy(colQualifier)),
        maximumBirthWeight = getFloat(DefaultSettingsTable.Columns.MAXIMUM_BIRTH_WEIGHT.qualifiedBy(colQualifier)),
        birthWeightUnitsId = getEntityId(DefaultSettingsTable.Columns.BIRTH_WEIGHT_UNITS_ID.qualifiedBy(colQualifier)),
        weightUnitsId = getEntityId(DefaultSettingsTable.Columns.WEIGHT_UNITS_ID.qualifiedBy(colQualifier)),
        salePriceUnitsId = getOptEntityId(DefaultSettingsTable.Columns.SALE_PRICE_UNITS_ID.qualifiedBy(colQualifier)),
        deathReasonId = getEntityId(DefaultSettingsTable.Columns.DEATH_REASON_ID.qualifiedBy(colQualifier)),
        deathReasonContactId = getOptEntityId(DefaultSettingsTable.Columns.DEATH_REASON_CONTACT_ID.qualifiedBy(colQualifier)),
        deathReasonCompanyId = getOptEntityId(DefaultSettingsTable.Columns.DEATH_REASON_COMPANY_ID.qualifiedBy(colQualifier)),
        transferReasonId = getEntityId(DefaultSettingsTable.Columns.TRANSFER_REASON_ID.qualifiedBy(colQualifier))
    )
}
