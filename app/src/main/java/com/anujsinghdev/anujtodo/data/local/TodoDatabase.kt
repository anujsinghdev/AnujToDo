package com.anujsinghdev.anujtodo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.model.TodoFolder
import com.anujsinghdev.anujtodo.domain.model.TodoList
import com.anujsinghdev.anujtodo.domain.model.FocusSession // Import the new Entity

// 1. Create a converter for the Enum
class Converters {
    @androidx.room.TypeConverter
    fun fromRepeatMode(value: com.anujsinghdev.anujtodo.domain.model.RepeatMode) = value.name
    @androidx.room.TypeConverter
    fun toRepeatMode(value: String) = com.anujsinghdev.anujtodo.domain.model.RepeatMode.valueOf(value)

    @androidx.room.TypeConverter
    fun fromSessionStatus(value: com.anujsinghdev.anujtodo.domain.model.SessionStatus) = value.name
    @androidx.room.TypeConverter
    fun toSessionStatus(value: String) = com.anujsinghdev.anujtodo.domain.model.SessionStatus.valueOf(value)
}

// 2. Add FocusSession to entities and bump version to 6
@Database(
    entities = [
        TodoItem::class,
        TodoFolder::class,
        TodoList::class,
        FocusSession::class // <--- Added new entity
    ],
    version = 6, // <--- Bumped version
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}