package com.jred.WordClub_App.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jred.WordClub_App.ui.theme.*

data class Word(
    val id: Int, val spelling: String, val meaning: String,
    val phonetic: String = "", val partOfSpeech: String = "noun",
    val prefix: String = "", val root: String = "", val suffix: String = ""
)

val sampleWords = listOf(
    Word(1, "persistent", "坚持不懈的；执着的", "/pərˈsɪstənt/", "adjective", "per-", "sist", "-ent"),
    Word(2, "elaborate", "精心制作的；详细阐述", "/ɪˈlæbərət/", "adjective, verb", "e-", "labor", "-ate"),
    Word(3, "substantial", "大量的；实质的", "/səbˈstænʃəl/", "adjective", "sub-", "stant", "-ial"),
)

@Composable
fun FirstSightScreen(navController: NavController) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var statusMsg by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var cardOrder by remember { mutableStateOf("random") }
    var largeFont by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }

    val word = if (currentIndex < sampleWords.size) sampleWords[currentIndex] else null
    val progress = currentIndex + 1
    val total = sampleWords.size

    LaunchedEffect(statusMsg) {
        if (statusMsg.isNotEmpty()) {
            kotlinx.coroutines.delay(2000)
            statusMsg = ""
        }
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
            Text(
                "学习进度 $progress / $total",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Blue600,
                trackColor = Gray200,
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Word Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(40.dp, 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        word.spelling,
                        fontSize = if (largeFont) 42.sp else 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(word.phonetic, fontSize = 14.sp, color = Gray500)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Blue50, contentColor = Blue600
                            )
                        ) {
                            Icon(Icons.Filled.VolumeUp, null, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = Blue50
                    ) {
                        Text(
                            word.partOfSpeech,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Blue600,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { navController.navigate("word/${word.id}") }) {
                        Icon(Icons.Filled.TouchApp, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("点击查看详情", color = Blue600, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Triple Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                ActionButton("不认识", Icons.Filled.Close, Red50, Red500) {
                    statusMsg = "已标记为不认识"
                    currentIndex = (currentIndex + 1) % total
                }
                ActionButton("模糊", Icons.Filled.VisibilityOff, Amber50, Amber500) {
                    statusMsg = "已标记为模糊"
                    currentIndex = (currentIndex + 1) % total
                }
                ActionButton("认识", Icons.Filled.Check, Green50, Green500) {
                    statusMsg = "已存入掌握列表"
                    currentIndex = (currentIndex + 1) % total
                }
            }

            // Status Banner
            AnimatedVisibility(
                visible = statusMsg.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Surface(
                    modifier = Modifier.padding(top = 20.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Green50
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Green500, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(statusMsg, fontWeight = FontWeight.Medium, color = Gray900, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = MaterialTheme.shapes.large
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showSettings = !showSettings }.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Tune, null, tint = Gray500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("学习设置", fontWeight = FontWeight.SemiBold)
                        }
                        Icon(
                            if (showSettings) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            null, tint = Gray400
                        )
                    }

                    if (showSettings) {
                        Divider(color = Gray100)
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Focus Mode
                            Text("学习重点", style = MaterialTheme.typography.labelSmall, color = Gray400)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = Blue50,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Filled.Visibility, null, tint = Blue600, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("认读模式", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text("专注于识别单词含义和发音。", fontSize = 12.sp, color = Gray500)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Card Order
                            Text("卡片顺序", style = MaterialTheme.typography.labelSmall, color = Gray400)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                listOf("字母序" to "alphabetical", "随机序" to "random").forEach { (label, value) ->
                                    FilterChip(
                                        selected = cardOrder == value,
                                        onClick = { cardOrder = value },
                                        label = { Text(label) },
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Display Options
                            Text("显示选项", style = MaterialTheme.typography.labelSmall, color = Gray400)
                            Spacer(modifier = Modifier.height(8.dp))
                            ToggleRow("FormatSize", "大字体", largeFont) { largeFont = it }
                            ToggleRow("DarkMode", "深色模式", darkMode) { darkMode = it }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ActionButton(
    label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: androidx.compose.ui.graphics.Color, iconColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f).height(80.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = White)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Gray500)
        }
    }
}

@Composable
private fun ToggleRow(iconName: String, label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.FormatSize, null, tint = Gray400, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = Blue600))
    }
}
