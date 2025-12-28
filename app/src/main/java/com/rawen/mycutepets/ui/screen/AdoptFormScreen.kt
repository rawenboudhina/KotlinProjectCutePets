package com.rawen.mycutepets.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rawen.mycutepets.data.PetImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptFormScreen(
    pet: PetImage,
    onSubmit: (name: String, phone: String, note: String?) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Demande d'adoption") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
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
            Text("Race: ${pet.breedName}")
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom complet") }, singleLine = true)
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Téléphone") }, singleLine = true)
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Message") })
            Button(
                onClick = { onSubmit(name.trim(), phone.trim(), note.trim().ifBlank { null }) },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Envoyer la demande")
            }
        }
    }
}
