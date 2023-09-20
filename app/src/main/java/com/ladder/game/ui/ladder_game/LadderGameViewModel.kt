package com.ladder.game.ui.ladder_game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ladder.game.core.library.GameViewModel
import com.ladder.game.core.library.XY
import com.ladder.game.core.library.XYIMpl
import com.ladder.game.core.library.l
import com.ladder.game.core.library.random
import com.ladder.game.domain.DataRepository
import com.ladder.game.domain.Platform
import com.ladder.game.domain.PlatformSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random

class LadderGameViewModel : GameViewModel() {
    private val repository = DataRepository()
    private val _platforms = MutableLiveData<List<Platform>>(emptyList())
    val platforms: LiveData<List<Platform>> = _platforms

    private val _stars = MutableLiveData<List<XYIMpl>>(emptyList())
    val stars: LiveData<List<XYIMpl>> = _stars

    private val _points = MutableLiveData(0)
    val points: LiveData<Int> = _points

    var spawnDelay = 1800L
    var isSpawning = false
    var distance = 5

    var jumpScope = CoroutineScope(Dispatchers.Default)
    var spawnScope = CoroutineScope(Dispatchers.Default)
    var moveScope = CoroutineScope(Dispatchers.Default)
    var saveScope = CoroutineScope(Dispatchers.Default)

    var isGoingDown = true
    var isGoingLeft = false
    var isGoingRight = false
    var isGoingUp = false
    var isStopped = true
    var canGoDown = true

    var isInitial = true


    init {
        _playerXY.postValue(XYIMpl(0f, 0f))

        viewModelScope.launch {
            delay(6000)
            isInitial = false
        }

        viewModelScope.launch {
            val game = repository.isGameExists()
            if (game != null) {
                spawnDelay = game.spawnDelay.toLong()
                distance = game.distance
                _points.postValue(game.stars)
            }
        }
    }

    fun deleteGame() {
        viewModelScope.launch {
            repository.deleteGame()
        }
    }

    fun initPlayer(x: Float, y: Float) {
        _playerXY.postValue(XYIMpl(x, y))
    }

    fun addResult() {
        viewModelScope.launch {
            repository.addResult(_points.value!!)
        }
    }

    private fun increaseSpeed() {
        gameScope.launch {
            while (true) {
                delay(5000)
                if (distance != 15) {
                    distance += 1
                }
                if (spawnDelay != 500L) {
                    spawnDelay -= 100
                }
            }
        }
    }

    private fun saveGame() {
        saveScope.launch {
            while (true) {
                delay(1000)
                repository.saveGame(_points.value!!, spawnDelay.toInt(), distance)
            }
        }
    }

    override fun stop() {
        spawnScope.cancel()
        gameScope.cancel()
        saveScope.cancel()
        jumpScope.cancel()
        moveScope.cancel()
    }

    fun start(
        maxX: Int,
        y1: Int,
        y2: Int,
        y3: Int,
        y4: Int,
        widthExtraLarge: Int,
        widthLarge: Int,
        widthMedium: Int,
        widthSmall: Int,
        ladderSpace: Int,
        playerHeight: Int,
        playerWidth: Int,
        platformHeight: Int,
        starSize: Int
    ) {
        gameScope = CoroutineScope(Dispatchers.Default)
        saveScope = CoroutineScope(Dispatchers.Default)
        spawnScope = CoroutineScope(Dispatchers.Default)
        moveScope = CoroutineScope(Dispatchers.Default)
        isSpawning = false
        generatePlatforms(
            maxX,
            y1,
            y2,
            y3,
            y4,
            widthExtraLarge,
            widthLarge,
            widthMedium,
            widthSmall,
            ladderSpace,
            starSize
        )
        letEverythingMove(
            widthExtraLarge,
            widthLarge,
            widthMedium,
            widthSmall,
            playerHeight,
            playerWidth,
            platformHeight,
            starSize
        )
        increaseSpeed()
        saveGame()
    }

