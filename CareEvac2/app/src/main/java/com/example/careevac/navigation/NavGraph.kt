package com.example.careevac.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.careevac.ui.screens.*
import com.example.careevac.viewmodel.AuthViewModel
import com.example.careevac.viewmodel.ResidentViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Admin : Screen("admin")
    object Emergency : Screen("emergency")
    object UserManagement : Screen("user_management")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean
) {
    val authViewModel: AuthViewModel = viewModel()
    val residentViewModel: ResidentViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Admin.route) {
            ResidentListScreen(
                navController = navController,
                viewModel = residentViewModel
            )
        }

        composable(Screen.Emergency.route) {
            EmergencyScreen(
                navController = navController,
                viewModel = residentViewModel,
                authViewModel = authViewModel
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }

        composable("add_resident") {
            AddEditResidentScreen(
                navController = navController,
                viewModel = residentViewModel
            )
        }

        composable("edit_resident/{residentId}") { backStackEntry ->
            val residentId = backStackEntry.arguments?.getString("residentId")
            AddEditResidentScreen(
                navController = navController,
                viewModel = residentViewModel,
                residentId = residentId
            )
        }
        composable("user_management") {
            UserManagementScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}