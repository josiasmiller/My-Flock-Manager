package com.weyr_associates.animaltrakkerfarmmobile.model

data class SavedEvaluation(
    override val id: EntityId,
    override val name: String,
    val userId: EntityId,
    val userType: UserType,
    val summarizeInAlert: Boolean = false,
    val trait01: BasicEvalTrait? = null,
    val trait02: BasicEvalTrait? = null,
    val trait03: BasicEvalTrait? = null,
    val trait04: BasicEvalTrait? = null,
    val trait05: BasicEvalTrait? = null,
    val trait06: BasicEvalTrait? = null,
    val trait07: BasicEvalTrait? = null,
    val trait08: BasicEvalTrait? = null,
    val trait09: BasicEvalTrait? = null,
    val trait10: BasicEvalTrait? = null,
    val trait11: UnitsEvalTrait? = null,
    val trait12: UnitsEvalTrait? = null,
    val trait13: UnitsEvalTrait? = null,
    val trait14: UnitsEvalTrait? = null,
    val trait15: UnitsEvalTrait? = null,
    val trait16: CustomEvalTrait? = null,
    val trait17: CustomEvalTrait? = null,
    val trait18: CustomEvalTrait? = null,
    val trait19: CustomEvalTrait? = null,
    val trait20: CustomEvalTrait? = null
): HasIdentity, HasName {
    companion object {

        const val ID_SIMPLE_LAMBING_RAW = "f5efab5f-d4e1-48dc-b122-5e2ef7fc36f9" //LEGACY ID = 1
        const val ID_SIMPLE_SORT_RAW = "01040d27-4019-4e30-8149-dc22c070980e" //LEGACY ID = 2
        const val ID_OPTIMAL_AG_RAM_TEST_RAW = "e709cb74-f72f-45ff-9dcb-7abaf2bd66b3" //LEGACY ID = 4
        const val ID_OPTIMAL_LIVESTOCK_EWE_ULTRASOUND_RAW = "129b3b03-2de2-4200-8480-b5df6f3cc8ff" //LEGACY ID = 5
        const val ID_SUCK_REFLEX_RAW = "a42e39e3-c892-4783-b5bc-2665f1cc8d2c" //LEGACY ID = 6
        const val ID_SIMPLE_BIRTHS_RAW = "2402f686-9be9-4247-b925-e37c36906007" //LEGACY ID = 7

        val ID_SIMPLE_LAMBING = EntityId(ID_SIMPLE_LAMBING_RAW)
        val ID_SIMPLE_SORT = EntityId(ID_SIMPLE_SORT_RAW)
        val ID_OPTIMAL_AG_RAM_TEST = EntityId(ID_OPTIMAL_AG_RAM_TEST_RAW)
        val ID_OPTIMAL_LIVESTOCK_EWE_ULTRASOUND = EntityId(ID_OPTIMAL_LIVESTOCK_EWE_ULTRASOUND_RAW)
        val ID_SUCK_REFLEX = EntityId(ID_SUCK_REFLEX_RAW)
        val ID_SIMPLE_BIRTHS = EntityId(ID_SIMPLE_BIRTHS_RAW)
    }
}
