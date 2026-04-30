package com.example.todolist

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.todolist.viewmodel.TodoViewModel
import kotlinx.coroutines.launch

sealed class BottomItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomItem("home", "Home", Icons.Default.Home)
    object Settings : BottomItem("settings", "Settings", Icons.Default.Settings)
    object Contact : BottomItem("contact", "Contact", Icons.Default.ContactPhone)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("ToDoList", modifier = Modifier.padding(16.dp))

                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("home")
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("settings")
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Contact") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("contact")
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ToDoList") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavigationBar(navController)
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("home") { HomeScreen() }
                composable("settings") { SettingsScreen() }
                composable("contact") { ContactScreen() }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomItem.Home,
        BottomItem.Settings,
        BottomItem.Contact
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) }
            )
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val viewModel = remember { TodoViewModel() }
    var text by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadTodos(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter task") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    viewModel.addTodo(text, context)
                    text = ""
                }) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Tasks", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (viewModel.todoList.isEmpty()) {
            item {
                Text("No tasks yet 👀")
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            items(viewModel.todoList) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = item.isDone,
                                onCheckedChange = {
                                    viewModel.toggleDone(item, context)
                                }
                            )

                            Text(
                                text = item.title,
                                modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                                textDecoration = if (item.isDone) {
                                    TextDecoration.LineThrough
                                } else {
                                    TextDecoration.None
                                }
                            )
                        }

                        Button(onClick = {
                            viewModel.removeTodo(item, context)
                        }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            Text("REST API Quote", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.fetchQuote()
                }
            ) {
                Text("Pobierz / Odśwież")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator()
            }

            if (viewModel.errorMessage.isNotBlank()) {
                Text(viewModel.errorMessage)
            }

            if (viewModel.quoteText.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("\"${viewModel.quoteText}\"")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("- ${viewModel.quoteAuthor}")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Application: ToDoList")
        Text("Technology: Kotlin + Jetpack Compose")
        Text("Navigation: Bottom Navigation + Drawer")
        Text("Local data: SharedPreferences persistence")
        Text("REST API: Retrofit + ViewModel")
        Text("Maps: Google Maps + location permission")
    }
}

@Composable
fun ContactScreen() {
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasLocationPermission = granted
        }

    val warsaw = com.google.android.gms.maps.model.LatLng(52.2297, 21.0122)

    val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(warsaw, 12f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        com.google.maps.android.compose.GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            cameraPositionState = cameraPositionState,
            properties = com.google.maps.android.compose.MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = com.google.maps.android.compose.MapUiSettings(
                myLocationButtonEnabled = hasLocationPermission,
                zoomControlsEnabled = true
            )
        ) {
            com.google.maps.android.compose.Marker(
                state = com.google.maps.android.compose.MarkerState(position = warsaw),
                title = "University",
                snippet = "Example location"
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {
                if (!hasLocationPermission) {
                    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        ) {
            Text(
                if (hasLocationPermission) {
                    "Location permission granted"
                } else {
                    "Request location permission"
                }
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:52.2297,21.0122?q=52.2297,21.0122,University")
                )

                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Brak aplikacji map
                }
            }
        ) {
            Text("Open in Maps App")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}