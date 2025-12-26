package com.rawen.mycutepets.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rawen.mycutepets.data.PetImage
import com.rawen.mycutepets.viewmodel.PetViewModel
import androidx.compose.foundation.shape.CircleShape
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: PetViewModel, navController: NavController) {
    val pets by viewModel.pets
    val isLoading by viewModel.isLoading
    val isDogMode by viewModel.isDogMode

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isDogMode) "Chiens Adorables 🐶" else "Chats Adorables 🐱") },
                actions = {
                    IconButton(onClick = { navController.navigate("favorites") }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favoris")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Button(
                onClick = { viewModel.togglePetType() },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
            ) {
                Text(if (isDogMode) "Voir les chats 🐱" else "Voir les chiens 🐶")
            }

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp
            ) {
                items(pets, key = { it.id }) { pet ->
                    PetCard(
                        pet = pet,
                        isFavorite = viewModel.isFavorite(pet.id),
                        onToggleFavorite = { viewModel.toggleFavorite(pet) },
                        onClick = { navController.navigate("detail/${pet.id}/${pet.isDog}") }
                    )
                }

                if (isLoading) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(32.dp))
                    }
                }
            }

            // Chargement infini
            LaunchedEffect(pets.size) {
                if (!isLoading && pets.isNotEmpty()) {
                    viewModel.loadMore()
                }
            }
        }
    }
}

@Composable
fun PetCard(
    pet: PetImage,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box {
            AsyncImage(
                model = pet.url,
                contentDescription = "Pet cute",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            )

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}