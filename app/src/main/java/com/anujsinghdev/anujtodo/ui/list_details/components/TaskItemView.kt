package com.anujsinghdev.anujtodo.ui.list_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anujsinghdev.anujtodo.domain.model.RepeatMode
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.ui.list_detail.PinkAccent
import com.anujsinghdev.anujtodo.ui.list_detail.formatDate

@Composable
fun TaskItemView(
    todo: TodoItem,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val textColor = if (todo.isCompleted) Color.Gray else Color.White
    val textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF252525))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
            Icon(
                if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                "Toggle",
                tint = if (todo.isCompleted) PinkAccent else Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = todo.title,
                color = textColor,
                fontSize = 16.sp,
                textDecoration = textDecoration
            )
            if (todo.dueDate != null || todo.repeatMode != RepeatMode.NONE) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (todo.dueDate != null) {
                        Text(
                            formatDate(todo.dueDate),
                            color = if (todo.dueDate < System.currentTimeMillis() && !todo.isCompleted) Color.Red else Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    if (todo.repeatMode != RepeatMode.NONE) {
                        if (todo.dueDate != null) Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Repeat,
                            null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
