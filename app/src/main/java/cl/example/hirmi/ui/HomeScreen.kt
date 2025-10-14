package cl.example.hirmi.ui

// Componentes básicos de Compose (layouts, gestos, shapes, etc.)
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

// Iconos de Material (grupo "filled")
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

// Componentes de Material 3 (TopAppBar, NavigationBar, Card, etc.)
import androidx.compose.material3.*
import androidx.compose.runtime.*

// Utilidades de Compose (alineación, modificadores, previsualización, etc.)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

// Modelos de datos y repositorio simulado
import cl.example.hirmi.model.User
import cl.example.hirmi.repository.UserRepository

// Para convertir desplazamientos flotantes a enteros en el offset
import kotlin.math.roundToInt

// Iconos extendidos (requiere dependencia material-icons-extended)
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.People

// Opt-in para APIs experimentales de Material3 (TopAppBar)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    // Carga de usuarios simulados desde el repositorio (estado memorizado)
    val users = remember { UserRepository.getUsers() }
    // Índice del usuario actual mostrado en la tarjeta
    var currentUserIndex by remember { mutableStateOf(0) }

    // Estructura base de pantalla con barra superior, contenido y barra inferior
    Scaffold(
        topBar = {
            // Barra superior con título y acciones (filtros y perfil)
            TopAppBar(
                title = { Text("HirMi") },
                actions = {
                    IconButton(onClick = { /* Filtros */ }) {
                        Icon(Icons.Filled.Tune, contentDescription = "Filtros")
                    }
                    IconButton(onClick = { /* Perfil */ }) {
                        Icon(Icons.Filled.Person, contentDescription = "Perfil")
                    }
                }
            )
        },
        bottomBar = {
            // Barra de navegación inferior con tres secciones
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { /* Navegar a Inicio */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.MusicNote, contentDescription = "Música") },
                    label = { Text("Música") },
                    selected = false,
                    onClick = { /* Navegar a Música */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.People, contentDescription = "Comunidad") },
                    label = { Text("Comunidad") },
                    selected = false,
                    onClick = { /* Navegar a Comunidad */ }
                )
            }
        }
    ) { paddingValues ->
        // Contenedor principal que respeta los paddings del Scaffold
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Si hay usuarios por mostrar, pinta la tarjeta actual; si no, muestra vacío
            if (currentUserIndex < users.size) {
                UserCard(
                    user = users[currentUserIndex],
                    onSwipeLeft = {
                        // Avanza al siguiente usuario si hay más
                        if (currentUserIndex < users.size - 1) currentUserIndex++
                    },
                    onSwipeRight = {
                        // Avanza al siguiente usuario si hay más
                        if (currentUserIndex < users.size - 1) currentUserIndex++
                    }
                )
            } else {
                NoMoreUsersCard()
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onSwipeLeft: () -> Unit,   // Acción al descartar
    onSwipeRight: () -> Unit   // Acción al interesar
) {
    // Offset actual de arrastre de la tarjeta (X/Y) para animar el desplazamiento
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Tarjeta principal del usuario, con gesto de arrastre tipo "swipe"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
            // Aplica el desplazamiento actual en pixeles enteros
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            // Captura gestos de arrastre para mover y decidir swipe
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Umbrales simples para considerar un swipe a izquierda/derecha
                        when {
                            offsetX > 300 -> onSwipeRight()
                            offsetX < -300 -> onSwipeLeft()
                        }
                        // Resetea posición al soltar
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // Contenido vertical de la tarjeta
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Encabezado con "avatar" de iniciales y nombres
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar circular con iniciales del usuario
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${user.firstName.first()}${user.lastName.first()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Nombre completo y username
                Column {
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información adicional del usuario
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Distancia del usuario
                InfoRow(
                    icon = Icons.Filled.LocationOn,
                    text = "${user.distance} metros de distancia"
                )

                Divider()

                // Sección de canción actual
                Text(
                    text = "Escuchando ahora:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Tarjeta secundaria para los datos de la canción
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Título y artista con icono musical
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = user.song?.title ?: "Sin canción",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = user.song?.artist ?: "Artista desconocido",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Álbum y género alineados a los extremos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Álbum: ${user.song?.album ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = user.song?.genre ?: "N/A",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Botones de acción flotantes (rechazar / me interesa)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FloatingActionButton(
                    onClick = onSwipeLeft,
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "No me interesa",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                FloatingActionButton(
                    onClick = onSwipeRight,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Me interesa",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// Fila reutilizable para mostrar un icono y un texto (p. ej., distancia)
@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Tarjeta que se muestra cuando no quedan usuarios por descubrir
@Composable
fun NoMoreUsersCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "¡No hay más usuarios!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Vuelve más tarde para descubrir nueva gente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Previsualizacióne
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}
