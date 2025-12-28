package com.rawen.mycutepets.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SelectionScreen(navController: NavController) {
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Titre principal
            Text(
                text = "Bienvenue ! 🎉",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp
                ),
                color = Color(0xFF5D4037),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choisissez votre compagnon préféré",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF8D6E63),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Carte pour les chiens
            AnimatedPetCard(
                emoji = "🐶",
                title = "Chiens Adorables",
                description = "Nos amis fidèles et joueurs",
                backgroundColor = Color(0xFFFFEBEE),
                accentColor = Color(0xFFE57373),
                onClick = { navController.navigate("breeds/dog") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Carte pour les chats
            AnimatedPetCard(
                emoji = "🐱",
                title = "Chats Adorables",
                description = "Nos compagnons élégants ",
                backgroundColor = Color(0xFFE3F2FD),
                accentColor = Color(0xFF64B5F6),
                onClick = { navController.navigate("breeds/cat") }
            )
        }
    }
}

@Composable
fun AnimatedPetCard(
    emoji: String,
    title: String,
    description: String,
    backgroundColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .scale(scale)
            .clickable(
                onClick = onClick,
                onClickLabel = title
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = emoji,
                    fontSize = 70.sp,
                    modifier = Modifier
                        .offset(y = (-animatedOffset / 2).dp)
                )
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = accentColor
                    )
                }
            }

            // Contenu texte
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(0.55f)
                    .padding(end = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accentColor,
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5D4037).copy(alpha = 0.85f),
                    textAlign = TextAlign.End
                )
            }

            // Indicateur de clic
            
        }
    }
}
