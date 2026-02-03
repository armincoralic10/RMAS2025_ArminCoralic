package com.example.careevac.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.careevac.model.User
import com.example.careevac.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val allUsers by authViewModel.allUsers.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    var showRoleDialog by remember { mutableStateOf(false) }
    var userToChangeRole by remember { mutableStateOf<User?>(null) }

    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.loadAllUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upravljanje korisnicima") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Super Admin Panel",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            "Ukupno korisnika: ${allUsers.size}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (allUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allUsers.sortedBy { it.fullName }) { user ->
                        UserCard(
                            user = user,
                            isCurrentUser = user.uid == currentUser?.uid,
                            isUpdating = isUpdating,
                            onToggleStatus = {
                                isUpdating = true
                                scope.launch {
                                    authViewModel.toggleUserStatus(user.uid, !user.isActive)
                                    kotlinx.coroutines.delay(500)
                                    isUpdating = false
                                }
                            },
                            onChangeRole = {
                                userToChangeRole = user
                                showRoleDialog = true
                            },
                            onDelete = {
                                if (user.uid != currentUser?.uid) {
                                    userToDelete = user
                                    showDeleteDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }

        // Delete Dialog
        if (showDeleteDialog && userToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Brisanje korisnika") },
                text = {
                    Text("Da li ste sigurni da želite obrisati korisnika ${userToDelete?.fullName}?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                userToDelete?.let {
                                    authViewModel.deleteUser(it.uid)
                                }
                                showDeleteDialog = false
                                userToDelete = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Text("Obriši")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Otkaži")
                    }
                }
            )
        }

        // Change Role Dialog
        if (showRoleDialog && userToChangeRole != null) {
            AlertDialog(
                onDismissRequest = { showRoleDialog = false },
                title = { Text("Promjena role") },
                text = {
                    Column {
                        Text("Odaberite novu rolu za ${userToChangeRole?.fullName}:")
                        Spacer(modifier = Modifier.height(16.dp))

                        val roles = listOf("staff", "admin", "super_admin")
                        roles.forEach { role ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = userToChangeRole?.role == role,
                                    onClick = {
                                        scope.launch {
                                            userToChangeRole?.let {
                                                authViewModel.changeUserRole(it.uid, role)
                                            }
                                            showRoleDialog = false
                                            userToChangeRole = null
                                        }
                                    }
                                )
                                Text(
                                    text = when (role) {
                                        "super_admin" -> "👑 Super Admin"
                                        "admin" -> "🔧 Admin"
                                        else -> "👤 Staff"
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showRoleDialog = false }) {
                        Text("Otkaži")
                    }
                }
            )
        }
    }
}

@Composable
fun UserCard(
    user: User,
    isCurrentUser: Boolean,
    isUpdating: Boolean,
    onToggleStatus: () -> Unit,
    onChangeRole: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (!user.isActive) Color(0xFFFFEBEE) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF4CAF50)
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "TI",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(user.email, color = Color.Gray, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (user.role) {
                                    "super_admin" -> Color(0xFFFFD700)
                                    "admin" -> Color(0xFF2196F3)
                                    else -> Color(0xFF9E9E9E)
                                }
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when (user.role) {
                                    "super_admin" -> "👑 SUPER ADMIN"
                                    "admin" -> "🔧 ADMIN"
                                    else -> "👤 STAFF"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (user.isActive)
                                    Color(0xFF4CAF50)
                                else
                                    Color(0xFFD32F2F)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (user.isActive) "AKTIVAN" else "NEAKTIVAN",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (user.isEmailVerified) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF00BCD4)
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "✓ EMAIL",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (!isCurrentUser) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onToggleStatus,
                        enabled = !isUpdating,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                if (user.isActive) Icons.Default.Clear else Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (user.isActive) "Deaktiviraj" else "Aktiviraj",
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onChangeRole,
                        enabled = !isUpdating,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Promijeni rolu", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDelete,
                        enabled = !isUpdating
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Obriši",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }
}