package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository

class IdValidations(animalRepository: AnimalRepository) {
    val checkIdInputCompleteness = CheckIdInputCompleteness()
    val checkIdEntryIsOfficial = CheckIdEntryIsOfficial()
    val checkIdNumberFormat = CheckIdNumberFormat()
    val checkEIDsNotDuplicated = CheckEIDsNotDuplicated(animalRepository)
    val checkIdCombinationValidity = CheckIdCombinationValidity()
}
