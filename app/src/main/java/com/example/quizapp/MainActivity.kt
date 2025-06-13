package com.example.quizapp

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                QuizApp()
            }
        }
    }
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

enum class ButtonState {
    Default, Correct, Incorrect
}

enum class QuizAppScreen {
    Start,
    Main,
    End
}

// Simple implementation to change screens
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizApp() {
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
        when (currentScreen) {
            QuizAppScreen.Start -> StartScreen(
                onStart = {
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

// Starting Screen Composable
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

// Main Screen Composable
@Composable
fun MainScreen(
    onFinish: () -> Unit,
    score: Int,
    onScore: () -> Unit,
    questionsAnswered: Int,
    onQuestionAnswered: () -> Unit,
    modifier: Modifier = Modifier
) {

    val questions = listOf(
        QuizQuestion("Capital of France?", listOf("Paris", "London", "Berlin"), 0),
        QuizQuestion("Capital of Serbia?", listOf("Madrid", "Bratislava", "Belgrade"), 2),
        QuizQuestion(
            "Who was the first programmer?",
            listOf("Mark Zuckerberg", "Tim Apple", "Ada Lovelace"),
            2
        ),
    )
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
        Text(
            text = "Score: $score / $questionsAnswered",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)

        )
        Text(currentQuestion.question, modifier = Modifier.padding(16.dp))
        currentQuestion.options.forEachIndexed { index, answer ->
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
        Button(onClick = onFinish) { Text("Finish Quiz") }
    }
}

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