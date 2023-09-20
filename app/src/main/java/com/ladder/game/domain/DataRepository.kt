package com.ladder.game.domain

import com.ladder.game.core.library.l
import com.ladder.game.data.data_base.Database
import com.ladder.game.data.data_base.entities.GameDB
import com.ladder.game.data.data_base.entities.ResultDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DataRepository {

    suspend fun getResultList(yourResult: Int): List<Pair<Int, Int>> {
        return suspendCoroutine { continuation ->
            CoroutineScope(Dispatchers.Default).launch {
                val _list = Database.instance.dao().getAllResults()
                val list = _list.sortedBy { it.amount }.reversed()

                l(list.toString())
                when (list.size) {
                    1 -> {
                        continuation.resume(
                            listOf(
                                1 to list[0].amount,
                                2 to 0,
                                1 to list[0].amount
                            )
                        )
                    }
                    else -> {
                        val yourItem = list.find { it.amount == yourResult }
                        continuation.resume(
                            listOf(
                                1 to list[0].amount,
                                2 to list[1].amount,
                                list.indexOf(yourItem) + 1 to (yourItem?.amount ?: 0)
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun addResult(result: Int) {
        withContext(Dispatchers.Default) {
            Database.instance.dao().addResult(ResultDB(0, result))
        }
    }

    suspend fun isGameExists(): GameDB? {
        return suspendCoroutine { continuation ->
            CoroutineScope(Dispatchers.Default).launch {
                val savedGame = Database.instance.gameDao().getGame()
                if (savedGame.isNotEmpty()) {
                    continuation.resume(savedGame[0])
                } else {
                    continuation.resume(null)
                }
            }
        }
    }

    suspend fun deleteGame() {
        withContext(Dispatchers.Default) {
            Database.instance.gameDao().deleteGame()
        }
    }

    suspend fun saveGame(stars: Int, spawnDelay: Int, distance: Int) {
        withContext(Dispatchers.Default) {
            val game = Database.instance.gameDao().getGame()
            if (game.isNotEmpty()) {
                val newGame = game[0]
                newGame.spawnDelay = spawnDelay
                newGame.distance = distance
                newGame.stars = stars
                Database.instance.gameDao().updateGame(newGame)
            } else {
                Database.instance.gameDao().saveGame(GameDB(0, spawnDelay, distance, stars))
            }
        }
    }
}