package com.rawen.mycutepets.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh // Ajouté pour refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rawen.mycutepets.data.Breed // Utilise Breed (domain)
import com.rawen.mycutepets.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedsScreen(isDog: Boolean, viewModel: PetViewModel, navController: NavController) {
    LaunchedEffect(isDog) {
        viewModel.setPetType(isDog)
        viewModel.fetchBreeds(isDog)
    }
    val isDogMode by viewModel.isDogMode
    val breeds by viewModel.breeds // List<Breed>
    val isLoading by viewModel.isLoadingBreeds
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isDogMode) "Races de Chiens 🐶" else "Races de Chats 🐱",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("selection") {
                            popUpTo("selection") { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },

            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDogMode) {
                            listOf(Color(0xFFFFEBEE), Color(0xFFFFF3E0))
                        } else {
                            listOf(Color(0xFFE3F2FD), Color(0xFFF3E5F5))
                        }
                    )
                )
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    var searchQuery by rememberSaveable { mutableStateOf("") }
                    var selectedBreedTitle by remember { mutableStateOf<String?>(null) }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Rechercher")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Effacer")
                                }
                            }
                        },
                        placeholder = { Text("Rechercher une race") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                    val itemsList: List<Breed> = remember(searchQuery, breeds) {
                        val onlySpecific = breeds.filter { it.id.isNotEmpty() }
                        if (searchQuery.isBlank()) onlySpecific
                        else onlySpecific.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(itemsList, key = { it.id }) { breed ->
                            BreedCard(
                                breed = breed, // Breed
                                isDog = isDogMode,
                                onClick = {
                                    viewModel.selectBreed(breed.id, breed.name)
                                    selectedBreedTitle = if (breed.id.isEmpty()) {
                                        if (isDogMode) "Tous les chiens" else "Tous les chats"
                                    } else breed.name
                                    navController.navigate("home")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreedCard(
    breed: Breed,
    isDog: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDog) Color(0xFFFFF3E0) else Color(0xFFE1F5FE)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (breed.localImagePath != null || breed.imageUrl != null) {
                AsyncImage(
                    model = breed.localImagePath ?: breed.imageUrl, // Priorité au chemin local
                    contentDescription = breed.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = if (isDog) "🐶" else "🐱",
                    fontSize = 36.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                Text(
                    text = breed.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
