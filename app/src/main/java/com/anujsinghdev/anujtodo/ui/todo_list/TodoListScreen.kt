package com.anujsinghdev.anujtodo.ui.todo_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anujsinghdev.anujtodo.ui.components.AnujButton
import com.anujsinghdev.anujtodo.ui.components.BottomNavItem
import com.anujsinghdev.anujtodo.ui.components.GlassBottomNavigation
import com.anujsinghdev.anujtodo.ui.util.Screen

@Composable
fun TodoListScreen(
    navController: NavController,
    viewModel: TodoListViewModel = hiltViewModel()
) {
    val backgroundColor = Color.Black
    val scrollState = rememberScrollState()

    // Observe Search State
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState(initial = TodoListSearchResults())

    // --- State for Dialogs ---
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }

    // --- Navigation State ---
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    val navItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Focus", Icons.Filled.CenterFocusStrong, Icons.Outlined.CenterFocusStrong),
        BottomNavItem("Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    )

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            // Hide Bottom Bar when searching
            if (!isSearchActive) {
                Column {
                    // 1. Your existing "New List/Folder" Action Bar
                    BottomBarAction(
                        onNewListClick = { showCreateListDialog = true },
                        onNewGroupClick = { showCreateGroupDialog = true }
                    )

                    // 2. The New Glassmorphic Navigation
                    GlassBottomNavigation(
                        items = navItems,
                        selectedItem = selectedNavIndex,
                        onItemClick = { index ->
                            selectedNavIndex = index
                            // Handle Navigation Here
                            when(index) {
                                0 -> { /* Already Home */ }
                                1 -> { /* Navigate to Focus Screen */ }
                                2 -> { /* Navigate to Stats Screen */ }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .then(if(!isSearchActive) Modifier.verticalScroll(scrollState) else Modifier)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ProfileHeader(
                name = viewModel.userName.value,
                email = viewModel.userEmail.value,
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                isSearchActive = isSearchActive,
                onSearchActiveChange = viewModel::onSearchActiveChange
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isSearchActive) {
                SearchResultsList(
                    lists = searchResults.lists,
                    tasks = searchResults.tasks,
                    onListClick = { list ->
                        navController.navigate(Screen.ListDetailScreen.createRoute(list.id, list.name))
                    },
                    onTaskClick = { task ->
                        val targetListId = task.listId ?: -1L
                        navController.navigate(Screen.ListDetailScreen.createRoute(targetListId, "Task List"))
                    }
                )
            } else {
                // --- NORMAL DASHBOARD VIEW ---

                // Menu Items Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        VerticalMenuItem(Icons.Outlined.WbSunny, "My Day", Color.Gray) {}
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp), color = Zinc700, thickness = 1.dp)
                    Box(modifier = Modifier.weight(1f)) {
                        VerticalMenuItem(
                            Icons.Outlined.CheckCircle, "Completed", Color.Gray,
                            onClick = { navController.navigate(Screen.ListDetailScreen.createRoute(-2L, "Completed")) }
                        )
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp), color = Zinc700, thickness = 1.dp)
                    Box(modifier = Modifier.weight(1f)) {
                        VerticalMenuItem(Icons.Outlined.Archive, "Archive", Color(0xFFF06292)) {}
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // User Lists
                viewModel.rootLists.forEach { list ->
                    UserListItem(
                        list = list,
                        onClick = { navController.navigate(Screen.ListDetailScreen.createRoute(list.id, list.name)) }
                    )
                }

                // Folders
                viewModel.folders.forEach { folder ->
                    FolderView(
                        folder = folder,
                        onToggleExpand = { viewModel.toggleFolderExpanded(folder.id) },
                        onAddListToFolder = { name -> viewModel.addListToFolder(folder.id, name) },
                        onListClick = { list -> navController.navigate(Screen.ListDetailScreen.createRoute(list.id, list.name)) }
                    )
                }

                // Add extra spacer at bottom so content isn't hidden behind the new taller bottom bar
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    // --- Dialogs ---
    if (showCreateGroupDialog) {
        CreateDialog("Create a group", "Name this group", { showCreateGroupDialog = false }, { viewModel.createFolder(it); showCreateGroupDialog = false })
    }
    if (showCreateListDialog) {
        CreateDialog("Create a list", "Name this list", { showCreateListDialog = false }, { viewModel.createList(it); showCreateListDialog = false })
    }
}