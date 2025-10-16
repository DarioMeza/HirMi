package cl.example.hirmi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.example.hirmi.model.User
import cl.example.hirmi.repository.UserRepository

// Pantalla principal de la app
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController,modifier: Modifier = Modifier ) {
    // Obtenemos los usuarios desde el repositorio simulado
    val users = remember { UserRepository.getUsers() }

    Scaffold(
        // Barra superior
        topBar = {
            TopAppBar(
                title = { Text("HirMi") }, // Título de la app
                actions = {
                    // Icono de filtros
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Tune, contentDescription = "Filtros")
                    }
                    // Icono de perfil
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Person, contentDescription = "Perfil")
                    }
                }
            )
        },
        // Barra inferior de navegación
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.MusicNote, contentDescription = "Música") },
                    label = { Text("Música") },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.People, contentDescription = "Comunidad") },
                    label = { Text("Comunidad") },
                    selected = false,
                    onClick = { }
                )
            }
        }
    ) { paddingValues ->
        // Contenedor principal que respeta el padding del Scaffold
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp), // Margen interno
            verticalArrangement = Arrangement.spacedBy(12.dp) // Espacio entre tarjetas
        ) {
            // Recorremos todos los usuarios y creamos una tarjeta para cada uno
            items(users.size) { index ->
                UserCard(user = users[index])
            }
        }
    }
}

// Composable que representa cada tarjeta de usuario
@Composable
fun UserCard(user: User) {
    // Variable que controla si la tarjeta está expandida o no
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth() // Ocupa todo el ancho disponible
            .clickable { expanded = !expanded }, // Al hacer click se expande/colapsa
        shape = RoundedCornerShape(12.dp) // Bordes redondeados
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior: avatar + nombre + username
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar circular con iniciales del usuario
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray), // Fondo gris claro (puede ser imagen)
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${user.firstName.first()}${user.lastName.first()}", // Iniciales
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp)) // Separación entre avatar y texto

                // Nombre y username
                Column {
                    Text("${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold)
                    Text("@${user.username}", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información básica
            Text("Canción: ${user.song?.title ?: "Sin canción"}",
                fontWeight = FontWeight.SemiBold)

            Text("Artista: ${user.song?.artist ?: "Sin artista"}",
                fontWeight = FontWeight.SemiBold)

            Text("Distancia: ${user.distance} metros")


            // Información adicional que se muestra al expandir
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider() // Línea separadora
                Text("Álbum: ${user.song?.album ?: "N/A"}")
                Text("Género: ${user.song?.genre ?: "N/A"}")

                Spacer(modifier = Modifier.height(8.dp))
                // Botones de acción
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { }) { Text("Seguir") }
                    Button(onClick = { }) { Text("Mensaje") }
                }
            }
        }
    }
}


