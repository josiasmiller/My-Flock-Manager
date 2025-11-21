package com.weyr_associates.animaltrakkerfarmmobile.model

data class DefaultSettings(
    val id: EntityId,
    val name: String,
    val ownerContactId: EntityId?,
    val ownerCompanyId: EntityId?,
    val ownerPremiseId: EntityId,
    val breederContactId: EntityId?,
    val breederCompanyId: EntityId?,
    val breederPremiseId: EntityId,
    val vetContactId: EntityId?,
    val vetPremiseId: EntityId?,
    val labCompanyId: EntityId?,
    val labPremiseId: EntityId?,
    val registryCompanyId: EntityId?,
    val registryPremiseId: EntityId?,
    val flockPrefixId: EntityId?,
    val breedId: EntityId,
    val sexId: EntityId,
    val idTypeIdPrimary: EntityId,
    val idTypeIdSecondary: EntityId?,
    val idTypeIdTertiary: EntityId?,
    val eidKeepMaleFemaleIdColorSame: Boolean,
    val eidColorMale: EntityId?,
    val eidColorFemale: EntityId?,
    val eidIdLocation: EntityId?,
    val farmKeepMaleFemaleIdColorSame: Boolean,
    val farmIdBasedOnEid: Boolean,
    val farmIdNumberDigitsFromEid: Int,
    val farmIdColorMale: EntityId?,
    val farmIdColorFemale: EntityId?,
    val farmIdLocation: EntityId?,
    val fedKeepMaleFemaleIdColorSame: Boolean,
    val fedIdColorMale: EntityId?,
    val fedIdColorFemale: EntityId?,
    val fedIdLocation: EntityId?,
    val nuesMaleFemaleKeepIdColorSame: Boolean,
    val nuesIdColorMale: EntityId?,
    val nuesIdColorFemale: EntityId?,
    val nuesIdLocation: EntityId?,
    val trichKeepMaleFemaleIdColorSame: Boolean,
    val trichIdColorMale: EntityId?,
    val trichIdColorFemale: EntityId?,
    val trichIdLocation: EntityId?,
    val trichIdAutoIncrement: Boolean,
    val trichNextIdNumber: Int?,
    val bangsIdKeepMaleFemaleIdColorSame: Boolean,
    val bangsIdColorMale: EntityId?,
    val bangsIdColorFemale: EntityId?,
    val bangsIdLocation: EntityId?,
    val saleOrderKeepMaleFemaleIdColorSame: Boolean,
    val saleOrderIdColorMale: EntityId?,
    val saleOrderIdColorFemale: EntityId?,
    val saleOrderIdLocation: EntityId?,
    val usePaintMarks: Boolean,
    val paintMarkColor: EntityId?,
    val paintMarkLocation: EntityId?,
    val tattooColor: EntityId?,
    val tattooLocation: EntityId?,
    val freezeBrandLocation: EntityId?,
    val removeReasonId: EntityId,
    val tissueSampleTypeId: EntityId?,
    val tissueTestId: EntityId?,
    val tissueSampleContainerTypeId: EntityId,
    val birthTypeId: EntityId,
    val rearTypeId: EntityId?,
    val minimumBirthWeight: Float,
    val maximumBirthWeight: Float,
    val birthWeightUnitsId: EntityId,
    val weightUnitsId: EntityId,
    val salePriceUnitsId: EntityId?,
    val deathReasonId: EntityId,
    val deathReasonContactId: EntityId?,
    val deathReasonCompanyId: EntityId?,
    val transferReasonId: EntityId
) {
    companion object {

        const val SETTINGS_ID_DEFAULT_RAW = "29753af4-2b46-49b3-854c-4644d8919db6" //LEGACY ID = 1
        const val SPECIES_ID_DEFAULT_RAW = "3ca0b500-3f96-4342-8620-bfda6e900222" //LEGACY ID = 1

        val SETTINGS_ID_DEFAULT = EntityId(SETTINGS_ID_DEFAULT_RAW)
        val SPECIES_ID_DEFAULT = EntityId(SPECIES_ID_DEFAULT_RAW)
        val ID_COLOR_ID_DEFAULT = IdColor.ID_COLOR_ID_NOT_APPLICABLE //YELLOW
        val ID_LOCATION_ID_DEFAULT = IdLocation.ID_LOCATION_ID_UNKNOWN
    }

    val userId: EntityId?
        get() = when {
            ownerContactId != null && ownerContactId.isValid -> ownerContactId
            ownerCompanyId != null && ownerCompanyId.isValid -> ownerCompanyId
            else -> null
        }

    val userType: UserType?
        get() = when {
            ownerContactId != null && ownerContactId.isValid -> UserType.CONTACT
            ownerCompanyId != null && ownerCompanyId.isValid -> UserType.COMPANY
            else -> null
        }

    val ownerId: EntityId?
        get() = when {
            ownerContactId != null && ownerContactId.isValid -> ownerContactId
            ownerCompanyId != null && ownerCompanyId.isValid -> ownerCompanyId
            else -> null
        }

    val ownerType: Int?
        get() = when {
            ownerContactId != null && ownerContactId.isValid -> Owner.Type.CONTACT.code
            ownerCompanyId != null && ownerCompanyId.isValid -> Owner.Type.COMPANY.code
        else -> null
    }

    val breederId: EntityId?
        get() = when {
            breederContactId != null && breederContactId.isValid -> breederContactId
            breederCompanyId != null && breederCompanyId.isValid -> breederCompanyId
            else -> null
        }

    val breederType: Int?
        get() = when {
            breederContactId != null && breederContactId.isValid -> Breeder.Type.CONTACT.code
            breederCompanyId != null && breederCompanyId.isValid -> Breeder.Type.COMPANY.code
            else -> null
        }

    val speciesId: EntityId
        get() = Sex.speciesIdFromSexId(sexId)

    fun defaultIdColorFromIdType(idTypeId: EntityId): EntityId? =
        when (idTypeId) {
            IdType.ID_TYPE_ID_FED -> fedIdColorMale
            IdType.ID_TYPE_ID_EID -> eidColorMale
            IdType.ID_TYPE_ID_PAINT -> paintMarkColor
            IdType.ID_TYPE_ID_FARM -> farmIdColorMale
            IdType.ID_TYPE_ID_TATTOO -> tattooColor
            IdType.ID_TYPE_ID_TRICH -> trichIdColorMale
            IdType.ID_TYPE_ID_NUES -> nuesIdColorMale
            IdType.ID_TYPE_ID_SALE_ORDER -> saleOrderIdColorMale
            IdType.ID_TYPE_ID_BANGS -> bangsIdColorMale
            else -> ID_COLOR_ID_DEFAULT
        }

    fun defaultIdLocationFromIdType(idTypeId: EntityId): EntityId? =
        when (idTypeId) {
            IdType.ID_TYPE_ID_FED -> fedIdLocation
            IdType.ID_TYPE_ID_EID -> eidIdLocation
            IdType.ID_TYPE_ID_PAINT -> paintMarkLocation
            IdType.ID_TYPE_ID_FARM -> farmIdLocation
            IdType.ID_TYPE_ID_TATTOO -> tattooLocation
            IdType.ID_TYPE_ID_TRICH -> trichIdLocation
            IdType.ID_TYPE_ID_NUES -> nuesIdLocation
            IdType.ID_TYPE_ID_SALE_ORDER -> saleOrderIdLocation
            IdType.ID_TYPE_ID_BANGS -> bangsIdLocation
            else -> ID_LOCATION_ID_DEFAULT
        }
}
