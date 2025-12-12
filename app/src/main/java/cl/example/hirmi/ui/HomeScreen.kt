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

    // Nuevo: modal de perfil ajeno
    var showProfileModal by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<ApiUser?>(null) }

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

    // Nuevo: Modal de perfil de usuario remoto
    if (showProfileModal && selectedUser != null) {
        // Asegurar que tenemos el catálogo completo de usuarios remotos
        LaunchedEffect(selectedUser!!.id) {
            viewModel.clearRemoteError()
            // intenta precargar todos los usuarios (no filtrados por distancia)
            // para poder mapear correctamente followers/following por ID
            // (la recomposición se disparará cuando el StateFlow se actualice)
            // No importa si ya está cargado: la repo se encarga.
            // Nota: si la API falla, el modal seguirá mostrando contadores.
            viewModel.scanRemoteUsers(100)
        }

        val (followersCount, followingCount) = viewModel.getCountersFor(selectedUser!!.id)
        // NUEVO: obtener listas reales para el modal
        val followersList = viewModel.getFollowersFor(selectedUser!!.id)
        val followingList = viewModel.getFollowingFor(selectedUser!!.id)
        UserProfileModal(
            user = selectedUser!!,
            isFollowing = remoteFollows.containsKey(selectedUser!!.id),
            followersCount = followersCount,
            followingCount = followingCount,
            followers = followersList,
            following = followingList,
            onClose = { showProfileModal = false },
            onToggleFollow = {
                viewModel.toggleFollowFor(selectedUser!!)
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
                                                onOpenProfile = {
                                                    selectedUser = user
                                                    showProfileModal = true
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
                                                onOpenProfile = {
                                                    selectedUser = user
                                                    showProfileModal = true
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
    onOpenProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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

                    Column(
                        modifier = Modifier.clickable { onOpenProfile() }
                    ) {
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onFollowClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) Color.LightGray else Color.Black,
                        contentColor = if (isFollowing) Color.Black else Color.White
                    )
                ) {
                    Text(if (isFollowing) "Siguiendo" else "Seguir")
                }
                Button(
                    onClick = onOpenProfile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Ver perfil")
                }
            }
        }
    }
}

// ============================= MODAL DE PERFIL AJENO =============================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileModal(
    user: ApiUser,
    isFollowing: Boolean,
    followersCount: Int,
    followingCount: Int,
    followers: List<ApiUser>,
    following: List<ApiUser>,
    onClose: () -> Unit,
    onToggleFollow: () -> Unit
) {
    // Estado para mostrar sub-listados
    var showList by remember { mutableStateOf<UserListType?>(null) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f)
                .widthIn(min = 380.dp, max = 560.dp)
                .heightIn(min = 460.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Avatar
                if (!user.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(104.dp)
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
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(user.bio ?: "Sin biografía", color = Color.Gray, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(14.dp))
                // Canción
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(user.song?.title ?: "Sin canción favorita")
                }
                user.song?.artist?.let { Text("de $it", color = Color.Gray) }

                Spacer(modifier = Modifier.height(16.dp))
                // Contadores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showList = UserListType.Followers }
                    ) {
                        Text("Seguidores", fontWeight = FontWeight.Bold)
                        Text(followersCount.toString())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showList = UserListType.Following }
                    ) {
                        Text("Seguidos", fontWeight = FontWeight.Bold)
                        Text(followingCount.toString())
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Botones apilados: ocupan ancho completo para mantener armonía
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        onClick = onToggleFollow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) Color.LightGray else Color.Black,
                            contentColor = if (isFollowing) Color.Black else Color.White
                        )
                    ) {
                        Text(text = if (isFollowing) "Dejar de seguir" else "Seguir", maxLines = 1)
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        onClick = { /* TODO: abrir chat */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(text = "Mensaje", maxLines = 1)
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(text = "Cerrar", maxLines = 1)
                    }
                }
            }
        }
    }

    // SUB-MODAL: listado de seguidores/seguidos
    when (showList) {
        UserListType.Followers -> {
            UserListModal(
                title = "Seguidores",
                users = followers,
                onClose = { showList = null }
            )
        }
        UserListType.Following -> {
            UserListModal(
                title = "Seguidos",
                users = following,
                onClose = { showList = null }
            )
        }
        null -> {}
    }
}

// Tipo de listado
private enum class UserListType { Followers, Following }

// Modal para mostrar lista simple de usuarios
@Composable
private fun UserListModal(
    title: String,
    users: List<ApiUser>,
    onClose: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.6f)
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                if (users.isEmpty()) {
                    Text("No hay elementos", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(users) { u ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (!u.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = u.avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.LightGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val initials = (u.firstName.firstOrNull()?.toString() ?: "") +
                                            (u.lastName.firstOrNull()?.toString() ?: "")
                                        Text(initials.ifEmpty { "?" })
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("${u.firstName} ${u.lastName}", fontWeight = FontWeight.SemiBold)
                                    Text(u.bio ?: "Sin biografía", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray, contentColor = Color.Black)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}
