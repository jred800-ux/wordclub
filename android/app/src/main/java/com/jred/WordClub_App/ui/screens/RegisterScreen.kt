package com.jred.WordClub_App.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jred.WordClub_App.ui.theme.*
import com.jred.WordClub_App.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var registrationDone by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Watch for registration completion
    LaunchedEffect(uiState.isLoading, uiState.error) {
        if (!uiState.isLoading && uiState.error == null && (username.isNotEmpty() || email.isNotEmpty())) {
            registrationDone = true
        }
    }

    // Clear error when user types
    LaunchedEffect(username, email, password) {
        if (uiState.error != null) authViewModel.clearError()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brand
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = Blue600
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("W", color = White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("WordClub", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray900)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("创建你的学习账户", fontSize = 15.sp, color = Gray500)
                Spacer(modifier = Modifier.height(24.dp))

                // Error banner
                if (uiState.error != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = Red50
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.ErrorOutline, null, tint = Red500, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(uiState.error!!, fontSize = 13.sp, color = Red500, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Success banner
                if (registrationDone && uiState.error == null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = Green50
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Green500, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("注册成功，请登录", fontSize = 13.sp, color = Gray900, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; registrationDone = false },
                    label = { Text("用户名") },
                    leadingIcon = { Icon(Icons.Filled.Person, null, tint = Gray400) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue600,
                        focusedContainerColor = Gray50,
                        unfocusedContainerColor = Gray50
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; registrationDone = false },
                    label = { Text("邮箱") },
                    leadingIcon = { Icon(Icons.Filled.Email, null, tint = Gray400) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue600,
                        focusedContainerColor = Gray50,
                        unfocusedContainerColor = Gray50
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; registrationDone = false },
                    label = { Text("密码（至少6位）") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Gray400) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                null, tint = Gray400
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue600,
                        focusedContainerColor = Gray50,
                        unfocusedContainerColor = Gray50
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Register Button
                Button(
                    onClick = { authViewModel.register(username, email, password) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !uiState.isLoading && !registrationDone,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("注册", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }

                // If registration succeeded, show "go to login" button
                if (registrationDone && uiState.error == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("前往登录", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login link
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("已有账户？", fontSize = 14.sp, color = Gray500)
            TextButton(onClick = onNavigateToLogin) {
                Text("立即登录 →", color = Blue600, fontWeight = FontWeight.Medium)
            }
        }
    }
}
