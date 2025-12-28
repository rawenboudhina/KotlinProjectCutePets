package com.rawen.mycutepets.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rawen.mycutepets.viewmodel.PetViewModel
import com.rawen.mycutepets.ui.screen.*


@Composable
fun CutePetsApp(
    navController: NavHostController = rememberNavController(),
    viewModel: PetViewModel = viewModel()
) {
    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route ?: "selection"
            NavigationBar(modifier = Modifier.height(70.dp)) {
                NavigationBarItem(
                    selected = currentRoute.startsWith("selection") || currentRoute == "home",
                    onClick = {
                        navController.navigate("selection") {
                            popUpTo("selection") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    icon = { Text("🏠") },
                    label = { Text("Accueil") }
                )
                NavigationBarItem(
                    selected = currentRoute.startsWith("favorites"),
                    onClick = {
                        navController.navigate("favorites") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Text("❤️") },
                    label = { Text("Favoris") }
                )
                NavigationBarItem(
                    selected = currentRoute.startsWith("profile"),
                    onClick = {
                        navController.navigate("profile") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Text("👤") },
                    label = { Text("Mon Espace") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = "selection")
        {
            composable("selection") {
                SelectionScreen(navController = navController)
            }
            composable("breeds/{petType}") { backStackEntry ->
                val petType = backStackEntry.arguments?.getString("petType") ?: "dog"
                val isDog = petType == "dog"
                BreedsScreen(
                    isDog = isDog,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("home") {
                HomeScreen(viewModel = viewModel, navController = navController)
            }
            composable("detail/{petId}/{isDog}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: return@composable
                val isDog = backStackEntry.arguments?.getString("isDog")?.toBoolean() ?: false
                val pet = viewModel.pets.value.find { it.id == petId && it.isDog == isDog }
                    ?: viewModel.favorites.value.find { it.id == petId && it.isDog == isDog }
                pet?.let {
                    DetailScreen(
                        pet = it,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onAdopt = { navController.navigate("adopt_form/${it.id}/${it.isDog}") }
                    )
                }
            }
            composable("favorites") {
                FavoritesScreen(viewModel = viewModel, navController = navController)
            }
            composable("profile") {
                ProfileScreen(navController = navController, viewModel = viewModel)
            }
            composable("adopt_form/{petId}/{isDog}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: return@composable
                val isDog = backStackEntry.arguments?.getString("isDog")?.toBoolean() ?: false
                val pet = viewModel.pets.value.find { it.id == petId && it.isDog == isDog }
                    ?: viewModel.favorites.value.find { it.id == petId && it.isDog == isDog }
                pet?.let {
                    AdoptFormScreen(
                        pet = it,
                        onSubmit = { name, phone, note ->
                            viewModel.submitAdoptionRequest(it.id, it.isDog, it.breedName, name, phone, note)
                            navController.navigate("profile") { launchSingleTop = true }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable("add_animal") {
                AddAnimalScreen(
                    isDog = viewModel.isDogMode.value,
                    breedName = viewModel.currentBreedName,
                    onSubmit = { title, imageUrl ->
                        viewModel.addAdoptionListing(title, imageUrl, viewModel.isDogMode.value, viewModel.currentBreedName)
                        navController.navigate("profile") { launchSingleTop = true }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
        }
    }
}
