package com.weyr_associates.animaltrakkerfarmmobile.model

object Company {
    const val ID_GENERIC_RAW = "ed382247-5cef-48ea-b7fe-b09491dc9ad6" //Legacy ID = 700
    val ID_GENERIC = EntityId(ID_GENERIC_RAW)

    val ID_REGISTRY_COMPANY_AMERICAN_BLACK_WELSH_MOUNTAIN_SHEEP_ASSOCIATION = EntityId("3a7e2399-17fd-4a8f-af43-d66fde9e0539") //LEGACY ID 25
    val ID_REGISTRY_COMPANY_AMERICAN_CHOCOLATE_WELSH_MOUNTAIN_SHEEP_ASSOCIATION = EntityId("dc9ffa44-049c-4b34-8430-61a442bbe025") //LEGACY ID 54
    val ID_REGISTRY_COMPANY_AMERICAN_WHITE_WELSH_MOUNTAIN_SHEEP_ASSOCIATION = EntityId("d2122a99-b1c1-419f-8e18-28e1112dc7a8") //LEGACY ID 55

    fun registryCompanyUsesBreedingDateToDetermineFlockPrefix(registryCompanyId: EntityId): Boolean {
        return when (registryCompanyId) {
            ID_REGISTRY_COMPANY_AMERICAN_BLACK_WELSH_MOUNTAIN_SHEEP_ASSOCIATION,
            ID_REGISTRY_COMPANY_AMERICAN_CHOCOLATE_WELSH_MOUNTAIN_SHEEP_ASSOCIATION,
            ID_REGISTRY_COMPANY_AMERICAN_WHITE_WELSH_MOUNTAIN_SHEEP_ASSOCIATION -> true
            else -> false
        }
    }
}
