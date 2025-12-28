package com.rawen.mycutepets.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalScreen(
    isDog: Boolean,
    breedName: String,
    onSubmit: (title: String, imageUrl: String?) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ajouter un animal à l'adoption") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = if (isDog) "Type: Chien" else "Type: Chat")
            Text(text = "Race: $breedName")
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titre") }, singleLine = true)
            OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL de l'image (optionnel)") }, singleLine = true)
            Button(
                onClick = { onSubmit(title.trim(), imageUrl.trim().ifBlank { null }) },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publier")
            }
        }
    }
}
