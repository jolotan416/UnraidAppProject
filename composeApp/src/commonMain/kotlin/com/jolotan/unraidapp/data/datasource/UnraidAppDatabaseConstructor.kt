package com.jolotan.unraidapp.data.datasource

import androidx.room.RoomDatabaseConstructor

// Room compiler generates the `actual` implementations
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object UnraidAppDatabaseConstructor : RoomDatabaseConstructor<UnraidAppDatabase> {
    override fun initialize(): UnraidAppDatabase
}