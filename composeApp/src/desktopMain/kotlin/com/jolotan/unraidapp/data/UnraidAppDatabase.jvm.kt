package com.jolotan.unraidapp.data

import androidx.room.Room
import androidx.room.RoomDatabase
import com.jolotan.unraidapp.data.datasource.ROOM_DATABASE_FILENAME
import com.jolotan.unraidapp.data.datasource.UnraidAppDatabase
import java.io.File

private const val JVM_TEMP_DIR = "java.io.tmpdir"

fun getDatabaseBuilder(): RoomDatabase.Builder<UnraidAppDatabase> =
    Room.databaseBuilder<UnraidAppDatabase>(
        name = File(System.getProperty(JVM_TEMP_DIR), ROOM_DATABASE_FILENAME).absolutePath,
    )