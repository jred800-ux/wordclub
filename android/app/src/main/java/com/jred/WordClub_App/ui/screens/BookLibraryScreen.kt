package com.jred.WordClub_App.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jred.WordClub_App.ui.theme.*

data class Course(
    val id: String, val name: String, val vocab: Int,
    val gradient: List<Color>, val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val courses = listOf(
    Course("cet4", "CET-4", 4500, listOf(Color(0xFF667EEA), Color(0xFF764BA2)), Icons.Filled.School),
    Course("cet6", "CET-6", 6000, listOf(Color(0xFF11998E), Color(0xFF38EF7D)), Icons.Filled.MenuBook),
    Course("postgrad", "考研英语", 5500, listOf(Color(0xFFF093FB), Color(0xFFF5576C)), Icons.Filled.AutoStories),
    Course("ielts", "雅思 IELTS", 8000, listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)), Icons.Filled.FlightTakeoff),
)

@Composable
fun BookLibraryScreen(navController: NavController) {
    var selectedId by remember { mutableStateOf("cet4") }
    var dailyGoal by remember { mutableIntStateOf(20) }

    val selected = courses.find { it.id == selectedId } ?: courses.first()
    val estimatedDays = selected.vocab / dailyGoal
    val intensity = when { estimatedDays <= 30 -> "高强度"; estimatedDays <= 60 -> "适中"; else -> "轻松" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section Header
        Text("词库选择", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("选择一套课程，开启你的语言沉浸式学习之旅。", style = MaterialTheme.typography.bodyMedium, color = Gray500)
        Spacer(modifier = Modifier.height(20.dp))

        // Course Grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            courses.forEach { course ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selectedId = course.id },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (selectedId == course.id) 4.dp else 1.dp)
                ) {
                    Column {
                        // Gradient header
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Transparent,
                                tonalElevation = 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .then(
                                            Modifier.background(Brush.linearGradient(course.gradient))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(course.icon, null, tint = White.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                                }
                            }
                            Surface(
                                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = Color.Black.copy(alpha = 0.45f)
                            ) {
                                Text(
                                    if (selectedId == course.id) "学习中" else "未开始",
                                    fontSize = 12.sp, color = White,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                )
                            }
                            if (selectedId == course.id) {
                                Surface(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = Green500
                                ) {
                                    Icon(Icons.Filled.Check, null, tint = White, modifier = Modifier.size(18.dp).padding(2.dp))
                                }
                            }
                        }
                        // Info
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.MenuBook, null, tint = Gray400, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(course.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("${course.vocab} 词", fontSize = 12.sp, color = Gray500)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Plan Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("个性化你的学习计划", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // Daily Goal
                Text("每日目标词数", style = MaterialTheme.typography.labelSmall, color = Gray400)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 20, 50, 100).forEach { n ->
                        FilterChip(
                            selected = dailyGoal == n,
                            onClick = { dailyGoal = n },
                            label = { Text("$n 词/天") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Estimate
                Surface(shape = MaterialTheme.shapes.medium, color = Blue50, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("预计完成天数", fontSize = 14.sp, color = Gray500)
                        Text("$estimatedDays 天", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Blue600)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Gray100)
                Spacer(modifier = Modifier.height(16.dp))

                // Overview
                Text("计划概览", style = MaterialTheme.typography.labelSmall, color = Gray400)
                Spacer(modifier = Modifier.height(8.dp))
                PlanRow(Icons.Filled.MenuBook, "已选书籍", selected.name)
                PlanRow(Icons.Filled.FormatListNumbered, "总词数", "${selected.vocab}")
                PlanRow(Icons.Filled.Speed, "强度", intensity)

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { navController.navigate("learn/first-sight") },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("开始学习", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PlanRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Gray400, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = Gray500, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
    }
}

