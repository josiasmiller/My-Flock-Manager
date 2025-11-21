package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

@JvmInline
value class Topic (val raw: String) {
    object Animal {
        val INFO = Topic("ANIMAL_INFO")
        val BIRTH_DATE = Topic("ANIMAL_BIRTH_DATE")
        val DEATH_DATE = Topic("ANIMAL_DEATH_DATE")
        val LOCATION = Topic("ANIMAL_LOCATION")
        val IDS = Topic("ANIMAL_IDS")
        val BREEDING = Topic("ANIMAL_BREEDING")
        val OFFSPRING = Topic("ANIMAL_OFFSPRING")
    }
}

data class TopicChange(
    val entityId: EntityId,
    val topics: Set<Topic>
) {
    fun covers(topicChange: TopicChange): Boolean {
        return entityId == topicChange.entityId &&
                topics.intersect(topicChange.topics)
                    .isNotEmpty()
    }
}

fun topicChange(entityId: EntityId, topicsCollector: MutableSet<Topic>.() -> Unit): TopicChange {
    return TopicChange(entityId, mutableSetOf<Topic>().also { topicsCollector(it) })
}
