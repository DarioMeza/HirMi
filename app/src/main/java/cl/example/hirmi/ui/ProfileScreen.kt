package cl.example.hirmi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.example.hirmi.viewmodel.UserViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: UserViewModel
) {
    val remoteFollows by viewModel.remoteFollows.collectAsState()

    val followedCount = remoteFollows.size
    val followersCount = 0 // TODO: requiere endpoint "followers" (quién sigue al usuario actual)

    val currentUser by viewModel.currentUser.collectAsState()

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            navController.navigate("welcome") {
                popUpTo("home") { inclusive = true }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PROFILE",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            // ===== AVATAR GRANDE =====
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                val initials = buildString {
                    currentUser!!.firstName.firstOrNull()?.let { append(it) }
                    currentUser!!.lastName.firstOrNull()?.let { append(it) }
                }.ifBlank { "?" }

                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ===== USUARIO =====
            Text(
                text = currentUser!!.username,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ===== FOLLOWERS / FOLLOWED =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FOLLOWERS", fontWeight = FontWeight.Bold)
                    Text(followersCount.toString())
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FOLLOWED", fontWeight = FontWeight.Bold)
                    Text(followedCount.toString())
                }
            }



            Spacer(modifier = Modifier.height(28.dp))

            // ===== CARD DE MÚSICA =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    val song = currentUser!!.song

                    Text(
                        text = song?.title ?: "Sin canción",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = song?.artist ?: "",
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Portada
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PORTADA", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Barra progreso
                    LinearProgressIndicator(
                        progress = { 0.35f },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("01:56", style = MaterialTheme.typography.bodySmall)
                        Text("-03:46", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
