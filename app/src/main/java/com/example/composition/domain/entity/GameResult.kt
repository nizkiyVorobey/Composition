package com.example.composition.domain.entity

import java.io.Serializable

data class GameResult(
    val isWin: Boolean,
    val countOfRightAnswers: Int,
    val countOfQuestion: Int,
    val gameSettings: GameSettings
) : Serializable