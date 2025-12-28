package com.rawen.mycutepets.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF3E0),
                            Color(0xFFFFE0B2),
                            Color(0xFFFFCC80)
                        )
                    )
                )
                .padding(padding)
        ) {
            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = Color(0xFFBDBDBD),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "Aucun favori pour l'instant",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF5D4037),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Ajoutes-en en cliquant sur ❤️ ! 🐾",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8D6E63),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    items(favorites, key = { it.id }) { pet ->
                        PetCard(
                            pet = pet,
                            isFavorite = true,
                            onToggleFavorite = { viewModel.toggleFavorite(pet) },
                            onClick = { navController.navigate("detail/${pet.id}/${pet.isDog}") },
                            onAdopt = { navController.navigate("adopt_form/${pet.id}/${pet.isDog}") }
                        )
                    }
                }
            }
        }
    }
}