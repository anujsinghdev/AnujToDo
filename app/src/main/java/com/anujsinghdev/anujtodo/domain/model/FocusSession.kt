package com.anujsinghdev.anujtodo.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationMinutes: Int,      // How long the user focused
    val timestamp: Long,           // When the session ended
    val status: SessionStatus,     // Completed or Stopped
    val tag: String = "Untagged"   // Work, Study, etc.
)

enum class SessionStatus {
    COMPLETED, STOPPED
}