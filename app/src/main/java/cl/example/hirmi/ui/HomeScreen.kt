package cl.example.hirmi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.example.hirmi.api.ApiUser
import cl.example.hirmi.viewmodel.UserViewModel
import coil.compose.AsyncImage

// Tabs internos de la Home
enum class HomeTab {
    INICIO,
    AMIGOS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: UserViewModel) {
    // Local (Room) – solo para usuario actual / sesión
    val scanned by viewModel.scanned.collectAsState()
    val lastDistance by viewModel.lastDistance.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Remoto (API externa)
    val remoteUsers by viewModel.remoteUsers.collectAsState()
    val remoteLoading by viewModel.remoteLoading.collectAsState()
    val remoteError by viewModel.remoteError.collectAsState()

    // Follows remotos (MockAPI)
    val remoteFollows by viewModel.remoteFollows.collectAsState()

    var showDistanceModal by remember { mutableStateOf(false) }
    var scanDistance by remember { mutableStateOf("100") }
    var isDistanceError by remember { mutableStateOf(false) }

    // Tab seleccionado en la barra inferior (Inicio por defecto)
    var selectedTab by remember { mutableStateOf(HomeTab.INICIO) }

    // ============================= MODAL DE DISTANCIA =============================
    if (showDistanceModal) {
        AlertDialog(
            onDismissRequest = { showDistanceModal = false },
            title = { Text("Configurar distancia de escaneo") },
            text = {
                Column {
                    OutlinedTextField(
                        value = scanDistance,
                        onValueChange = {
                            scanDistance = it
                            isDistanceError = it.toIntOrNull()?.let { v -> v !in 1..100 } ?: true
                        },
                        label = { Text("Distancia máxima (metros)") },
                        isError = isDistanceError
                    )
                    if (isDistanceError) {
                        Text(
                            "Por favor ingresa un número entre 1 y 100",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("Máximo 100 metros", color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scanDistance.toIntOrNull()?.let { distance ->
                            if (!isDistanceError) {
                                viewModel.scanRemoteUsers(distance)
                                showDistanceModal = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text("Escanear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDistanceModal = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ============================= ESTRUCTURA PRINCIPAL =============================
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HirMi") },
                actions = {
                    IconButton(
                        onClick = {
                            if (currentUser != null) {
                                navController.navigate("profile")
                            } else {
                                navController.navigate("login")
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = "Perfil")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = selectedTab == HomeTab.INICIO,
                    onClick = { selectedTab = HomeTab.INICIO },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = Color(0xFFE0E0E0),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Amigos") },
                    label = { Text("Amigos") },
                    selected = selectedTab == HomeTab.AMIGOS,
                    onClick = { selectedTab = HomeTab.AMIGOS },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = Color(0xFFE0E0E0),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDistanceModal = true },
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Radar, contentDescription = "Escanear")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                remoteLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Buscando personas cercanas...")
                    }
                }

                remoteError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = remoteError ?: "Ocurrió un error al buscar usuarios.",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showDistanceModal = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Intentar de nuevo")
                        }
                    }
                }

                else -> {
                    val followingUsers = remoteUsers.filter { remoteFollows.containsKey(it.id) }
                    val otherUsers = if (scanned) {
                        remoteUsers
                    } else {
                        emptyList()
                    }

                    when (selectedTab) {
                        HomeTab.INICIO -> {
                            // SOLO PERSONAS CERCANAS, sin sección de Amigos
                            if (!scanned) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Pulsa el botón de radar para buscar personas cercanas.",
                                        textAlign = TextAlign.Center,
                                        color = Color.Gray
                                    )
                                }
                            } else if (otherUsers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No se encontraron usuarios a menos de $lastDistance metros.",
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Personas cercanas (≤ $lastDistance m)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(otherUsers) { user ->
                                            val isFollowing = remoteFollows.containsKey(user.id)
                                            RemoteUserCard(
                                                user = user,
                                                isFollowing = isFollowing,
                                                onFollowClick = { viewModel.toggleFollowFor(user) },
                                                onMessageClick = {
                                                    // TODO: Navegar a ChatScreen
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HomeTab.AMIGOS -> {
                            // SOLO AMIGOS (seguidos)
                            if (followingUsers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Todavía no sigues a nadie.\nExplora en Inicio para encontrar personas.",
                                        textAlign = TextAlign.Center,
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Amigos",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(followingUsers) { user ->
                                            RemoteUserCard(
                                                user = user,
                                                isFollowing = true,
                                                onFollowClick = { viewModel.toggleFollowFor(user) },
                                                onMessageClick = {
                                                    // TODO: Navegar a ChatScreen
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================================= TARJETA DE USUARIO REMOTO (API) ====================================

@Composable
fun RemoteUserCard(
    user: ApiUser,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // HEADER: avatar + nombre + distancia
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!user.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = "Foto de ${user.firstName}",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val initials = buildString {
                            user.firstName.firstOrNull()?.let { append(it) }
                            user.lastName.firstOrNull()?.let { append(it) }
                        }.ifEmpty { "?" }

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                initials,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.bio ?: "Sin biografía",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${user.distance} m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canción principal
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = "Canción",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = user.song?.title ?: "Sin canción favorita",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            user.song?.artist?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "de $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                user.song?.let { song ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Álbum: ${song.album}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Género: ${song.genre}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onFollowClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (isFollowing) "Siguiendo" else "Seguir")
                    }
                    Button(
                        onClick = onMessageClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Mensaje")
                    }
                }
            }

            // Expansión al tocar el texto
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (expanded) "Ver menos" else "Ver más",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { expanded = !expanded }
            )
        }
    }
}
