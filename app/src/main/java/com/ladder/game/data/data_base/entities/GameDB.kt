package com.ladder.game.data.data_base.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GameDB (
    @PrimaryKey (autoGenerate = true) val id: Int,
    var spawnDelay: Int,
    var distance: Int,
    var stars: Int
)