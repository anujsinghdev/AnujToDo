package com.anujsinghdev.anujtodo.ui.list_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.anujsinghdev.anujtodo.ui.components.AnimatedDialog
import com.anujsinghdev.anujtodo.ui.list_detail.ListDetailViewModel
import com.anujsinghdev.anujtodo.ui.list_detail.PinkAccent
import com.anujsinghdev.anujtodo.ui.list_detail.SortOption
import com.anujsinghdev.anujtodo.ui.todo_list.DialogBg

@Composable
fun ListNameDialog(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialName) }
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = DialogBg), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PinkAccent,
                        focusedIndicatorColor = PinkAccent,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
                    TextButton(
                        onClick = { if (text.isNotBlank()) onConfirm(text) },
                        enabled = text.isNotBlank()
                    ) {
                        Text("SAVE", color = if (text.isNotBlank()) PinkAccent else Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SortDialog(
    currentSortOption: SortOption,
    onSortSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text("Sort by", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                val options = listOf(
                    SortOption.IMPORTANCE to "Importance",
                    SortOption.DUE_DATE to "Due date",
                    SortOption.ALPHABETICAL to "Alphabetically",
                    SortOption.CREATION_DATE to "Creation date"
                )
                options.forEach { (option, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(option) }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (option) {
                                SortOption.IMPORTANCE -> Icons.Default.StarBorder
                                SortOption.DUE_DATE -> Icons.Default.CalendarMonth
                                SortOption.ALPHABETICAL -> Icons.AutoMirrored.Outlined.Sort
                                else -> Icons.Default.Add
                            },
                            contentDescription = null,
                            tint = if (option == currentSortOption) PinkAccent else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = label, color = if (option == currentSortOption) PinkAccent else Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        if (option == currentSortOption) {
                            Icon(Icons.Default.CheckCircle, null, tint = PinkAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteListDialog(
    listName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedDialog(onDismissRequest = onDismiss) { triggerDismiss ->
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.width(320.dp).padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFF3E1E1E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Delete, "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Delete List?", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Are you sure you want to delete \"$listName\"? This action cannot be undone.", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { triggerDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("Cancel", fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = { triggerDismiss(); onConfirm() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("Delete", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
