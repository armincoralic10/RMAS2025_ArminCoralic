package com.example.careevac.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.careevac.model.Resident
import com.example.careevac.viewmodel.ResidentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentListScreen(
    navController: NavController,
    viewModel: ResidentViewModel
) {
    val residents by viewModel.residents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var residentToDelete by remember { mutableStateOf<Resident?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administracija Stanara", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_resident") },
                containerColor = Color(0xFF2E7D32)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj stanara", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading && residents.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (residents.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Nema stanara u bazi",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Kliknite + dugme da dodate novog stanara",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(modifier = Modifier.padding(padding)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF1976D2)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Ukupno stanara: ${residents.size}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                                Text(
                                    "Kliknite Edit za uređivanje",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(residents.sortedBy { it.fullName }) { resident ->
                            ResidentCard(
                                resident = resident,
                                onEditClick = {
                                    navController.navigate("edit_resident/${resident.id}")
                                },
                                onDeleteClick = {
                                    residentToDelete = resident
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteDialog && residentToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Brisanje stanara") },
                text = {
                    Text("Da li ste sigurni da želite obrisati stanara ${residentToDelete?.fullName}?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                residentToDelete?.let {
                                    viewModel.deleteResident(it.id)
                                }
                                showDeleteDialog = false
                                residentToDelete = null
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
    }
}

@Composable
fun ResidentCard(
    resident: Resident,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resident.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Soba ${resident.roomNumber}",
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = resident.mobilityStatus,
                    color = when (resident.mobilityStatus.uppercase()) {
                        "NEPOKRETAN" -> Color(0xFFB71C1C)
                        "OTEZANO POKRETAN", "OTEŽANO POKRETAN" -> Color(0xFFFF6F00)
                        else -> Color(0xFF2E7D32)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Uredi",
                        tint = Color(0xFF1976D2)
                    )
                }
                IconButton(onClick = onDeleteClick) {
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