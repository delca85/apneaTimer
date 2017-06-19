package com.bibi.android.apnea.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*
import java.sql.Types.*

/**
 * Created by bibi on 19/06/17.
 */

class RecordDatabaseHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "RecordDatabase", null, 1) {
    companion object {
        private var instance: RecordDatabaseHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): RecordDatabaseHelper {
            if (instance == null) {
                instance = RecordDatabaseHelper(ctx.getApplicationContext())
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) = // Here you create tables
            db?.createTable("ApneaLog", ifNotExists = true,
                    columns = *arrayOf("id" to TEXT + PRIMARY_KEY + UNIQUE,
                    "apnea" to TEXT,
                    "breath" to TEXT)
            )

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db?.dropTable("ApneaLog", true)
    }
}

// Access property for Context
val Context.database: RecordDatabaseHelper
    get() = RecordDatabaseHelper.getInstance(applicationContext)