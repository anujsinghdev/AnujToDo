package com.anujsinghdev.anujtodo.ui.list_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anujsinghdev.anujtodo.domain.model.RepeatMode
import com.anujsinghdev.anujtodo.ui.list_detail.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskInputBottomSheet(
    title: String,
    initialText: String,
    initialDate: Long?,
    initialRepeat: RepeatMode,
    onDismiss: () -> Unit,
    onSave: (String, Long?, RepeatMode) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf(initialText) }
    var date by remember { mutableStateOf(initialDate) }
    var repeat by remember { mutableStateOf(initialRepeat) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showRepeatMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkPopupBg,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = LightGrey
            ) {}
        },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .imePadding()
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkerGrey,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, LightGrey.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("What needs to be done?", color = TextGrey) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = PinkAccent,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AnimatedVisibility(
                        visible = text.isNotBlank(),
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Surface(
                            onClick = { if (text.isNotBlank()) onSave(text, date, repeat) },
                            shape = CircleShape,
                            color = PinkAccent,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DarkActionChip(
                    icon = Icons.Default.CalendarMonth,
                    label = if (date != null) formatDate(date!!) else "Due date",
                    isActive = date != null,
                    onClick = { showDatePicker = true }
                )
                Box {
                    DarkActionChip(
                        icon = Icons.Default.Repeat,
                        label = if (repeat != RepeatMode.NONE)
                            repeat.name.lowercase().replaceFirstChar { it.uppercase() }
                        else "Repeat",
                        isActive = repeat != RepeatMode.NONE,
                        onClick = { showRepeatMenu = true }
                    )
                    DropdownMenu(
                        expanded = showRepeatMenu,
                        onDismissRequest = { showRepeatMenu = false },
                        modifier = Modifier.background(DarkPopupBg, RoundedCornerShape(12.dp)),
                        containerColor = DarkPopupBg,
                        border = BorderStroke(1.dp, LightGrey)
                    ) {
                        RepeatMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }, color = TextWhite) },
                                onClick = {
                                    repeat = mode
                                    showRepeatMenu = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    date = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = PinkAccent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = TextGrey) }
            },
            colors = DatePickerDefaults.colors(containerColor = DarkPopupBg),
            shape = RoundedCornerShape(20.dp)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = DarkPopupBg,
                    titleContentColor = TextWhite,
                    headlineContentColor = TextWhite,
                    weekdayContentColor = TextGrey,
                    dayContentColor = TextWhite,
                    selectedDayContainerColor = PinkAccent,
                    todayDateBorderColor = PinkAccent,
                    yearContentColor = TextWhite,
                    currentYearContentColor = TextWhite,
                    selectedYearContainerColor = PinkAccent
                )
            )
        }
    }
}

@Composable
fun DarkActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) LightGrey else DarkerGrey,
        border = if (isActive) BorderStroke(1.dp, TextGrey) else BorderStroke(1.dp, LightGrey.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                null,
                tint = if (isActive) TextWhite else TextGrey,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = if (isActive) TextWhite else TextGrey,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
