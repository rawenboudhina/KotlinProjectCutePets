package com.rawen.mycutepets.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rawen.mycutepets.data.PetImage
import com.rawen.mycutepets.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(pet: PetImage, viewModel: PetViewModel, onBack: () -> Unit, onAdopt: () -> Unit) {
    val isFavorite = viewModel.isFavorite(pet.id)
    val isDog = pet.isDog
    val breeds by viewModel.breeds
    val info = breeds.firstOrNull { it.name.equals(pet.breedName, true) && it.isDog == isDog }
    var showConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(pet.breedName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isFavorite) {
                            showConfirm = true
                        } else {
                            viewModel.toggleFavorite(pet)
                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDog) {
                            listOf(Color(0xFFFFEBEE), Color(0xFFFFF3E0))
                        } else {
                            listOf(Color(0xFFE3F2FD), Color(0xFFF3E5F5))
                        }
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = pet.localPath ?: pet.url,
                    contentDescription = "Animal",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = pet.breedName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (info != null) {
                    Spacer(Modifier.height(8.dp))
                    if (!info.description.isNullOrBlank()) {
                        Text(text = info.description ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { onAdopt() }) {
                        Text("Adopter")
                    }
                    Button(onClick = { onBack() }) { Text("Retour") }
                }
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
                    viewModel.toggleFavorite(pet)
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
