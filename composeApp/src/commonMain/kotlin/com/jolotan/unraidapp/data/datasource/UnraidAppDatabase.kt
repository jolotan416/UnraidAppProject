package com.jolotan.unraidapp.data.datasource

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.jolotan.unraidapp.data.models.NasConnectionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal const val ROOM_DATABASE_FILENAME = "unraid_app.db"

@Database(entities = [NasConnectionData::class], version = 1)
@ConstructedBy(UnraidAppDatabaseConstructor::class)
abstract class UnraidAppDatabase : RoomDatabase() {
    abstract fun getNasConnectionDao(): NasConnectionDao
}

fun RoomDatabase.Builder<UnraidAppDatabase>.getUnraidAppDatabase(): UnraidAppDatabase =
    fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()