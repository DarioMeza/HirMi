package cl.example.hirmi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.example.hirmi.model.User
import cl.example.hirmi.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    // ========== ESTADOS ==========
    // Lista de usuarios que se mostrará en pantalla (inicia vacía)
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    // Estados para el modal de distancia
    var showDistanceModal by remember { mutableStateOf(false) }  // Controla visibilidad del modal
    var scanDistance by remember { mutableStateOf("100") }       // Almacena la distancia ingresada
    var isDistanceError by remember { mutableStateOf(false) }    // Controla errores de validación

    // ========== MODAL DE DISTANCIA ==========
    if (showDistanceModal) {
        AlertDialog(
            onDismissRequest = { showDistanceModal = false },
            title = { Text("Configurar distancia de escaneo") },
            text = {
                Column {
                    // Campo para ingresar la distancia
                    OutlinedTextField(
                        value = scanDistance,
                        onValueChange = { newValue ->
                            scanDistance = newValue
                            // Valida que sea un número positivo
                            isDistanceError = newValue.toIntOrNull()?.let { value ->
                                value <= 0 || value > 100
                            } ?: true
                        },
                        label = { Text("Distancia máxima (metros)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = isDistanceError,
                        supportingText = {
                            if (isDistanceError) {
                                Text("Por favor ingresa un número válido entre 1 y 100")
                        } else {
                                Text("Máximo 100 metros")
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    // Texto informativo
                    Text(
                        "Distancia de busqueda: ${scanDistance} metros de distancia",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDistanceError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            // Botón para confirmar la distancia y buscar usuarios
            confirmButton = {
                Button(
                    onClick = {
                        scanDistance.toIntOrNull()?.let { distance ->
                            if (distance > 0) {
                                showDistanceModal = false
                                // Actualiza la lista de usuarios según la distancia ingresada
                                users = UserRepository.getUsers(distance)
                            }
                        }
                    },
                    enabled = !isDistanceError && scanDistance.isNotEmpty()
                ) {
                    Text("Escanear")
                }
            },
            // Botón para cancelar
            dismissButton = {
                TextButton(onClick = { showDistanceModal = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ========== ESTRUCTURA PRINCIPAL ==========
    Scaffold(
        // Barra superior con título y acciones
        topBar = {
            TopAppBar(
                title = { Text("HirMi") },
                actions = {
                    // Botones de la barra superior
                    IconButton(onClick = { /* TODO: Implementar filtros */ }) {
                        Icon(Icons.Filled.Tune, contentDescription = "Filtros")
                    }
                    IconButton(onClick = { /* TODO: Implementar perfil */ }) {
                        Icon(Icons.Filled.Person, contentDescription = "Perfil")
                    }
                }
            )
        },
        // Barra de navegación inferior
        bottomBar = {
            NavigationBar {
                // Botones de navegación
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { /* TODO: Navegación */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.MusicNote, contentDescription = "Música") },
                    label = { Text("Música") },
                    selected = false,
                    onClick = { /* TODO: Navegación */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.People, contentDescription = "Comunidad") },
                    label = { Text("Comunidad") },
                    selected = false,
                    onClick = { /* TODO: Navegación */ }
                )
            }
        },
        // Botón flotante para iniciar el escaneo
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDistanceModal = true }
            ) {
                Icon(
                    if (users.isEmpty()) Icons.Filled.PlayArrow else Icons.Filled.Refresh,
                    contentDescription = "Configurar escaneo"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        // ========== CONTENIDO PRINCIPAL ==========
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (users.isEmpty()) {
                // Estado inicial - Mensaje cuando no hay usuarios
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
                // Lista de usuarios encontrados
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { user ->
                        UserCard(user = user)
                    }
                }
            }
        }
    }
}

// ========== TARJETA DE USUARIO ==========
@Composable
fun UserCard(user: User) {
    // Control de expansión de la tarjeta
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera de la tarjeta: Avatar + Información básica
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar con iniciales
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

                // Información de usuario
                Column {
                    Text("${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold)
                    Text("@${user.username}", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información musical y distancia
            Text("Canción: ${user.song?.title ?: "Sin canción"}", fontWeight = FontWeight.SemiBold)
            Text("Artista: ${user.song?.artist ?: "Sin artista"}", fontWeight = FontWeight.SemiBold)
            Text("Distancia: ${user.distance} metros")

            // Contenido expandible
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                // Detalles adicionales
                Text("Álbum: ${user.song?.album ?: "N/A"}")
                Text("Género: ${user.song?.genre ?: "N/A"}")

                Spacer(modifier = Modifier.height(8.dp))
                // Botones de acción
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { /* TODO: Implementar seguir */ }) {
                        Text("Seguir")
                    }
                    Button(onClick = { /* TODO: Implementar mensaje */ }) {
                        Text("Mensaje")
                    }
                }
            }
        }
    }
}
