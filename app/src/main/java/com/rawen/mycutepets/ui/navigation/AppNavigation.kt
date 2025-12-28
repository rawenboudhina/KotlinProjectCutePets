package com.rawen.mycutepets.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rawen.mycutepets.viewmodel.PetViewModel
import com.rawen.mycutepets.ui.screen.*


@Composable
fun CutePetsApp(
    navController: NavHostController = rememberNavController(),
    viewModel: PetViewModel = viewModel()
) {
    NavHost(navController = navController, startDestination = "selection") {
        // Page de sélection initiale (Chiens ou Chats)
        composable("selection") {
            SelectionScreen(navController = navController)
        }

        // Page de sélection des races
        composable("breeds/{petType}") { backStackEntry ->
            val petType = backStackEntry.arguments?.getString("petType") ?: "dog"
            val isDog = petType == "dog"
            BreedsScreen(
                isDog = isDog,
                viewModel = viewModel,
                navController = navController
            )
        }

        // Page d'accueil avec la liste des animaux
        composable("home") {
            HomeScreen(viewModel = viewModel, navController = navController)
        }

        // Page de détails d'un animal
        composable("detail/{petId}/{isDog}") { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: return@composable
            val isDog = backStackEntry.arguments?.getString("isDog")?.toBoolean() ?: false
            val pet = viewModel.pets.value.find { it.id == petId && it.isDog == isDog }
                ?: viewModel.favorites.value.find { it.id == petId && it.isDog == isDog }
            pet?.let {
                DetailScreen(
                    pet = it,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Page des favoris
        composable("favorites") {
            FavoritesScreen(viewModel = viewModel, navController = navController)
        }
    }
}