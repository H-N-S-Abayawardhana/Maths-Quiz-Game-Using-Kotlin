package com.example.mathsquizgame

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Random

class MainActivity : AppCompatActivity() {

    private val soundManager = SoundManager(this)

    private var selectedOperation: Int = ADDITION

    companion object {
        const val ADDITION = 0
        const val SUBTRACTION = 1
        const val MULTIPLICATION = 2
        const val DIVISION = 3
        const val RANDOM = 4
    }

    private lateinit var startButton: LinearLayout
    private lateinit var subtractButton: LinearLayout
    private lateinit var multiplyButton: LinearLayout
    private lateinit var divisionButton: LinearLayout
    private lateinit var randomButton: LinearLayout
    private lateinit var gamelayout: LinearLayout
    private lateinit var dashbord: RelativeLayout
    private lateinit var gameover: RelativeLayout
    private lateinit var questionTextView: TextView
    private lateinit var option1Button: Button
    private lateinit var option2Button: Button
    private lateinit var option3Button: Button
    private lateinit var option4Button: Button
    private lateinit var exitButton: Button
    private lateinit var gotohome: Button
    private lateinit var finalscoretv: TextView
    private lateinit var timerTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var questionNumberTextView: Button
    private lateinit var highScoreTextView: TextView

    private var currentQuestionNumber: Int = 1

    private val random = Random()
    private var score = 0
    private var currentQuestionIndex = 0
    private lateinit var currentQuestion: Question
    private var timer: CountDownTimer? = null

