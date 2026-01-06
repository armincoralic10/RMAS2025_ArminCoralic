package com.example.careevac.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    navController: NavController,
    viewModel: ResidentViewModel
) {
    var selectedTabIndex by remember { mutableStateOf(1) }
    val tabs = listOf("PRIORITETI", "SVI", "EVAK.")

    val residents by viewModel.residents.collectAsState()
    val evacuatedCount by viewModel.evacuatedCount.collectAsState()
    val waitingCount by viewModel.waitingCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HITNA EVAKUACIJA", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.resetAllEvacuations() }) {
                        Text("RESET", color = Color.Red)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F8F8))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Čeka: $waitingCount",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    fontSize = 18.sp
                )
                Text(
                    text = "Evakuisano: $evacuatedCount",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val filteredList = when (selectedTabIndex) {
                    0 -> residents.filter {
                        it.mobilityStatus.uppercase().contains("NEPOKRETAN") && !it.isEvacuated
                    }
                    2 -> residents.filter { it.isEvacuated }
                    else -> residents.filter { !it.isEvacuated }
                }

                if (filteredList.isEmpty()) {
                    item {
                        Text(
                            "Nema stanovnika u ovoj kategoriji",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }

                items(filteredList) { resident ->
                    EmergencyResidentCard(
                        resident = resident,
                        onEvacuateClick = {
                            viewModel.markAsEvacuated(resident.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyResidentCard(resident: Resident, onEvacuateClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (resident.isEvacuated) Color(0xFFE8F5E9) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        onClick = { onEvacuateClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resident.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Soba ${resident.roomNumber}",
                    color = Color.Gray
                )

                val statusColor = when (resident.mobilityStatus.uppercase()) {
                    "NEPOKRETAN" -> Color(0xFFB71C1C)
                    "OTEZANO POKRETAN", "OTEŽANO POKRETAN" -> Color(0xFFFF6F00)
                    else -> Color(0xFF2E7D32)
                }

                Text(
                    text = resident.mobilityStatus.uppercase(),
                    color = statusColor,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (!resident.isEvacuated) {
                Button(
                    onClick = onEvacuateClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("EVAKUIŠI", fontWeight = FontWeight.Bold)
                }
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Evakuisan",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}