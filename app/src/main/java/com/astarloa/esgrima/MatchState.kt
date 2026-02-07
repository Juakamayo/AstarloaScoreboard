package com.astarloa.esgrima

data class MatchState(
    var leftScore: Int = 0,
    var rightScore: Int = 0,
    var leftYellow: Boolean = false,
    var rightYellow: Boolean = false,
    var leftRed: Boolean = false,
    var rightRed: Boolean = false,

    var restActive: Boolean = false,
    var restPaused: Boolean = false,
    var restSecondsRemaining: Int = 0
)
