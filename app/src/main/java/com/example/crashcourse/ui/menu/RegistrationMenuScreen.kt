package com.example.crashcourse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 📋 RegistrationMenuScreen - Menu for choosing registration type
 * 
 * Features:
 * - ✅ Two registration options: Individual and Bulk
 * - ✅ Modern card-based UI design
 * - ✅ Clear visual distinction between options
 * - ✅ Easy navigation to respective screens
 */
@Composable
fun RegistrationMenuScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToBulkRegister: () -> Unit,
    onNavigateToAddUser: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Choose Registration Type",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Individual Registration Card
        Card(
            onClick = onNavigateToRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Individual Registration",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Individual Registration",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF008080) // Teal color
                    )
                    Text(
                        text = "Register one person at a time with camera",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF008080).copy(alpha = 0.8f) // Teal color with transparency
                    )
                }
            }
        }


        // Add User Card
        Card(
            onClick = onNavigateToAddUser,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Add User",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Add New User",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF008080) // Teal color
                    )
                    Text(
                        text = "Add a new user with face embedding and details",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF008080).copy(alpha = 0.8f) // Teal color with transparency
                    )
                }
            }
        }

        // Excel Image Process Card
        Card(
            onClick = onNavigateToBulkRegister, // Reusing this navigation callback for Excel Image Process
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Excel Image Process",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Excel Image Process",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF008080) // Teal color
                    )
                    Text(
                        text = "Process images and data from Excel file",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF008080).copy(alpha = 0.8f) // Teal color with transparency
                    )
                }
            }
        }

        // Bulk Registration Card
        Card(
            onClick = onNavigateToBulkRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Bulk Registration",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Bulk Registration",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF008080) // Teal color
                    )
                    Text(
                        text = "Register multiple users by uploading a photo or file",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF008080).copy(alpha = 0.8f) // Teal color with transparency
                    )
                }
            }
        }

        // Helper text
        Text(
            text = "Choose the registration method that best fits your needs",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}
