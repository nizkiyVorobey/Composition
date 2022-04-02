package com.example.composition.domain.entity

data class GameResult(
    val isWin: Boolean,
    val countOfRightAnswers: Int,
    val countOfQuestion: Int,
    val gameSettings: GameSettings
)