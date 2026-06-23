package com.jred.WordClub_App.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jred.WordClub_App.ui.theme.*

@Composable
fun WordDetailScreen(wordId: String, navController: NavController) {
    val id = wordId.toIntOrNull() ?: 1
    val word = sampleWords.find { it.id == id } ?: sampleWords.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Hero
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(word.spelling, fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(word.phonetic, fontSize = 15.sp, color = Gray500)
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { }, modifier = Modifier.size(28.dp), colors = IconButtonDefaults.iconButtonColors(containerColor = Blue50, contentColor = Blue600)) {
                    Icon(Icons.Filled.VolumeUp, null, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(shape = MaterialTheme.shapes.extraLarge, color = Blue50) {
                Text(word.partOfSpeech, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Blue600, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
            }
        }

        // Morpheme Analysis
        SectionCard("词素分析", Icons.Filled.Schema) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MorphemeItem("per-", "前缀", "Prefix", Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                MorphemeItem(word.root.ifEmpty { "sist" }, "词根", "Root", Color(0xFF10B981), modifier = Modifier.weight(1f))
                MorphemeItem("-ent", "后缀", "Suffix", Color(0xFF3B82F6), modifier = Modifier.weight(1f))
            }
        }

        // Definitions
        SectionCard("释义", Icons.Filled.Translate) {
            Text("English", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Continuing firmly or obstinately in a course of action in spite of difficulty or opposition.", fontSize = 15.sp, color = Gray900, lineHeight = 24.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("中文", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(word.meaning, fontSize = 15.sp, color = Gray900, lineHeight = 24.sp)
        }

        // Example Sentences
        SectionCard("例句", Icons.Filled.FormatQuote) {
            val sentences = listOf(
                "She was ${word.spelling} in her efforts to learn English." to "她在学习英语方面坚持不懈。",
                "The ${word.spelling} symptoms required further examination." to "持续的症状需要进一步检查。"
            )
            sentences.forEach { (en, zh) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.PlayCircle, null, tint = Blue600, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(en, fontSize = 15.sp, color = Gray900, lineHeight = 22.sp)
                        Text(zh, fontSize = 13.sp, color = Gray500, lineHeight = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Next button
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("返回学习", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Blue600, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun MorphemeItem(text: String, type: String, typeEn: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Gray50
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = MaterialTheme.shapes.medium, color = color) {
                Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(type, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
            Text(typeEn, fontSize = 11.sp, color = Gray400)
        }
    }
}
