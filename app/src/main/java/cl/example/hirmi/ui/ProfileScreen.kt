package cl.example.hirmi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.example.hirmi.model.Song
import cl.example.hirmi.model.SongCatalog
import cl.example.hirmi.viewmodel.UserViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: UserViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val remoteFollows by viewModel.remoteFollows.collectAsState()
    val followedCount = remoteFollows.size
    val followersCountText = "—"

    var showSongPicker by remember { mutableStateOf(false) }

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            navController.navigate("welcome") {
                popUpTo("home") { inclusive = true }
            }
        }
        return
    }

    if (showSongPicker) {
        AlertDialog(
            onDismissRequest = { showSongPicker = false },
            title = { Text("Elegir canción") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(SongCatalog.songs) { song ->
                        SongRow(
                            song = song,
                            onClick = {
                                viewModel.updateNowPlaying(song)
                                showSongPicker = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSongPicker = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Volver")
                    }
                },
                actions = {
                    var expanded by remember { mutableStateOf(false) }

                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Cerrar sesión",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                expanded = false
                                viewModel.logout()
                                navController.navigate("welcome") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Cerrar sesión",
                                    tint = Color.Red
                                )
                            }
                        )
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

            // ===== AVATAR =====
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

            Text(
                text = currentUser!!.username,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ===== FOLLOW INFO =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Seguidores", fontWeight = FontWeight.Bold)
                    Text(followersCountText)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Seguidos", fontWeight = FontWeight.Bold)
                    Text(followedCount.toString())
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ===== MUSIC CARD =====
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
                    val context = LocalContext.current
                    val cover = song?.coverURL

                    val model: Any? = when {
                        cover.isNullOrBlank() -> null
                        cover.startsWith("res:") -> {
                            val resName = cover.removePrefix("res:")
                            val resId = context.resources.getIdentifier(
                                resName,
                                "drawable",
                                context.packageName
                            )
                            if (resId != 0) resId else null
                        }
                        else -> cover
                    }

                    Text(
                        text = song?.title ?: "Sin canción",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = song?.artist ?: "",
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (model != null) {
                            AsyncImage(
                                model = model,
                                contentDescription = "Portada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("PORTADA", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(progress = { 0.3f })
                }
            }
        }
    }
}

@Composable
fun SongRow(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(song.title)
        Spacer(modifier = Modifier.width(8.dp))
        Text(song.artist, color = Color.Gray)
    }
}

