package com.example.composition.presentation

import android.app.Application
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.composition.R
import com.example.composition.data.GameRepositoryImpl
import com.example.composition.databinding.FragmentChooseLevelBinding
import com.example.composition.databinding.FragmentGameBinding
import com.example.composition.domain.entity.GameResult
import com.example.composition.domain.entity.GameSettings
import com.example.composition.domain.entity.Level
import com.example.composition.domain.entity.Question
import com.example.composition.domain.usecases.GenerateQuestionUseCase
import com.example.composition.domain.usecases.GetGameSettingsUseCase

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var gameSettings: GameSettings
    private lateinit var level: Level
    private val context = application

    private val repository = GameRepositoryImpl
    private val getSettingsUseCase = GetGameSettingsUseCase(repository)
    private val generateQuestionUseCase = GenerateQuestionUseCase(repository)

    private val _currentQuestion = MutableLiveData<Question>()
    val currentQuestion: LiveData<Question>
        get() = _currentQuestion

    private var _formattedTime = MutableLiveData<String>()
    val formattedTime: LiveData<String>
        get() = _formattedTime

    private var _percentOfRightAnswers = MutableLiveData<Int>()
    val percentOfRightAnswers: LiveData<Int>
        get() = _percentOfRightAnswers

    private var _progressAnswers = MutableLiveData<String>()
    val progressAnswers: LiveData<String>
        get() = _progressAnswers


    private var _enoughCountOfRightAnswers = MutableLiveData<Boolean>()
    val enoughCountOfRightAnswers: LiveData<Boolean>
        get() = _enoughCountOfRightAnswers

    private var _enoughPercentOfRightAnswers = MutableLiveData<Boolean>()
    val enoughPercentOfRightAnswers: LiveData<Boolean>
        get() = _enoughPercentOfRightAnswers

    private var _minPercent = MutableLiveData<Int>()
    val minPercent: LiveData<Int>
        get() = _minPercent

    private var _gameResult = MutableLiveData<GameResult>()
    val gameResult: LiveData<GameResult>
        get() = _gameResult

    private var timer: CountDownTimer? = null
    private var countOfRightAnswers = 0
    private var countOfQuestion = 0


    fun startGame(level: Level) {
        getGameSettings(level)
        startTimer()
        generateQuestion()
        updateProgress()
    }

    private fun getGameSettings(level: Level) {
        this.level = level
        gameSettings = getSettingsUseCase(level)
        _minPercent.postValue(gameSettings.minPercentOfRightAnswers)
    }

    private fun startTimer() {
        timer = object : CountDownTimer(
            gameSettings.gameTimeInSeconds * MS_IN_SECONDS,
            MS_IN_SECONDS
        ) {
            override fun onTick(msUntilFinished: Long) {
                _formattedTime.postValue(formatTime(msUntilFinished))
            }

            override fun onFinish() {
                finishGame()
            }
        }
        timer?.start()
    }

    private fun generateQuestion() {
        val question = generateQuestionUseCase(gameSettings.maxSumValue)
        _currentQuestion.postValue(question)
    }

    fun chooseAnswer(answer: Int) {
        checkAnswer(answer)
        generateQuestion()
        updateProgress()
    }

    private fun checkAnswer(answer: Int) {
        val correctAnswer = currentQuestion.value?.correctAnswer

        countOfQuestion++
        if (correctAnswer == answer) {
            countOfRightAnswers++
        }
    }

    private fun updateProgress() {
        val percent = calculatePercentOfRightAnswers()
        _percentOfRightAnswers.postValue(percent)
        _progressAnswers.postValue(
            String.format(
                context.resources.getString(R.string.progress_answers),
                countOfRightAnswers,
                gameSettings.minCountOfRightAnswers
            )
        )

        _enoughCountOfRightAnswers.postValue(countOfRightAnswers >= gameSettings.minCountOfRightAnswers)
        _enoughPercentOfRightAnswers.postValue(percent >= gameSettings.minPercentOfRightAnswers)
    }

    private fun calculatePercentOfRightAnswers(): Int {
        if (countOfRightAnswers == 0) {
            return 0
        }

        return ((countOfRightAnswers / countOfQuestion.toDouble()) * 100).toInt()
    }

    private fun formatTime(msUntilFinished: Long): String {
        val seconds = msUntilFinished / MS_IN_SECONDS
        val minutes = seconds / SECONDS_IN_MINUTES
        val leftSeconds = seconds - (minutes * SECONDS_IN_MINUTES)

        /**
         * "%02d:%02d"
         * Вставить два числа, якщо воно менше за 10 до достасть до нього 0. Це для того щоб було ось так 05:08 (5 хвилин і 8 секунд)
         */
        return String.format("%02d:%02d", minutes, leftSeconds)
    }

    private fun finishGame() {
        val gameResult = GameResult(
            isWin = enoughCountOfRightAnswers.value == true && enoughPercentOfRightAnswers.value == true,
            countOfRightAnswers = countOfRightAnswers,
            countOfQuestion = countOfQuestion,
            gameSettings = gameSettings
        )
        _gameResult.postValue(gameResult)
    }

    override fun onCleared() {
        timer?.cancel()
    }

    companion object {
        const val MS_IN_SECONDS = 1000L
        const val SECONDS_IN_MINUTES = 60
    }

}