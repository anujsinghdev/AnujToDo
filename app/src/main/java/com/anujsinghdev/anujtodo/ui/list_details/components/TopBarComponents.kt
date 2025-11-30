package com.anujsinghdev.anujtodo.ui.list_detail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anujsinghdev.anujtodo.ui.list_detail.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailTopBar(
    listName: String,
    listId: Long,
    onBackClick: () -> Unit,
    onRenameClick: () -> Unit,
    onSortClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(listName, color = PinkAccent, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
        // FIX: Set windowInsets to 0.dp to remove default status bar padding
        windowInsets = WindowInsets(0.dp),
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PinkAccent)
            }
        },
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, "Menu", tint = PinkAccent)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.width(220.dp).background(DarkPopupBg, RoundedCornerShape(12.dp)),
                containerColor = DarkPopupBg,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, LightGrey),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (listId != SMART_LIST_COMPLETED_ID) {
                    DarkMenuItem(Icons.Outlined.Edit, "Rename list") { showMenu = false; onRenameClick() }
                    HorizontalDivider(color = LightGrey.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                }
                DarkMenuItem(Icons.AutoMirrored.Outlined.Sort, "Sort by") { showMenu = false; onSortClick() }
                if (listId != SMART_LIST_COMPLETED_ID) {
                    DarkMenuItem(Icons.Outlined.FileCopy, "Duplicate list") { showMenu = false; onDuplicateClick() }
                    DarkMenuItem(Icons.Outlined.Archive, "Archive list") { showMenu = false; onArchiveClick() }
                    HorizontalDivider(color = LightGrey.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                    DarkMenuItem(Icons.Outlined.Delete, "Delete list", textColor = Color(0xFFFF5252), iconTint = Color(0xFFFF5252)) { showMenu = false; onDeleteClick() }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
    )
}

@Composable
fun DarkMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    textColor: Color = TextWhite,
    iconTint: Color = TextGrey,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                Text(text, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Normal)
            }
        },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}
