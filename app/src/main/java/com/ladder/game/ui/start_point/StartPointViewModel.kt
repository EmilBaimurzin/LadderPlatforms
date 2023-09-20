package com.ladder.game.ui.start_point

import androidx.lifecycle.ViewModel
import com.ladder.game.data.data_base.entities.GameDB
import com.ladder.game.domain.DataRepository
import kotlin.coroutines.suspendCoroutine

class StartPointViewModel: ViewModel() {
    private val repository = DataRepository()
    suspend fun getGame(): GameDB? {
        return repository.isGameExists()
    }

    suspend fun deleteGame() {
        repository.deleteGame()
    }
}