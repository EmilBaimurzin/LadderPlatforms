package com.ladder.game.data.data_base

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ladder.game.data.data_base.entities.GameDB

@Dao
interface GameDao {
    @Insert
    fun saveGame(game: GameDB)

    @Update
    fun updateGame(game: GameDB)

    @Query("DELETE FROM GameDB")
    fun deleteGame()

    @Query("SELECT * FROM GameDB")
    fun getGame(): List<GameDB>
}