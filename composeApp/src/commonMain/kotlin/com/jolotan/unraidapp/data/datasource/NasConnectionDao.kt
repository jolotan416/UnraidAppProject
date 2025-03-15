package com.jolotan.unraidapp.data.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jolotan.unraidapp.data.models.NasConnectionData
import kotlinx.coroutines.flow.Flow

@Dao
interface NasConnectionDao {
    @Query("SELECT * FROM nasConnection")
    fun getNasConnectionDataFlow(): Flow<List<NasConnectionData>>

    @Insert
    suspend fun insertNasConnection(nasConnectionData: NasConnectionData)

    @Update
    suspend fun updateNasConnectionData(nasConnectionData: NasConnectionData)

    @Delete
    suspend fun deleteNasConnectionData(nasConnectionData: NasConnectionData)
}