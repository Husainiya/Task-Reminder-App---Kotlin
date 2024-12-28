package com.example.todoapp

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_table")
    fun getTasks(): LiveData<List<TodoModel>>

    @Query("SELECT * FROM todo_table WHERE id = :todoId")
    fun getTaskById(todoId: Long): TodoModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTodo(todo: TodoModel)

    @Update
    fun updateTask(todo: TodoModel)

    @Query("DELETE FROM todo_table WHERE id = :todoId")
    fun deleteTask(todoId: Long)

    @Query("UPDATE todo_table SET isFinished = 1 WHERE id = :todoId")
    fun finishTask(todoId: Long)

    @Query("SELECT * FROM todo_table WHERE title LIKE '%' || :query || '%'")
    fun searchTasks(query: String): LiveData<List<TodoModel>>

    @Query("SELECT * FROM todo_table")
    fun getAllTasks(): LiveData<List<TodoModel>>
}
