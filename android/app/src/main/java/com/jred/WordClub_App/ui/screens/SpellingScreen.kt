package com.jred.WordClub_App.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jred.WordClub_App.ui.theme.*

@Composable
fun SpellingScreen(navController: NavController) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var userInput by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val word = if (currentIndex < sampleWords.size) sampleWords[currentIndex] else null
    val progress = currentIndex + 1
    val total = sampleWords.size

    LaunchedEffect(currentIndex) {
        userInput = ""
        submitted = false
        showHint = false
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    fun checkAnswer() {
        if (word == null) return
        isCorrect = userInput.lowercase().trim() == word.spelling.lowercase()
        submitted = true
    }

    fun nextWord() {
        currentIndex = (currentIndex + 1) % total
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (word != null) {
            // Progress
            Text("学习进度 $progress / $total", style = MaterialTheme.typography.bodySmall, color = Gray500, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress.toFloat() / total }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Blue600, trackColor = Gray200)
            Spacer(modifier = Modifier.height(24.dp))

            // Spelling Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Part of Speech
                    Surface(shape = MaterialTheme.shapes.extraLarge, color = Blue50) {
                        Text(word.partOfSpeech, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Blue600, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Definition
                    Text(word.meaning, fontSize = 18.sp, color = Gray900, textAlign = TextAlign.Center, lineHeight = 28.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Phonetic
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(word.phonetic, fontSize = 14.sp, color = Gray400)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { }, modifier = Modifier.size(28.dp), colors = IconButtonDefaults.iconButtonColors(containerColor = Blue50, contentColor = Blue600)) {
                            Icon(Icons.Filled.VolumeUp, null, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Letter Input Area
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (submitted) {
                            // Show result
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                word.spelling.forEachIndexed { idx, ch ->
                                    val color = when {
                                        !isCorrect -> Red500
                                        else -> Green500
                                    }
                                    Text(
                                        ch.toString(),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        } else {
                            BasicTextField(
                                value = userInput,
                                onValueChange = { if (it.length <= word.spelling.length) userInput = it },
                                modifier = Modifier.focusRequester(focusRequester),
                                textStyle = TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gray900,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 8.sp
                                ),
                                cursorBrush = SolidColor(Blue600),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.Center) {
                                        if (userInput.isEmpty()) {
                                            Text(
                                                "_ ".repeat(word.spelling.length).trim(),
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Gray200,
                                                textAlign = TextAlign.Center,
                                                letterSpacing = 8.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Result
                    AnimatedVisibility(visible = submitted) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = if (isCorrect) Green50 else Red50
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel, null,
                                    tint = if (isCorrect) Green500 else Red500, modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isCorrect) "正确！" else "正确答案：${word.spelling}",
                                    fontWeight = FontWeight.Medium, fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hint button
                    if (!submitted) {
                        TextButton(onClick = { showHint = true }) {
                            Icon(Icons.Filled.Lightbulb, null, tint = Amber500, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("提示", color = Amber500, fontSize = 14.sp)
                        }
                        if (showHint) {
                            Text(
                                "首字母: ${word.spelling.first()}",
                                fontSize = 13.sp, color = Gray500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Submit / Next
                    if (!submitted) {
                        Button(
                            onClick = { checkAnswer() },
                            enabled = userInput.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("确认", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ENTER 提交", fontSize = 11.sp, color = White.copy(alpha = 0.7f))
                        }
                    } else {
                        Button(
                            onClick = { nextWord() },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("下一个", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { nextWord() }, enabled = !submitted) {
                        Icon(Icons.Filled.SkipNext, null, tint = Gray400, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("跳过此词", color = Gray400, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
