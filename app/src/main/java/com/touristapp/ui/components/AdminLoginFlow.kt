package com.touristapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.touristapp.data.repository.TouristRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Reusable login + apartment picker flow.
 * Used by both SetupScreen (full-screen) and AdminDialog (overlay).
 *
 * @param onApartmentSelected called with the chosen apartment ID
 * @param onDismiss if non-null, shows a close button (used in dialog mode)
 * @param onLockout called when 3 failed attempts are reached
 */
@Composable
fun AdminLoginFlow(
    onApartmentSelected: (String) -> Unit,
    onDismiss: (() -> Unit)?,
    onLockout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val repository = remember { TouristRepository() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var failedAttempts by remember { mutableIntStateOf(0) }

    // After login: list of (apartmentId, displayName)
    var apartments by remember { mutableStateOf<List<Pair<String, String>>?>(null) }

    Column(modifier = modifier) {
        // Close button (only in dialog mode)
        if (onDismiss != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        if (apartments == null) {
            // --- Step 1: Login ---
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null

                    scope.launch {
                        try {
                            auth.signInWithEmailAndPassword(email, password).await()
                            val uid = auth.currentUser?.uid ?: throw Exception("No user")

                            // Fetch admin's apartments
                            val ids = repository.getAdminApartmentIds(uid)
                            val names = repository.getApartmentNames(ids)
                            apartments = names
                        } catch (e: Exception) {
                            failedAttempts++
                            if (failedAttempts >= 3) {
                                // Lockout — silently dismiss
                                auth.signOut()
                                onLockout?.invoke()
                                onDismiss?.invoke()
                            } else {
                                errorMessage = "Invalid credentials"
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sign in")
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            // --- Step 2: Apartment Picker ---
            Text(
                text = "Select apartment",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(apartments!!) { (id, displayName) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // Save selection, sign out, transition
                                auth.signOut()
                                onApartmentSelected(id)
                            }
                    ) {
                        Text(
                            text = displayName,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
