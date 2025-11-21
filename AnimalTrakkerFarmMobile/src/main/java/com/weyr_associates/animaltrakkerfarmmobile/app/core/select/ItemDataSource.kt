package com.weyr_associates.animaltrakkerfarmmobile.app.core.select

import android.content.Context
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager

interface ItemDataSource<T> : AutoCloseable {
    suspend fun queryItems(filterText: String): List<T>
    override fun close() { /*NO-OP*/ }
}

abstract class EntityItemDataSource<T>(context: Context) : ItemDataSource<T> {

    protected val databaseHandler: DatabaseHandler = DatabaseManager.getInstance(context)
        .createDatabaseHandler()

    override fun close() {
        databaseHandler.close()
        super.close()
    }
}
