package com.ladder.game.data.data_base

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ladder.game.data.data_base.entities.GameDB
import com.ladder.game.data.data_base.entities.ResultDB

@Dao
interface Dao {

    @Query("SELECT * FROM ResultDB")
    fun getAllResults(): List<ResultDB>

    @Insert
    fun addResult(result: ResultDB)

}