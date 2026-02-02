@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.aeye
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.heraapp.ui.ChatMessage
import com.example.heraapp.ui.ChatMessage.BotMessage
import com.example.heraapp.ui.ChatMessage.UserMessage
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

// Main Activity that launches the Hera chatbot screen
class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeraChatScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeraChatScreen() {
// Initialize context + user ID (from firebase) + chatbot messages + coroutines + Chatbot readiness
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val scope = rememberCoroutineScope()
    var isChatbotReady by remember { mutableStateOf(false) }

    // Initialize chatbot instance
    val chatbot = remember {
        HybridChatbot(
            heraBot = HeraChatbot(),
            generalBot = CohereBot("NB40tA2s3koDkEFfDu4bjR3nlAiwETJCNHWugMNr")
        )
    }

    // Load user cycle data and prepare chatbot
    LaunchedEffect(Unit) {
        if (userId != null) {
            println("🔍 Fetching all chatbot data for userId = $userId")
            Firestore().getCycleLogs(
                userId = userId,
                onSuccess = { logs ->
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.UK)

                    // Filters and sorts logs chronologically
                    val sortedLogs = logs
                        .filter { it.startDate.matches(Regex("""\d{2}/\d{2}/\d{4}""")) }
                        .sortedBy { sdf.parse(it.startDate) }

                    val latestLog = sortedLogs.lastOrNull()
                    if (latestLog != null) {
                        // Extracts symptom data from the latest log
                        val symptoms = Symptoms(
                            mood = latestLog.mood,
                            energy = latestLog.energyLevel,
                            painLevel = latestLog.painLevel,
                            hydration = latestLog.hydration.toInt(),
                        )
                        // Prepares cycle metadata for the chatbot
                        val cycleData = CycleData(
                            userId = userId,
                            cycleStartDate = latestLog.startDate,
                            cycleEndDate = latestLog.endDate,
                            symptoms = symptoms
                        )
                        // Updates chatbot memory with user data
                        chatbot.updateUserData(symptoms, cycleData)

                        // Give Hera all sorted logs for prediction
                        (chatbot as? HybridChatbot)?.heraBot?.let {
                            if (it is HeraChatbot) it.updateCycleHistory(sortedLogs)
                        }

                        isChatbotReady = true
                    }
                },
                onFailure = { e ->
                    println("❌ Failed to load chatbot data: ${e.message}")
                    Toast.makeText(context, "Chatbot failed to load data", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            println("❌ No userId found. User might not be logged in.")
        }
    }

    // Greet user with intro message
    LaunchedEffect(Unit) {
        messages.add(BotMessage("Hello! Im Hera 🌸, How can I help you today? \nYou can ask me about your diet, pain relief, or menstrual cycle."))
    }

    // Layout for chatbot UI
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Top App Bar with Hera branding
        Column(modifier = Modifier
            .fillMaxSize()
            .imePadding()) {
            CenterAlignedTopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.hera),
                            contentDescription = "Hera Text Logo",
                            modifier = Modifier.align(Alignment.Center).height(100.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.setting),
                            contentDescription = "Hera Logo",
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp)
                                .size(45.dp)
                        )
                    }
                },
                colors = centerAlignedTopAppBarColors(containerColor = Color(0xFFF89AAC
                )
                )
            )

            if (!isChatbotReady) {
                // Show loading spinner
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xDDD89AAC))
                }
            } else {
                // Then show main chat screen
                ChatScreen(messages) { userMessage ->
                    messages.add(UserMessage(userMessage))
                    messages.add(BotMessage("..."))

                    scope.launch {
                        messages.removeAt(messages.lastIndex)
                        val botReply = chatbot.sendMessage(userMessage)
                        messages.add(BotMessage(botReply))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(messages: List<ChatMessage>, onSend: (String) -> Unit) {
    // Keeps track of messages already animated
    val animatedMessageIds = remember { mutableStateListOf<String>() }
    // Initializes vertical scrolling + coroutines
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    // Holds user's current input in the text field
    var input by remember { mutableStateOf("") }
    // Detects if the keyboard is currently open
    val isKeyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    // Implemented Auto-scroll when new Chatbot message appears
    LaunchedEffect(messages.size, isKeyboardOpen) {
        coroutineScope.launch {
            scrollState.animateScrollToItem(messages.size)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Added LazyColumn to display all chatbot and user messages in a scrollable list
        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            state = scrollState,
            reverseLayout = false
        ) {
            items(messages) { message ->
                when (message) {
                    is UserMessage -> UserMessageBubble(message.message)
                    is BotMessage -> if (message.message == "...") BotTypingIndicator() else BotMessageBubbleAnimated(message.id, message.message, scrollState, animatedMessageIds)
                    else -> {}
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Added section for Input field and send button
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF626567)).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).height(62.dp)) {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Type your message...") },
                    shape = RoundedCornerShape(25.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFDADADA),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth().padding(5.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = {
                if (input.isNotBlank()) {
                    onSend(input)
                    input = ""
                }
            }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// Added UI bubble for user message
@Composable
fun UserMessageBubble(message: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Text(
            text = message,
            modifier = Modifier.background(Color(0xFF888888), shape = RoundedCornerShape(20.dp)).padding(10.dp),
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

// Added bubble for bot message with animated typewriter effect
@Composable
fun BotMessageBubbleAnimated(
    messageId: String,
    fullMessage: String,
    scrollState: LazyListState,
    animatedMessageIds: MutableList<String>
) {
    var visibleText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(messageId) {
        if (!animatedMessageIds.contains(messageId)) {
            visibleText = ""
            fullMessage.forEachIndexed { index, _ ->
                visibleText = fullMessage.substring(0, index + 1)
                delay(50L)
                scope.launch {
                    scrollState.animateScrollToItem(scrollState.layoutInfo.totalItemsCount)
                }
            }
            animatedMessageIds.add(messageId)
        } else {
            visibleText = fullMessage
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Text(
            text = visibleText,
            modifier = Modifier.background(Color(0xFFF89AAC), shape = RoundedCornerShape(24.dp)).padding(10.dp),
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

//Added Typing animation for bot feedback
@SuppressLint("SuspiciousIndentation")
@Composable
fun BotTypingIndicator() {
    val dotCount = rememberInfiniteTransition().animateValue(
        initialValue = 0,
        targetValue = 3,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val dots = "Hera is typing" + ".".repeat(dotCount.value + 1)

    Row(horizontalArrangement = Arrangement.Start) {
        Text(
            text = dots,
            modifier = Modifier.background(Color(0xFFF89AAC), shape = RoundedCornerShape(24.dp)).padding(12.dp),
            color = Color.White,
            fontSize = 16.sp
        )
    }
}
