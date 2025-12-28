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
    val pets by viewModel.pagedPets
    val allPets by viewModel.pets
    val isLoading by viewModel.isLoading
    val isDogMode by viewModel.isDogMode
    val pageIndex by viewModel.pageIndex
    val pageSize by viewModel.pageSize

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { navController.navigate("favorites") }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favoris")
                    }
                    TextButton(onClick = { navController.navigate(if (isDogMode) "breeds/dog" else "breeds/cat") }) {
                        Text("Races")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_animal") }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val total = allPets.size
                val start = if (total == 0) 0 else pageIndex * pageSize + 1
                val end = (pageIndex * pageSize + pageSize).coerceAtMost(total)
                Text(
                    text = "Page ${pageIndex + 1} — Images $start–$end",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.prevPage() },
                    enabled = viewModel.hasPrevPage()
                ) {
                    Text("Précédent")
                }
                Button(
                    onClick = { viewModel.nextPage() },
                    enabled = !isLoading && (viewModel.hasNextPage() || allPets.isNotEmpty())
                ) {
                    Text("Suivant")
                }
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
                        onClick = { navController.navigate("detail/${pet.id}/${pet.isDog}") },
                        onAdopt = { navController.navigate("adopt_form/${pet.id}/${pet.isDog}") }
                    )
                }

                if (isLoading) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(32.dp))
                    }
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
    onClick: () -> Unit,
    onAdopt: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box {
            AsyncImage(
                model = pet.localPath ?: pet.url,
                contentDescription = "Pet cute",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            )

            IconButton(
                onClick = {
                    if (isFavorite) {
                        showConfirm = true
                    } else {
                        onToggleFavorite()
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            Button(
                onClick = { onAdopt() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text("Adopter")
            }
        }
    }
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Retirer des favoris ?") },
            text = { Text("Êtes-vous sûr de vouloir retirer cet animal de vos favoris ?") },
            confirmButton = {
                TextButton(onClick = {
                    onToggleFavorite()
                    showConfirm = false
                }) {
                    Text("Oui")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Non")
                }
            }
        )
    }
}
