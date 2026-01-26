package com.example.careevac.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.careevac.model.Resident
import com.example.careevac.viewmodel.ResidentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditResidentScreen(
    navController: NavController,
    viewModel: ResidentViewModel,
    residentId: String? = null
) {
    val isEditMode = residentId != null
    val residents by viewModel.residents.collectAsState()
    val currentResident = residents.find { it.id == residentId }

    var fullName by remember { mutableStateOf(currentResident?.fullName ?: "") }
    var roomNumber by remember { mutableStateOf(currentResident?.roomNumber ?: "") }
    var emergencyContact by remember { mutableStateOf(currentResident?.emergencyContact ?: "") }
    var medicalNotes by remember { mutableStateOf(currentResident?.medicalNotes ?: "") }

    val mobilityStatuses = listOf("Pokretan", "Otezano pokretan", "Nepokretan")
    var selectedStatus by remember {
        mutableStateOf(currentResident?.mobilityStatus ?: mobilityStatuses[0])
    }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Uredi stanara" else "Novi stanar")
                },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Text(
                        "Ime i Prezime",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("npr. Ivan Ivić") },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Broj Sobe",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("npr. 101") },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Kontakt za hitne slučajeve",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = emergencyContact,
                        onValueChange = { emergencyContact = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("npr. +387 61 123 456") },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Medicinske napomene",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = medicalNotes,
                        onValueChange = { medicalNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("npr. Dijabetes tip 2") },
                        shape = RoundedCornerShape(8.dp),
                        minLines = 2,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Status mobilnosti",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(Modifier.selectableGroup()) {
                        mobilityStatuses.forEach { status ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = (status == selectedStatus),
                                        onClick = { selectedStatus = status },
                                        role = Role.RadioButton,
                                        enabled = !isLoading
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (status == selectedStatus),
                                    onClick = null,
                                    enabled = !isLoading
                                )
                                Text(
                                    text = status,
                                    modifier = Modifier.padding(start = 12.dp),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (fullName.isBlank() || roomNumber.isBlank()) {
                        errorMessage = "Ime i broj sobe su obavezni"
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        val resident = Resident(
                            id = residentId ?: "",
                            fullName = fullName,
                            roomNumber = roomNumber,
                            mobilityStatus = selectedStatus,
                            emergencyContact = emergencyContact,
                            medicalNotes = medicalNotes,
                            isEvacuated = currentResident?.isEvacuated ?: false
                        )

                        val success = if (isEditMode) {
                            viewModel.updateResident(resident)
                        } else {
                            viewModel.addResident(resident)
                        }

                        isLoading = false

                        if (success) {
                            navController.popBackStack()
                        } else {
                            errorMessage = "Greška pri ${if (isEditMode) "ažuriranju" else "dodavanju"} stanara"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2C2C)
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEditMode) "SAČUVAJ IZMJENE" else "DODAJ STANARA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}