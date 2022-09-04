package com.example.pair

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class PairApplication : Application() {

    companion object {
        var room: RoomDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()

        /*val MIGRATION = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    execSQL("CREATE TABLE account_backup (UUID TEXT, name TEXT, PRIMARY KEY (UUID))")
                    execSQL("INSERT INTO account_backup SELECT UUID, name FROM account")
                    execSQL("DROP TABLE account")
                    execSQL("ALTER TABLE account_backup ADD COLUMN image TEXT")
                    execSQL("ALTER TABLE account_backup RENAME to account")
                }
            }
        }*/

        // TODO: Remove destructive migration
        room = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            // TODO change db name to pair
            "cliqDB"
        ).fallbackToDestructiveMigration().build()
    }
}