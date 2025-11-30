package com.anujsinghdev.anujtodo.ui.list_detail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anujsinghdev.anujtodo.domain.model.RepeatMode
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.ui.components.ElasticSwipeToDismiss
import com.anujsinghdev.anujtodo.ui.list_detail.components.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Constants - Dark Theme Colors
val PinkAccent = Color(0xFF0091EA)
val BackgroundColor = Color.Black
val SurfaceColor = Color(0xFF1E1E1E)
val TextSecondary = Color.Gray

// Dark Popup Colors
val DarkPopupBg = Color(0xFF1A1A1A)
val DarkerGrey = Color(0xFF0F0F0F)
val LightGrey = Color(0xFF404040)
val TextWhite = Color.White
val TextGrey = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    navController: NavController,
    listId: Long,
    listName: String,
    viewModel: ListDetailViewModel = hiltViewModel()
) {
    val tasks by viewModel.getTasksForList(listId).collectAsState(initial = emptyList())
    val realListName by viewModel.getListNameFlow(listId, listName).collectAsState(initial = listName)
    val activeTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }

    var showAddTaskSheet by remember { mutableStateOf(false) }
    var selectedTaskToEdit by remember { mutableStateOf<TodoItem?>(null) }
    var isCompletedExpanded by remember { mutableStateOf(true) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val currentSortOption by viewModel.currentSortOption.collectAsState()

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            ListDetailTopBar(
                listName = realListName,
                listId = listId,
                onBackClick = { navController.popBackStack() },
                onRenameClick = { showRenameDialog = true },
                onSortClick = { showSortDialog = true },
                onDuplicateClick = { viewModel.duplicateList(listId, realListName) },
                onArchiveClick = {
                    viewModel.archiveList(listId)
                    navController.popBackStack()
                },
                onDeleteClick = { showDeleteDialog = true }
            )
        },
        floatingActionButton = {
            if (listId != SMART_LIST_COMPLETED_ID) {
                FloatingActionButton(
                    onClick = { showAddTaskSheet = true },
                    containerColor = PinkAccent,
                    shape = CircleShape,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Add Task")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (listId == SMART_LIST_COMPLETED_ID) {
                val groupedTasks by viewModel.groupedCompletedTasks.collectAsState(initial = emptyMap())
                if (groupedTasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyStateView("Completed")
                    }
                } else {
                    GroupedCompletedList(
                        groupedTasks = groupedTasks,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onEditTask = { selectedTaskToEdit = it }
                    )
                }
            } else {
                if (tasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyStateView(realListName)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().rubberBandEffect().padding(horizontal = 16.dp)
                    ) {
                        items(items = activeTasks, key = { it.id }) { task ->
                            Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)) {
                                ElasticSwipeToDismiss(
                                    onDelete = { viewModel.deleteTask(task) },
                                    onComplete = { viewModel.toggleTask(task) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    TaskItemView(
                                        todo = task,
                                        onToggle = { viewModel.toggleTask(task) },
                                        onClick = { selectedTaskToEdit = task }
                                    )
                                }
                            }
                        }
                        if (completedTasks.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                CompletedHeader(
                                    title = "Completed",
                                    count = completedTasks.size,
                                    isExpanded = isCompletedExpanded,
                                    onClick = { isCompletedExpanded = !isCompletedExpanded }
                                )
                            }
                            if (isCompletedExpanded) {
                                items(items = completedTasks, key = { it.id }) { task ->
                                    Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)) {
                                        ElasticSwipeToDismiss(
                                            onDelete = { viewModel.deleteTask(task) },
                                            onComplete = { viewModel.toggleTask(task) },
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            TaskItemView(
                                                todo = task,
                                                onToggle = { viewModel.toggleTask(task) },
                                                onClick = { selectedTaskToEdit = task }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // --- DIALOGS & SHEETS ---
    if (showRenameDialog) {
        ListNameDialog(
            title = "Rename list",
            initialName = realListName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { viewModel.renameList(listId, it); showRenameDialog = false }
        )
    }
    if (showSortDialog) {
        SortDialog(
            currentSortOption = currentSortOption,
            onSortSelected = { viewModel.updateSortOption(it); showSortDialog = false },
            onDismiss = { showSortDialog = false }
        )
    }
    if (showDeleteDialog) {
        DeleteListDialog(
            listName = realListName,
            onConfirm = { viewModel.deleteList(listId) { navController.popBackStack() } },
            onDismiss = { showDeleteDialog = false }
        )
    }
    if (showAddTaskSheet) {
        TaskInputBottomSheet(
            title = "Add Task",
            initialText = "",
            initialDate = null,
            initialRepeat = RepeatMode.NONE,
            onDismiss = { showAddTaskSheet = false },
            onSave = { title, date, repeat ->
                viewModel.addTask(title, date, repeat, listId)
                showAddTaskSheet = false
            }
        )
    }
    selectedTaskToEdit?.let { task ->
        TaskInputBottomSheet(
            title = "Edit Task",
            initialText = task.title,
            initialDate = task.dueDate,
            initialRepeat = task.repeatMode,
            onDismiss = { selectedTaskToEdit = null },
            onSave = { title, date, repeat ->
                viewModel.updateTask(task.copy(title = title, dueDate = date, repeatMode = repeat))
                selectedTaskToEdit = null
            }
        )
    }
}

// Utility & Helper Composables that were too small to split further

@Composable
fun GroupedCompletedList(
    groupedTasks: Map<String, List<TodoItem>>,
    onToggleTask: (TodoItem) -> Unit,
    onEditTask: (TodoItem) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().rubberBandEffect().padding(horizontal = 16.dp)) {
        groupedTasks.forEach { (listName, tasks) ->
            item {
                var isExpanded by remember { mutableStateOf(true) }
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    CompletedHeader(title = listName, count = tasks.size, isExpanded = isExpanded, onClick = { isExpanded = !isExpanded })
                    if (isExpanded) {
                        tasks.forEach { task ->
                            TaskItemView(
                                todo = task,
                                onToggle = { onToggleTask(task) },
                                onClick = { onEditTask(task) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun CompletedHeader(title: String, count: Int, isExpanded: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "Arrow")
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.KeyboardArrowRight, null, tint = TextSecondary, modifier = Modifier.rotate(rotation).size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$title $count", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EmptyStateView(listName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No tasks in $listName", color = Color.Gray)
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun Modifier.rubberBandEffect(): Modifier = composed {
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (offsetY.value != 0f) {
                    val newOffset = offsetY.value + delta
                    if ((offsetY.value > 0 && newOffset < 0) || (offsetY.value < 0 && newOffset > 0)) {
                        scope.launch { offsetY.snapTo(0f) }
                        return Offset(0f, -offsetY.value)
                    } else {
                        scope.launch { offsetY.snapTo(newOffset) }
                        return Offset(0f, delta)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag && available.y != 0f) {
                    scope.launch { offsetY.snapTo(offsetY.value + available.y * 0.3f) }
                    return available
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                return super.onPostFling(consumed, available)
            }
        }
    }
    this.nestedScroll(connection).graphicsLayer { translationY = offsetY.value }
}
