package com.example.quizapp

// Android and Jetpack Compose imports for UI components and functionality
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quizapp.ui.theme.QuizAppTheme
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * Main entry point for the Quiz Application.
 * Sets up the theme and initial composable structure.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge display
        setContent {
            QuizAppTheme {
                QuizApp() // Main composable that handles the app screens
            }
        }
    }
}

/**
 * Data class representing a quiz question with multiple choice options.
 * @param question The question text
 * @param options List of possible answers
 * @param correctAnswerIndex Index of the correct answer in the options list
 */
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

/**
 * Defines the visual states for answer buttons:
 * - Default: Normal button appearance
 * - Correct: Green button when answer is correct
 * - Incorrect: Red button when answer is wrong
 */
enum class ButtonState {
    Default, Correct, Incorrect
}

/**
 * Defines the different screens in the quiz application flow
 */
enum class QuizAppScreen {
    Start,  // Welcome screen
    Main,   // Quiz questions screen
    End     // Results screen
}

/**
 * Main composable that manages navigation between screens and maintains quiz state.
 * Uses Scaffold for consistent app structure with a top bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizApp() {
    // State variables to track current screen, score, and questions answered
    var currentScreen by remember { mutableStateOf(QuizAppScreen.Start) }
    var score by remember { mutableIntStateOf(0) }
    var questionsAnswered by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Quiz App") }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        // Display different screens based on navigation state
        when (currentScreen) {
            QuizAppScreen.Start -> StartScreen(
                onStart = {
                    // Reset score and counter when starting a new quiz
                    score = 0
                    questionsAnswered = 0
                    currentScreen = QuizAppScreen.Main
                },
                modifier = Modifier.padding(innerPadding)
            )

            QuizAppScreen.Main -> MainScreen(
                onFinish = { currentScreen = QuizAppScreen.End },
                score = score,
                onScore = { score++ },
                questionsAnswered = questionsAnswered,
                onQuestionAnswered = { questionsAnswered++ },
                modifier = Modifier.padding(innerPadding)
            )

            QuizAppScreen.End -> EndScreen(
                score = score,
                total = questionsAnswered,
                onRestart = { currentScreen = QuizAppScreen.Start },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * Welcome screen with a start button to begin the quiz.
 */
@Composable
fun StartScreen(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Hello and welcome to the Quiz App!", modifier = Modifier.padding(16.dp))
        Button(onClick = onStart) { Text("Start Quiz") }
    }
}

/**
 * Main quiz screen that displays questions and tracks user progress.
 * Handles user selection of answers and provides feedback.
 */
@Composable
fun MainScreen(
    onFinish: () -> Unit,
    score: Int,
    onScore: () -> Unit,
    questionsAnswered: Int,
    onQuestionAnswered: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sample quiz questions - in a real app, these would likely come from a database or API
    val questions = listOf(
        QuizQuestion("Capital of France?", listOf("Paris", "London", "Berlin"), 0),
        QuizQuestion("Capital of Serbia?", listOf("Madrid", "Bratislava", "Belgrade"), 2),
        QuizQuestion(
            "Who was the first programmer?",
            listOf("Mark Zuckerberg", "Tim Apple", "Ada Lovelace"),
            2
        ),
    )

    // State to track the current question and selected answer
    var currentQuestionIndex by remember { mutableIntStateOf(Random.nextInt(questions.size)) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    val currentQuestion = questions[currentQuestionIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display current score
        Text(
            text = "Score: $score / $questionsAnswered",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Display current question
        Text(currentQuestion.question, modifier = Modifier.padding(16.dp))

        // Display answer options
        currentQuestion.options.forEachIndexed { index, answer ->
            // Determine button state based on user selection
            val state = when {
                selectedAnswer == null -> ButtonState.Default
                index == currentQuestion.correctAnswerIndex && selectedAnswer != null -> ButtonState.Correct
                index == selectedAnswer && selectedAnswer != currentQuestion.correctAnswerIndex -> ButtonState.Incorrect
                else -> ButtonState.Default
            }
            QuizAnswerButton(
                text = answer,
                state = state,
                onClick = {
                    // Only allow selection if no answer is currently selected
                    if (selectedAnswer == null) {
                        selectedAnswer = index
                        onQuestionAnswered()
                        if (index == currentQuestion.correctAnswerIndex) {
                            onScore()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Next question button - only enabled after answering
        Button(
            onClick = {
                selectedAnswer = null
                currentQuestionIndex = Random.nextInt(questions.size)
            },
            enabled = selectedAnswer != null
        ) {
            Text("Next Question")
        }

        Spacer(Modifier.height(16.dp))

        // Allow finishing the quiz at any time
        Button(onClick = onFinish) { Text("Finish Quiz") }
    }
}

/**
 * End screen that displays the final score and allows restarting the quiz.
 */
@Composable
fun EndScreen(score: Int, total: Int, onRestart: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Quiz Finished!", modifier = Modifier.padding(16.dp))
        Text(
            text = "You've got $score answers out of $total right.",
            modifier = Modifier.padding(16.dp)
        )
        Button(onClick = onRestart) { Text("Restart Quiz") }
    }
}

/**
 * Preview composable for showing a quiz question box in the design preview.
 */
@Preview(showBackground = true)
@Composable
fun QuizQuestionBox(
    modifier: Modifier = Modifier
) {
    Text(
        text = "Quiz Question Box Placeholder",
        modifier = modifier
    )
}

/**
 * Custom button for quiz answers that changes color based on selection state:
 * - Default: Standard button color
 * - Correct: Green color for correct answers
 * - Incorrect: Red color for incorrect answers
 */
@Composable
fun QuizAnswerButton(
    text: String,
    state: ButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = when (state) {
        ButtonState.Default -> ButtonDefaults.buttonColors()
        ButtonState.Correct -> ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Green
        ButtonState.Incorrect -> ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)) // Red
    }
    Button(
        onClick = onClick,
        colors = colors,
        modifier = modifier
    ) {
        Text(text = text)
    }
}