package com.weyr_associates.animaltrakkerfarmmobile.app.settings

import android.content.Context
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadDefaultRegistryCompanyId(
    private val loadActiveDefaults: LoadActiveDefaultSettings
) {
    companion object {
        fun from(context: Context, databaseHandler: DatabaseHandler): LoadDefaultRegistryCompanyId {
            return LoadDefaultRegistryCompanyId(
                LoadActiveDefaultSettings.from(
                    context.applicationContext,
                    databaseHandler
                )
            )
        }
    }

    suspend operator fun invoke(): EntityId? {
        return withContext(Dispatchers.IO) {
            loadActiveDefaults().registryCompanyId
        }
    }
}