    private fun generatePlatforms(
        maxX: Int,
        y1: Int,
        y2: Int,
        y3: Int,
        y4: Int,
        widthExtraLarge: Int,
        widthLarge: Int,
        widthMedium: Int,
        widthSmall: Int,
        ladderSpace: Int,
        starSize: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(spawnDelay)
                l("spawns")
                isSpawning = true
                spawnScope = CoroutineScope(Dispatchers.Default)
                moveScope.cancel()
                spawnScope.launch {
                    val currentList = _platforms.value!!.toMutableList()
                    val randomSize = listOf(
                        PlatformSize.EXTRA_LARGE,
                        PlatformSize.LARGE,
                        PlatformSize.MEDIUM,
                        PlatformSize.SMALL
                    ).random()
                    val randomY = if (currentList.isNotEmpty()) {
                        val lastY = currentList.last().y
                        val platformNum = when {
                            lastY.toInt() == y1 -> 1
                            lastY.toInt() == y2 -> 2
                            lastY.toInt() == y3 -> 3
                            else -> 4
                        }
                        val numList = mutableListOf(1, 2, 3, 4)
                        numList.remove(platformNum)
                        numList.random()
                    } else 1 random 4
                    when (randomY) {
                        1 -> {
                            val platform = Platform(
                                maxX.toFloat(),
                                y1.toFloat(),
                                Random().nextBoolean(),
                                false,
                                randomSize
                            )
                            val size = when (platform.platformSize) {
                                PlatformSize.EXTRA_LARGE -> widthExtraLarge
                                PlatformSize.LARGE -> widthLarge
                                PlatformSize.MEDIUM -> widthMedium
                                PlatformSize.SMALL -> widthSmall
                            }

                            val currentStarsList = _stars.value!!.toMutableList()

                            repeat(1 random 3) {
                                currentStarsList.add(
                                    XYIMpl(
                                        (platform.x.toInt()..(platform.x.toInt() + size)).random()
                                            .toFloat(), platform.y - starSize
                                    )
                                )
                            }

                            _stars.postValue(currentStarsList)

                            currentList.add(platform)
                            if (platform.hasLadder) {
                                val bonusPlatform = Platform(
                                    (maxX.toFloat() + size - ladderSpace),
                                    y2.toFloat(),
                                    false,
                                    false,
                                    randomSize
                                )
                                repeat(1 random 3) {
                                    currentStarsList.add(
                                        XYIMpl(
                                            (bonusPlatform.x.toInt()..(bonusPlatform.x.toInt() + size)).random()
                                                .toFloat(), bonusPlatform.y - starSize
                                        )
                                    )
                                }

                                _stars.postValue(currentStarsList)
                                currentList.add(bonusPlatform)
                            }
                            _platforms.postValue(currentList)
                            delay(5)
                            _platforms.postValue(currentList)
                            delay(5)
                            _platforms.postValue(currentList)
                        }

                        2 -> {
                            val randomLadder = listOf(true, false).random()
                            val platform = Platform(
                                maxX.toFloat(),
                                y2.toFloat(),
                                randomLadder,
                                Random().nextBoolean(),
                                randomSize
                            )
                            val size = when (platform.platformSize) {
                                PlatformSize.EXTRA_LARGE -> widthExtraLarge
                                PlatformSize.LARGE -> widthLarge
                                PlatformSize.MEDIUM -> widthMedium
                                PlatformSize.SMALL -> widthSmall
                            }

                            val currentStarsList = _stars.value!!.toMutableList()

                            repeat(1 random 3) {
                                currentStarsList.add(
                                    XYIMpl(
                                        (platform.x.toInt()..(platform.x.toInt() + size)).random()
                                            .toFloat(), platform.y - starSize
                                    )
                                )
                            }

                            _stars.postValue(currentStarsList)

                            currentList.add(platform)
                            if (platform.hasLadder) {
                                val bonusPlatform = Platform(
                                    (maxX.toFloat() + size - ladderSpace),
                                    if (platform.isLadderUp) y1.toFloat() else y3.toFloat(),
                                    false,
                                    false,
                                    randomSize
                                )
                                repeat(1 random 3) {
                                    currentStarsList.add(
                                        XYIMpl(
                                            (bonusPlatform.x.toInt()..(bonusPlatform.x.toInt() + size)).random()
                                                .toFloat(), bonusPlatform.y - starSize
                                        )
                                    )
                                }

                                _stars.postValue(currentStarsList)
                                currentList.add(bonusPlatform)
                            }
                            _platforms.postValue(currentList)
                            delay(5)
                            _platforms.postValue(currentList)
                            delay(5)
                            _platforms.postValue(currentList)
                        }

                        3 -> {
                            val randomLadder = listOf(true, false).random()
                            val platform = Platform(
                                maxX.toFloat(),
                                y3.toFloat(),
                                randomLadder,
                                Random().nextBoolean(),
                                randomSize
                            )
                            val size = when (platform.platformSize) {
                                PlatformSize.EXTRA_LARGE -> widthExtraLarge
                                PlatformSize.LARGE -> widthLarge
                                PlatformSize.MEDIUM -> widthMedium
                                PlatformSize.SMALL -> widthSmall
                            }

                            val currentStarsList = _stars.value!!.toMutableList()

                            repeat(1 random 3) {
                                currentStarsList.add(
                                    XYIMpl(
                                        (platform.x.toInt()..(platform.x.toInt() + size)).random()
                                            .toFloat(), platform.y - starSize
                                    )
                                )
                            }

                            _stars.postValue(currentStarsList)

                            currentList.add(platform)
                            if (platform.hasLadder) {
                                val bonusPlatform = Platform(
                                    (maxX.toFloat() + size - ladderSpace),
                                    if (platform.isLadderUp) y2.toFloat() else y4.toFloat(),
                                    false,
                                    false,
                                    randomSize
                                )
                                repeat(1 random 3) {
                                    currentStarsList.add(
                                        XYIMpl(
                                            (bonusPlatform.x.toInt()..(bonusPlatform.x.toInt() + size)).random()
                                                .toFloat(), bonusPlatform.y - starSize
                                        )
                                    )
                                }

                                _stars.postValue(currentStarsList)
                                currentList.add(bonusPlatform)
                            }
                            _platforms.postValue(currentList)
                            delay(5)
                            _platforms.postValue(currentList)
                            delay(5)
                            _platforms.postValue(currentList)
                        }

                        else -> {
                            val platform = Platform(
                                maxX.toFloat(),
                                y4.toFloat(),
                                Random().nextBoolean(),
                                true,
                                randomSize
                            )
                            val size = when (platform.platformSize) {
                                PlatformSize.EXTRA_LARGE -> widthExtraLarge
                                PlatformSize.LARGE -> widthLarge
                                PlatformSize.MEDIUM -> widthMedium
                                PlatformSize.SMALL -> widthSmall
                            }

                            val currentStarsList = _stars.value!!.toMutableList()

                            repeat(1 random 3) {
                                currentStarsList.add(
                                    XYIMpl(
                                        (platform.x.toInt()..(platform.x.toInt() + size)).random()
                                            .toFloat(), platform.y - starSize
                                    )
                                )
                            }

                            _stars.postValue(currentStarsList)

                            currentList.add(platform)
                            if (platform.hasLadder) {
                                val bonusPlatform = Platform(
                                    (maxX.toFloat() + size - ladderSpace),
                                    y3.toFloat(),
                                    false,
                                    false,
                                    randomSize
                                )
                                repeat(1 random 3) {
                                    currentStarsList.add(
                                        XYIMpl(
                                            (bonusPlatform.x.toInt()..(bonusPlatform.x.toInt() + size)).random()
                                                .toFloat(), bonusPlatform.y - starSize
                                        )
                                    )
                                }

                                _stars.postValue(currentStarsList)
                                currentList.add(bonusPlatform)
                            }
                            _platforms.postValue(currentList)
                            delay(5)
                            _platforms.postValue(currentList)
                            delay(5)
                            _platforms.postValue(currentList)
                        }
                    }
                    moveScope = CoroutineScope(Dispatchers.Default)
                    isSpawning = false
                    spawnScope.cancel()
                }
            }
        }
    }

    fun jump(
        y1: Int,
        y2: Int,
        widthExtraLarge: Int,
        widthLarge: Int,
        widthMedium: Int,
        widthSmall: Int,
        ladderSpace: Int,
        playerHeight: Int,
        playerWidth: Int,
    ) {
        isInitial = false
        var canContinue = true
        _platforms.value!!.forEach { platform ->
            val size = when (platform.platformSize) {
                PlatformSize.EXTRA_LARGE -> widthExtraLarge
                PlatformSize.LARGE -> widthLarge
                PlatformSize.MEDIUM -> widthMedium
                PlatformSize.SMALL -> widthSmall
            }
            val ladderY =
                (platform.y.toInt())..(platform.y.toInt() + if (platform.isLadderUp) ladderSpace else -ladderSpace + 10)
            val ladderX = (platform.x.toInt() + size - ladderSpace)..(platform.x.toInt() + size)

            val playerX = _playerXY.value!!.x.toInt()..(_playerXY.value!!.x + playerWidth).toInt()
            val playerY = _playerXY.value!!.y.toInt()..(_playerXY.value!!.y + playerHeight).toInt()

            if (platform.hasLadder && playerX.any { it in ladderX } && playerY.any { it in ladderY }) {
                if (platform.isLadderUp) {
                    _playerXY.postValue(
                        XYIMpl(
                            _playerXY.value!!.x,
                            _playerXY.value!!.y - (y2 - y1)
                        )
                    )
                } else {
                    _playerXY.postValue(
                        XYIMpl(
                            _playerXY.value!!.x,
                            _playerXY.value!!.y + (y2 - y1)
                        )
                    )
                }
                canContinue = false
                return
            }
        }

        if (canContinue) {
            jumpScope.cancel()
            jumpScope = CoroutineScope(Dispatchers.Default)
            isStopped = false
            isGoingUp = true
            isGoingDown = false
            jumpScope.launch {
                repeat(10) { r ->
                    repeat(5) {
                        delay(16)
                        val xy = _playerXY.value!!
                        if (isGoingLeft) {
                            xy.x = xy.x - distance / 2
                        }
                        if (isGoingRight) {
                            xy.x = xy.x + distance / 2
                        }
                        xy.y = xy.y - (10 - r + 1)
                        _playerXY.postValue(xy)
                    }
                }

                isGoingDown = true

                repeat(10) { r ->
                    repeat(5) {
                        delay(16)
                        val xy = _playerXY.value!!
                        if (isGoingLeft) {
                            xy.x = xy.x - distance / 2
                        }
                        if (isGoingRight) {
                            xy.x = xy.x + distance / 2
                        }
                        xy.y = xy.y + (r + 1)
                        _playerXY.postValue(xy)
                    }
                }

                while (true) {
                    delay(16)
                    if (!isStopped) {
                        val xy = _playerXY.value!!
                        if (isGoingLeft) {
                            xy.x = xy.x - distance / 2
                        }
                        if (isGoingRight) {
                            xy.x = xy.x + distance / 2
                        }
                        xy.y = xy.y + (10)
                        _playerXY.postValue(xy)
                    }
                }
            }
        }
    }

    private fun letEverythingMove(
        widthExtraLarge: Int,
        widthLarge: Int,
        widthMedium: Int,
        widthSmall: Int,
        playerHeight: Int,
        playerWidth: Int,
        platformHeight: Int,
        starSize: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(10)
                if (!spawnScope.isActive && moveScope.isActive) {
                    moveScope.launch {
                        var needToFall = true
                        val currentList = _platforms.value!!.toMutableList()
                        val newList = mutableListOf<Platform>()
                        currentList.forEach { platform ->
                            val size = when (platform.platformSize) {
                                PlatformSize.EXTRA_LARGE -> widthExtraLarge
                                PlatformSize.LARGE -> widthLarge
                                PlatformSize.MEDIUM -> widthMedium
                                PlatformSize.SMALL -> widthSmall
                            }
                            //____________________________________________________
                            val platformY =
                                platform.y.toInt()..(platform.y + platformHeight).toInt()
                            val platformX = platform.x.toInt()..(platform.x + size).toInt()

                            val player = _playerXY.value!!

                            val playerY =
                                (player.y + playerHeight / 1.2).toInt()..(player.y + playerHeight).toInt()
                            val playerX = player.x.toInt()..(player.x + playerWidth).toInt()

                            if (playerY.any { it in platformY } && playerX.any { it in platformX } && isGoingDown) {
                                isStopped = true
                                jumpScope.cancel()
                                player.x = player.x - distance

                                if (isGoingRight) {
                                    player.x = player.x + distance * 2
                                }

                                needToFall = false

                                _playerXY.postValue(
                                    XYIMpl(
                                        player.x,
                                        platform.y - playerHeight + platformHeight / 4
                                    )
                                )

                            }

                            //____________________________________________________
                            platform.x = platform.x - distance
                            if (platform.x + size > 0) {
                                newList.add(platform)
                            }
                        }
                        if (needToFall && !jumpScope.isActive) {
                            val xy = _playerXY.value!!
                            if (isGoingLeft) {
                                xy.x = xy.x - distance / 2
                            }
                            if (isGoingRight) {
                                xy.x = xy.x + distance / 2
                            }
                            _playerXY.postValue(XYIMpl(xy.x, xy.y + 5))
                        }
                        _platforms.postValue(newList)
                        val currentStarsList = _stars.value!!

                        _stars.postValue(
                            moveSomethingLeft(
                                starSize,
                                starSize,
                                playerWidth,
                                playerHeight,
                                currentStarsList.toMutableList(),
                                { star ->
                                    _points.postValue(_points.value!! + 1)
                                },
                                {},
                                distance
                            ).toList() as List<XYIMpl>
                        )
                    }
                }
            }
        }
    }

    fun stopMoving() {
        isGoingLeft = false
        isGoingRight = false
    }

    fun goDown(downDistance: Int) {
        viewModelScope.launch {
            if (isStopped && canGoDown) {
                canGoDown = false
                _playerXY.postValue(XYIMpl(_playerXY.value!!.x, _playerXY.value!!.y + downDistance))
                delay(500)
                canGoDown = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        jumpScope.cancel()
    }
}