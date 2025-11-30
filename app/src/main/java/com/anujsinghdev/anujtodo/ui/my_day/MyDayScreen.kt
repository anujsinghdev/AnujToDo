package com.anujsinghdev.anujtodo.ui.my_day

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anujsinghdev.anujtodo.domain.model.RepeatMode
import com.anujsinghdev.anujtodo.ui.components.ElasticSwipeToDismiss
import com.anujsinghdev.anujtodo.ui.list_detail.components.TaskInputBottomSheet
import com.anujsinghdev.anujtodo.ui.list_detail.components.TaskItemView
import com.anujsinghdev.anujtodo.ui.todo_list.LoginBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyDayScreen(
    navController: NavController,
    viewModel: MyDayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentDate = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()) }

    // --- State for Add Task Sheet ---
    var showAddTaskSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Black,
        // FIX: Set contentWindowInsets to 0.dp to remove default system bar padding
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Adjusted padding: ensure it doesn't conflict with system bars if edge-to-edge is active
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskSheet = true },
                containerColor = LoginBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BackgroundLines()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "My Day",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = currentDate,
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.overdueTasks.isNotEmpty()) {
                        item {
                            Text(
                                "Overdue",
                                color = Color.Red.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(items = state.overdueTasks, key = { it.id }) { task ->
                            ElasticSwipeToDismiss(
                                onDelete = { viewModel.deleteTask(task) },
                                onComplete = { viewModel.toggleTask(task) },
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                TaskItemView(
                                    todo = task,
                                    onToggle = { viewModel.toggleTask(task) },
                                    onClick = {}
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    if (state.todayTasks.isNotEmpty()) {
                        items(items = state.todayTasks, key = { it.id }) { task ->
                            ElasticSwipeToDismiss(
                                onDelete = { viewModel.deleteTask(task) },
                                onComplete = { viewModel.toggleTask(task) },
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                TaskItemView(
                                    todo = task,
                                    onToggle = { viewModel.toggleTask(task) },
                                    onClick = {}
                                )
                            }
                        }
                    } else if (state.overdueTasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(bottom = 100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No tasks for today", color = Color.Gray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // --- SHOW BOTTOM SHEET ---
    if (showAddTaskSheet) {
        TaskInputBottomSheet(
            title = "Add to My Day",
            initialText = "",
            initialDate = System.currentTimeMillis(),
            initialRepeat = RepeatMode.NONE,
            onDismiss = { showAddTaskSheet = false },
            onSave = { title, date, repeat ->
                viewModel.addTask(title, date, repeat)
                showAddTaskSheet = false
            }
        )
    }
}

@Composable
fun BackgroundLines() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineHeight = 60.dp.toPx()
        val lineCount = (size.height / lineHeight).toInt()

        for (i in 1..lineCount) {
            val y = i * lineHeight
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
