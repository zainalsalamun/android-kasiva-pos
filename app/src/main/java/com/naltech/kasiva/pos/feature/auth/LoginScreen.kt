package com.naltech.kasiva.pos.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naltech.kasiva.pos.data.local.LocalAuthStore
import kotlinx.coroutines.launch

private val DeepBlue = Color(0xFF02569B)
private val LightBlue = Color(0xFF0EA5E9)
private val SoftBlue = Color(0xFFEAF4FF)
private val InputBg = Color(0xFFF1F3F5)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val authStore = remember(context) { LocalAuthStore(context.applicationContext) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FBFF), SoftBlue, Color.White)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFFD9ECFF).copy(alpha = 0.72f),
                radius = size.minDimension * 0.34f,
                center = androidx.compose.ui.geometry.Offset(-size.width * 0.04f, size.height * 0.18f)
            )
            drawCircle(
                color = Color(0xFFB9DCFF).copy(alpha = 0.50f),
                radius = size.minDimension * 0.42f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 1.05f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.55f),
                radius = size.minDimension * 0.26f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.98f, size.height * 0.15f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            val isCompact = maxWidth < 600.dp
            val cardWidth = if (isCompact) Modifier.fillMaxWidth() else Modifier.widthIn(max = 460.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isCompact) 24.dp else 40.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(if (isCompact) 18.dp else 42.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(if (isCompact) 70.dp else 80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF0B6BEF), DeepBlue))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(if (isCompact) 34.dp else 40.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "K",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.offset(y = (-6).dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Row {
                Text(
                    text = "Kasiva",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "POS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepBlue
                )
            }

            Text(
                text = "Smart Offline Point of Sale",
                fontSize = 14.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Login Card
            Surface(
                modifier = Modifier
                    .then(cardWidth)
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp), spotColor = DeepBlue.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Masuk ke Akun Anda",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Silakan masuk untuk melanjutkan",
                        fontSize = 14.sp,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Email Field
                    Text("Email atau Username", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("kasir@tokosaya.com", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextGray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = InputBg,
                            focusedContainerColor = InputBg,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = DeepBlue
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Text("Password", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••••••", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextGray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = InputBg,
                            focusedContainerColor = InputBg,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = DeepBlue
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    errorMessage?.let {
                        Surface(
                            color = Color(0xFFFFE8E8),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = it,
                                color = Color(0xFFB91C1C),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = DeepBlue)
                            )
                            Text("Ingat saya", fontSize = 13.sp, color = TextDark)
                        }
                        TextButton(onClick = { }) {
                            Text("Lupa password?", fontSize = 13.sp, color = DeepBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Button
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val isSuccess = authStore.login(email, password, rememberMe)
                                isLoading = false
                                if (isSuccess) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Email/username atau password tidak sesuai"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Divider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
                        Text(
                            "atau masuk dengan",
                            modifier = Modifier.padding(horizontal = 12.dp),
                            fontSize = 12.sp,
                            color = TextGray
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Demo Account Button
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                authStore.loginDemo()
                                isLoading = false
                                onLoginSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        border = border(width = 1.dp, color = DeepBlue.copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBlue)
                    ) {
                        Icon(Icons.Default.PersonOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Akun Demo", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "Versi 1.0.0",
                fontSize = 12.sp,
                color = TextGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun border(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = {})
}
