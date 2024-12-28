package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val list = arrayListOf<TodoModel>()
    private val adapter = TodoAdapter(list) { todoModel ->
        val intent = Intent(this, UpdateTaskActivity::class.java)
        intent.putExtra("TASK_ID", todoModel.id)
        startActivity(intent)
    }

    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set back navigation to HomeActivity
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish() // Close MainActivity
        }

        val todoRv: RecyclerView = findViewById(R.id.todoRv)
        todoRv.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        val fabAddTask: FloatingActionButton = findViewById(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            openNewTask()
        }

        initSwipe()

        db.todoDao().getTasks().observe(this, Observer { taskList ->
            list.clear()
            if (!taskList.isNullOrEmpty()) {
                list.addAll(taskList)
            }
            adapter.notifyDataSetChanged()
        })
    }

    private fun initSwipe() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val todo = list[position]

                // Use a coroutine to delete the task on a background thread
                CoroutineScope(Dispatchers.IO).launch {
                    db.todoDao().deleteTask(todo.id)

                    // Update the UI on the main thread after the deletion
                    withContext(Dispatchers.Main) {
                        list.removeAt(position)
                        adapter.notifyItemRemoved(position)
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(findViewById(R.id.todoRv))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                openNewTask()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openNewTask() {
        startActivity(Intent(this, TaskActivity::class.java))
    }
}
