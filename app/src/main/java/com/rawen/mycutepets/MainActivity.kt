package com.rawen.mycutepets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rawen.mycutepets.ui.navigation.CutePetsApp  // ← À adapter selon ton organisation
import com.rawen.mycutepets.ui.theme.MyCutePetsTheme  // Ou CutePetsTheme si tu l'as renommé

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCutePetsTheme {  // Garde le nom de ton thème (MyCutePetsTheme ou CutePetsTheme)
                // Surface pour appliquer la couleur de fond du thème
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CutePetsApp()  // Lance toute notre app avec navigation
                }
            }
        }
    }
}