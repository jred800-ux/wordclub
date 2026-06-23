package com.jred.WordClub_App.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jred.WordClub_App.ui.theme.*

@Composable
fun StudySummaryScreen() {
    var shared by remember { mutableStateOf(false) }

    // Generate sample heatmap data
    val heatmapData = remember {
        List(7) { List(15) { (0..4).random() } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Celebration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Amber50),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Celebration, null, tint = Amber500, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("恭喜！", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Gray900)
                Spacer(modifier = Modifier.height(4.dp))
                Text("今日目标已达成。", style = MaterialTheme.typography.bodyMedium, color = Gray500)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard2("50", "今日学习单词", Blue600, Icons.Filled.MenuBook, modifier = Modifier.weight(1f))
            StatCard2("45 分钟", "今日学习时长", Amber500, Icons.Filled.Timer, modifier = Modifier.weight(1f))
            StatCard2("15", "连续打卡天数", Red500, Icons.Filled.LocalFireDepartment, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Heatmap Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("学习热力图", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // Heatmap grid
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    heatmapData.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            row.forEach { level ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(
                                            when (level) {
                                                0 -> Color(0xFFEBEDF0)
                                                1 -> Color(0xFFC6E48B)
                                                2 -> Color(0xFF7BC96F)
                                                3 -> Color(0xFF239A3B)
                                                else -> Color(0xFF196127)
                                            },
                                            shape = MaterialTheme.shapes.extraSmall
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("少", fontSize = 11.sp, color = Gray400)
                    Spacer(modifier = Modifier.width(4.dp))
                    listOf(Color(0xFFEBEDF0), Color(0xFFC6E48B), Color(0xFF7BC96F), Color(0xFF239A3B), Color(0xFF196127)).forEach { c ->
                        Box(modifier = Modifier.size(12.dp).background(c, MaterialTheme.shapes.extraSmall))
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("多", fontSize = 11.sp, color = Gray400)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Share Button
        Button(
            onClick = { shared = true },
            colors = ButtonDefaults.buttonColors(containerColor = if (shared) Green500 else Amber500),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Filled.Share, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (shared) "已分享！" else "打卡并分享", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quote
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "\"语言的边界，即是世界的边界。\"",
                fontSize = 14.sp,
                color = Gray500,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("— Ludwig Wittgenstein", fontSize = 12.sp, color = Gray400)
        }
    }
}

@Composable
private fun StatCard2(
    value: String, label: String, color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = White),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Gray900)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = Gray500, textAlign = TextAlign.Center)
        }
    }
}
