package com.example.todoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class TodoModel(
    val title: String,
    val description: String,
    val category: String,
    val date: Long,  // Timestamp in milliseconds
    val time: Long,  // Timestamp in milliseconds
    val isFinished: Int = 0,  // Use Int instead of Boolean

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)
