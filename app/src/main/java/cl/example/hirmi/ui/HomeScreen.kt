package cl.example.hirmi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import cl.example.hirmi.model.User
import cl.example.hirmi.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: UserViewModel) {
    val users by viewModel.users.collectAsState()
    val scanned by viewModel.scanned.collectAsState()
    val lastDistance by viewModel.lastDistance.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showDistanceModal by remember { mutableStateOf(false) }
    var showProfileModal by remember { mutableStateOf(false) }
    var scanDistance by remember { mutableStateOf("100") }
    var isDistanceError by remember { mutableStateOf(false) }

    // ============================= MODAL DE PERFIL =============================
    if (showProfileModal && currentUser != null) {
        AlertDialog(
            onDismissRequest = { showProfileModal = false },
            confirmButton = {
                TextButton(onClick = { showProfileModal = false }) {
                    Text("Cerrar")
                }
            },
            title = { Text("Mi perfil", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Nombre: ${currentUser!!.firstName} ${currentUser!!.lastName}")
                    Text("Usuario: @${currentUser!!.username}")
                    Text("Correo: ${currentUser!!.email}")
                    Text("Fecha de nacimiento: ${currentUser!!.birthdate}")
                    if (currentUser!!.song != null) {
                        Text("Canción: ${currentUser!!.song!!.title}")
                        Text("Artista: ${currentUser!!.song!!.artist}")
                    }
                }
            }
        )
    }

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
                        scanDistance.toIntOrNull()?.let {
                            if (!isDistanceError) {
                                viewModel.filterByDistance(it)
                                showDistanceModal = false
                            }
                        }
                    }
                ) { Text("Escanear") }
            },
            dismissButton = {
                TextButton(onClick = { showDistanceModal = false }) {
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
                                showProfileModal = true
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
                    selected = true,
                    onClick = { /* ya estás en Home */ }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDistanceModal = true }) {
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
            if (!scanned) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Pulsa el botón para comenzar a buscar personas cercanas",
                        color = Color.Gray,
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
                        "Mostrando usuarios a menos de $lastDistance metros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(users) { user ->
                            UserCard(user)
                        }
                    }
                }
            }
        }
    }
}

// ================================= TARJETA DE USUARIO ===========================================

@Composable
fun UserCard(user: User) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${user.firstName.first()}${user.lastName.first()}",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text("${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold)
                    Text("@${user.username}", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Canción: ${user.song?.title ?: "Sin canción"}", fontWeight = FontWeight.SemiBold)
            Text("Artista: ${user.song?.artist ?: "Sin artista"}", fontWeight = FontWeight.SemiBold)
            Text("Distancia: ${user.distance} metros")

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Text("Álbum: ${user.song?.album ?: "N/A"}")
                Text("Género: ${user.song?.genre ?: "N/A"}")

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { /* seguir */ }) { Text("Seguir") }
                    Button(onClick = { /* mensaje */ }) { Text("Mensaje") }
                }
            }
        }
    }
}
