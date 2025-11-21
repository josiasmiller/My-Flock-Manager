package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

data class EvaluationField(
    val id: EvaluationFieldId,
    val traitId: EntityId? = null,
    val traitName: String = "",
    val traitEntry: Entry = Entry.UNCOLLECTED,
) {

    enum class Entry {
        REQUIRED,
        OPTIONAL,
        UNCOLLECTED;

        val isNotRequired
            get() = this != REQUIRED

        val isUncollected
            get() = this == UNCOLLECTED
    }
}
