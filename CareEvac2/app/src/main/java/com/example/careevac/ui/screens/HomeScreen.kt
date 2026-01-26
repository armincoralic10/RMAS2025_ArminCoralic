package com.example.careevac.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.careevac.R
import com.example.careevac.navigation.Screen
import com.example.careevac.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CareEvac", fontWeight = FontWeight.Bold)
                        currentUser?.let {
                            Text(
                                text = "Dobrodošli, ${it.fullName}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Rola: ${getRoleName(it.role)}",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.logout {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Odjavi se",
                            tint = Color.Red
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.careevac),
                contentScale = ContentScale.Fit,
                contentDescription = "CareEvac Logo",
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(120.dp))

            Button(
                onClick = { navController.navigate(Screen.Emergency.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "POKRENI HITNU\nEVAKUACIJU",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentUser?.isAdmin() == true) {
                Button(
                    onClick = { navController.navigate(Screen.Admin.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Administracija stanara", fontWeight = FontWeight.Medium)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Text(
                        text = "Nemate pristup administraciji (samo Admin)",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }
    }
}

private fun getRoleName(role: String): String {
    return when (role) {
        "super_admin" -> "Super Admin"
        "admin" -> "Admin"
        "staff" -> "Osoblje"
        else -> "Nepoznato"
    }
}