    private lateinit var scoreTextView: TextView
    private val correctColor: Int by lazy { ContextCompat.getColor(this, R.color.correctColor) }
    private val incorrectColor: Int by lazy { ContextCompat.getColor(this, R.color.incorrectColor) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val exitLayout: LinearLayout = findViewById(R.id.exit)
        val exitButton : Button = findViewById(R.id.exitbutton)
        val gotohome : Button = findViewById(R.id.gotohome)

        val exitClickListener = View.OnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Exit")
                .setMessage("Are you sure you want to exit the app?")
            builder.setPositiveButton("Yes") { dialog, which ->
                finish()
            }
            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        exitLayout.setOnClickListener(exitClickListener)
        exitButton.setOnClickListener(exitClickListener)

        startButton = findViewById(R.id.addition_layout)
        subtractButton = findViewById(R.id.subtract_button)
        multiplyButton = findViewById(R.id.multiply_button)
        divisionButton = findViewById(R.id.division_button)
        randomButton = findViewById(R.id.random_button)
        gamelayout = findViewById(R.id.gamelayout)
        dashbord = findViewById(R.id.dashboard)
        gameover = findViewById(R.id.gameoverscreen)
        progressBar = findViewById(R.id.progressBar)
        timerTextView = findViewById(R.id.timerTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        finalscoretv = findViewById(R.id.final_score)
        questionNumberTextView = findViewById(R.id.questionNumberTextView)
        highScoreTextView = findViewById(R.id.highScoreTextView)

        gamelayout.visibility = View.GONE

        startButton.setOnClickListener {
            val durationInMillis: Long = 10000
            val intervalInMillis: Long = 100
            object : CountDownTimer(durationInMillis, intervalInMillis) {
                override fun onTick(millisUntilFinished: Long) {
                    val progress = (millisUntilFinished * 100 / durationInMillis).toInt()
                    progressBar.progress = progress
                    val secondsRemaining = (millisUntilFinished / 1000).toInt()
                    timerTextView.text = secondsRemaining.toString()
                }
                override fun onFinish() {
                    timerTextView.text = "0"
                }
            }.start()
        }

        questionTextView = findViewById(R.id.questionTextView)
        option1Button = findViewById(R.id.option1Button)
        option2Button = findViewById(R.id.option2Button)
        option3Button = findViewById(R.id.option3Button)
        option4Button = findViewById(R.id.option4Button)

        startButton.setOnClickListener {
            // Default to addition if the user starts the game without selecting an operation
            startGame(ADDITION)
        }

        subtractButton.setOnClickListener {
            selectedOperation = SUBTRACTION
            startGame(SUBTRACTION)
        }

        multiplyButton.setOnClickListener {
            selectedOperation = MULTIPLICATION
            startGame(MULTIPLICATION)
        }

        divisionButton.setOnClickListener {
            selectedOperation = DIVISION
            startGame(DIVISION)
        }

        randomButton.setOnClickListener {
            selectedOperation = RANDOM
            startGame(RANDOM)
        }

        option1Button.setOnClickListener { onOptionSelected(it) }
        option2Button.setOnClickListener { onOptionSelected(it) }
        option3Button.setOnClickListener { onOptionSelected(it) }
        option4Button.setOnClickListener { onOptionSelected(it) }

        changeStatusBarColor("#673AB7") // Replace with your desired color code

        gotohome.setOnClickListener {
            gameover.visibility = View.GONE
            dashbord.visibility = View.VISIBLE
        }

        val playagain =  findViewById<Button>(R.id.play_again)

        playagain.setOnClickListener {
            startGame(selectedOperation)
            gameover.visibility = View.GONE
        }

        highScoreTextView = findViewById(R.id.highScoreTextView)
        loadHighScore()
    }

    private fun changeStatusBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = android.graphics.Color.parseColor(color)
        }
    }

    private fun startGame(selectedOperation: Int) {
        option1Button.setBackgroundResource(R.drawable.rounded_button_background)
        option2Button.setBackgroundResource(R.drawable.rounded_button_background)
        option3Button.setBackgroundResource(R.drawable.rounded_button_background)
        option4Button.setBackgroundResource(R.drawable.rounded_button_background)

        dashbord.visibility = View.GONE
        gamelayout.visibility = View.VISIBLE
        score = 0
        currentQuestionIndex = 0
        showNextQuestion(selectedOperation)
        score = 0
        updateScoreDisplay()
    }

    private fun showNextQuestion(selectedOperation: Int) {
        currentQuestion = generateRandomQuestion(selectedOperation)
        updateQuestionUI()
        startTimer()
        questionNumberTextView.text = "    Question $currentQuestionNumber/50    "
        currentQuestionNumber++
    }

    private fun updateQuestionUI() {
        questionTextView.text = currentQuestion.question
        val options = currentQuestion.options
        option1Button.text = options[0]
        option2Button.text = options[1]
        option3Button.text = options[2]
        option4Button.text = options[3]
    }

    private fun startTimer() {
        timer?.cancel()
        var timeLeft = 10000L
        progressBar.progress = 100
        val duration = 10000L // Total duration of the timer in milliseconds
        val interval = 50L // Update interval in milliseconds

        timer = object : CountDownTimer(duration, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((duration - millisUntilFinished) * 100 / duration).toInt()
                progressBar.progress = progress
                timeLeft = millisUntilFinished
                timerTextView.text = "${millisUntilFinished / 1000}"
            }
            override fun onFinish() {
                progressBar.progress = 100
                timerTextView.text = "Time's up!"
                endGame()
            }
        }.start()

        soundManager.stopSound()
        soundManager.playSound(R.raw.timer)
    }

    private fun generateRandomQuestion(operation: Int): Question {
        val num1 = random.nextInt(100)
        val num2 = when (operation) {
            ADDITION, SUBTRACTION, MULTIPLICATION -> random.nextInt(100) + 1 // Adjust the range as needed
            DIVISION -> generateRandomDivisor(num1)
            else -> random.nextInt(100) + 1 // Adjust the range as needed for other cases
        }

        val question: String
        val correctAnswer: Int

        when (operation) {
            ADDITION -> {
                question = "$num1 + $num2?"
                correctAnswer = num1 + num2
            }
            SUBTRACTION -> {
                question = "$num1 - $num2?"
                correctAnswer = num1 - num2
            }
            MULTIPLICATION -> {
                question = "$num1 * $num2?"
                correctAnswer = num1 * num2
            }
            DIVISION -> {
                question = "$num1 / $num2?"
                correctAnswer = num1 / num2
            }
            RANDOM -> {
                // Randomly choose an operation for the RANDOM case
                val randomOperation = random.nextInt(4)
                return generateRandomQuestion(randomOperation)
            }
            else -> {
                // Default to addition for any other case
                question = "$num1 + $num2?"
                correctAnswer = num1 + num2
            }
        }

        val options = generateOptions(correctAnswer)

        return Question(question, options, options.indexOf(correctAnswer.toString()))
    }

    private fun generateRandomDivisor(dividend: Int): Int {
        // Generate a random divisor that evenly divides the dividend
        val possibleDivisors = (1..dividend).filter { dividend % it == 0 }
        return possibleDivisors.random()
    }

    private fun generateOptions(correctAnswer: Int): List<String> {
        val options = mutableListOf<String>()
        options.add(correctAnswer.toString())

        while (options.size < 4) {
            val wrongAnswer = correctAnswer + random.nextInt(20) - 10
            if (wrongAnswer != correctAnswer && !options.contains(wrongAnswer.toString())) {
                options.add(wrongAnswer.toString())
            }
        }

        return options.shuffled()
    }

    private fun endGame() {
        Toast.makeText(this, "Game Over! Your score: $score", Toast.LENGTH_SHORT).show()
        finalscoretv.setText("Score $score")

        val highScore = getHighScore()
        if (score > highScore) {
            saveHighScore(score)
            highScoreTextView.text = "High Score: $score"
        } else {
            highScoreTextView.text = "High Score: $highScore"
        }

        soundManager.stopSound()
        soundManager.playSound(R.raw.gameover)

        Handler(Looper.getMainLooper()).postDelayed({
            gameover.visibility = View.VISIBLE
            gamelayout.visibility = View.GONE
            soundManager.stopSound()
        }, 1000)

        timer?.cancel()
        currentQuestionNumber = 1
    }

    private fun saveHighScore(score: Int) {
        val sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("highScore", score)
        editor.apply()
    }

    private fun getHighScore(): Int {
        val sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("highScore", 0)
    }

    private fun loadHighScore() {
        val highScore = getHighScore()
        highScoreTextView.text = "High Score: $highScore"
    }

    fun onOptionSelected(view: View) {
        val selectedOption = when (view.id) {
            R.id.option1Button -> 0
            R.id.option2Button -> 1
            R.id.option3Button -> 2
            R.id.option4Button -> 3
            else -> -1
        }

        if (selectedOption == currentQuestion.correctOption) {
            score += 10
            updateScoreDisplay()
            view.setBackgroundColor(correctColor)
            soundManager.stopSound()
            soundManager.playSound(R.raw.correctanswer)
        } else {
            view.setBackgroundColor(incorrectColor)
            Toast.makeText(this, "Wrong! Game Over.", Toast.LENGTH_SHORT).show()
            endGame()
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            view.setBackgroundResource(R.drawable.rounded_button_background)
            currentQuestionIndex++
            showNextQuestion(selectedOperation)
        }, 1000)
    }

    private fun updateScoreDisplay() {
        scoreTextView.text = "Score: $score"
    }
}
