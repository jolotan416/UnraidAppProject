package com.jolotan.unraidapp.data

import androidx.room.Room
import androidx.room.RoomDatabase
import com.jolotan.unraidapp.data.datasource.ROOM_DATABASE_FILENAME
import com.jolotan.unraidapp.data.datasource.UnraidAppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<UnraidAppDatabase> =
    Room.databaseBuilder<UnraidAppDatabase>(
        name = "${getDocumentDirectory()}/$ROOM_DATABASE_FILENAME"
    )

@OptIn(ExperimentalForeignApi::class)
private fun getDocumentDirectory(): String =
    requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )?.path
    )