package com.ladder.game.data.data_base.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ResultDB (
    @PrimaryKey (autoGenerate = true) val id: Int,
    val amount: Int
)