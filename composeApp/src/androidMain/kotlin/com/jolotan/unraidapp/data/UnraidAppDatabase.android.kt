package com.jolotan.unraidapp.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jolotan.unraidapp.data.datasource.ROOM_DATABASE_FILENAME
import com.jolotan.unraidapp.data.datasource.UnraidAppDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<UnraidAppDatabase> =
    context.applicationContext.run {
        Room.databaseBuilder(
            context = this,
            name = getDatabasePath(ROOM_DATABASE_FILENAME).absolutePath
        )
    }