package com.rawen.mycutepets.ui.screen  // ← Vérifie que c'est bien .ui.screen si tu l'as mis là

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rawen.mycutepets.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: PetViewModel, navController: NavController) {
    val favorites by viewModel.favorites

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mes Favoris ❤️") }
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucun favori pour l'instant... Ajoutes-en ! 🐾")
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                modifier = Modifier.padding(padding)
            ) {
                items(favorites, key = { it.id }) { pet ->
                    PetCard(
                        pet = pet,
                        isFavorite = true,  // Dans les favoris, c'est toujours true
                        onToggleFavorite = { viewModel.toggleFavorite(pet) },
                        onClick = { navController.navigate("detail/${pet.id}/${pet.isDog}") },
                        onAdopt = {}
                    )
                }
            }
        }
    }
}
