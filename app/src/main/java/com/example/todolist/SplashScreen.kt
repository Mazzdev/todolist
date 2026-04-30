package com.example.todolist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen() {
    var showLogin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500)
        showLogin = true
    }

    if (showLogin) {
        LoginScreen()
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ToDoList",
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val auth = FirebaseAuth.getInstance()

    var isLoggedIn by remember {
        mutableStateOf(auth.currentUser != null)
    }

    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    if (isLoggedIn) {
        AppNavigation()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRegisterMode) "Register" else "Login",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        message = "Email and password cannot be empty"
                        return@Button
                    }

                    if (isRegisterMode) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                message = "Account created"
                                isLoggedIn = true
                            }
                            .addOnFailureListener {
                                message = it.message ?: "Register error"
                            }
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                message = "Login successful"
                                isLoggedIn = true
                            }
                            .addOnFailureListener {
                                message = it.message ?: "Login error"
                            }
                    }
                }
            ) {
                Text(if (isRegisterMode) "Register" else "Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    isRegisterMode = !isRegisterMode
                    message = ""
                }
            ) {
                Text(
                    if (isRegisterMode) {
                        "Already have an account? Login"
                    } else {
                        "Create new account"
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (message.isNotBlank()) {
                Text(message)
            }
        }
    }
}