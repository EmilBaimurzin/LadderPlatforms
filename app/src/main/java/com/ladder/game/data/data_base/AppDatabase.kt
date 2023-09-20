package com.ladder.game.data.data_base

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ladder.game.data.data_base.entities.GameDB
import com.ladder.game.data.data_base.entities.ResultDB

@Database(entities = [ResultDB::class, GameDB::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): Dao
    abstract fun gameDao(): GameDao

    companion object {
        const val DATABASE_NAME = "app_database"
    }
}