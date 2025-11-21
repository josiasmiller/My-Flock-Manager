package com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHandler(context: Context?, dbName: String?) :
    SQLiteOpenHelper(context, dbName, null, DATABASE_VERSION) {

    init {
        setOpenParams(
            SQLiteDatabase.OpenParams.Builder()
                .setJournalMode("DELETE")
                .build()
        )
    }

    override fun onCreate(db: SQLiteDatabase) {
        //NO-OP
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //NO-OP
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    companion object {

        private const val DATABASE_VERSION = 5

        fun create(context: Context, databaseName: String): DatabaseHandler {
            return DatabaseHandler(context, databaseName)
        }
    }
}
