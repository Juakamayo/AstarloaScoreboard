package com.astarloa.esgrima

data class MatchState(
    // Datos comunes (sable y espada)
    var leftScore: Int = 0,
    var rightScore: Int = 0,
    var leftYellow: Boolean = false,
    var rightYellow: Boolean = false,
    var leftRed: Boolean = false,
    var rightRed: Boolean = false,

    // Descanso (común para ambas modalidades)
    var restActive: Boolean = false,
    var restPaused: Boolean = false,
    var restSecondsRemaining: Int = 0,

    // Específico para Florete/Espada
    var timerSeconds: Int = 180,  // Timer del combate (3 minutos por defecto)
    var timerRunning: Boolean = false,
    var timerPaused: Boolean = false,
    var currentRound: Int = 1,  // Ronda actual (1-3) (MAX 3) (cambiar de ser necesario?)
    var priorityActive: Boolean = false,  // Si hay prioridad asignada
    var priorityLeft: Boolean = false  // true = izquierda, false = derecha
)
