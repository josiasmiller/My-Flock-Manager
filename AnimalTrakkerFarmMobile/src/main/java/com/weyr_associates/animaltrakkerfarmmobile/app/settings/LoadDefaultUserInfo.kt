package com.weyr_associates.animaltrakkerfarmmobile.app.settings

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.UserType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserInfo(
    val userId: EntityId,
    val userType: UserType
)

class LoadDefaultUserInfo(
    private val loadActiveDefaults: LoadActiveDefaultSettings
) {
    suspend operator fun invoke(): UserInfo {
        return withContext(Dispatchers.IO) {
            loadActiveDefaults()
        }.let {
            val userId = it.userId
            val userType = it.userType
            if (userId != null && userType != null) {
                UserInfo(userId, userType)
            } else {
                UserInfo(EntityId.UNKNOWN, UserType.CONTACT)
            }
        }
    }
}
