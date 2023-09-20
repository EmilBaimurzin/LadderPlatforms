package com.ladder.game.domain

import com.ladder.game.core.library.XY

data class Platform(
    override var x: Float,
    override var y: Float,
    val hasLadder: Boolean = false,
    val isLadderUp: Boolean = false,
    val platformSize: PlatformSize,
    val hasStarLeft: Boolean = false,
    val hasStarMiddle: Boolean = false,
    val hasStarRight: Boolean = false,
): XY
