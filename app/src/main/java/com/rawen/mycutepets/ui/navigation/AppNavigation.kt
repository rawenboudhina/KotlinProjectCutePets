package com.rawen.mycutepets.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rawen.mycutepets.viewmodel.PetViewModel
import com.rawen.mycutepets.ui.screen.DetailScreen
import com.rawen.mycutepets.ui.screen.FavoritesScreen
import com.rawen.mycutepets.ui.screen.HomeScreen

@Composable
fun CutePetsApp(
    navController: NavHostController = rememberNavController(),
    viewModel: PetViewModel = viewModel()
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        composable("detail/{petId}/{isDog}") { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: return@composable
            val isDog = backStackEntry.arguments?.getString("isDog")?.toBoolean() ?: false

            val pet = viewModel.pets.value.find { it.id == petId && it.isDog == isDog }
                ?: viewModel.favorites.value.find { it.id == petId && it.isDog == isDog }

            pet?.let {
                DetailScreen(pet = it, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
        composable("favorites") {
            FavoritesScreen(viewModel = viewModel, navController = navController)
        }
    }
}