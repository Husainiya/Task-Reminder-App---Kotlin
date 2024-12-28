package com.example.todoapp

import com.example.todoapp.TodoModel
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.databinding.ActivityUpdateTaskBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class UpdateTaskActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityUpdateTaskBinding
    private lateinit var myCalendar: Calendar
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener

    private var finalDate = 0L
    private var finalTime = 0L

    private val labels = arrayListOf("Personal", "Business", "Insurance", "Shopping", "Banking")

    // Initialize the database reference
    private val db by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = ActivityUpdateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up UI components
        binding.dateEdt.setOnClickListener(this)
        binding.timeEdt.setOnClickListener(this)
        binding.updateBtn.setOnClickListener(this)

        setUpSpinner()

        // Load task details
        loadTask()
    }

    private fun setUpSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        labels.sort()
        binding.categorySpinner.adapter = adapter
    }

    private fun loadTask() {
        val taskId = intent.getLongExtra("TASK_ID", -1L)
        if (taskId == -1L) {
            Log.e("UpdateTaskActivity", "No task ID provided")
            finish()  // Exit activity if no task ID is provided
            return
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val task = db.todoDao().getTaskById(taskId)  // Fetch task details from the database
                    withContext(Dispatchers.Main) {
                        binding.titleEdt.setText(task.title)
                        binding.descriptionEdt.setText(task.description)
                        binding.categorySpinner.setSelection(labels.indexOf(task.category))
                        finalDate = task.date
                        finalTime = task.time
                        binding.dateEdt.setText(formatDate(finalDate))  // Correct usage
                        binding.timeEdt.setText(formatTime(finalTime))  // Correct usage
                    }
                } catch (e: Exception) {
                    Log.e("UpdateTaskActivity", "Error loading task", e)
                }
            }
        }
    }

    private fun formatDate(time: Long): String {  // Expect Long for time
        val myFormat = "EEE, d MMM yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        return sdf.format(Date(time))  // Convert Long to Date
    }

    private fun formatTime(time: Long): String {  // Expect Long for time
        val myFormat = "h:mm a"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        return sdf.format(Date(time))  // Convert Long to Date
    }

    override fun onClick(v: View) {
        when (v.id) {
            binding.dateEdt.id -> setDateListener()  // Updated function name
            binding.timeEdt.id -> setTimeListener()  // Updated function name
            binding.updateBtn.id -> updateTask()
        }
    }

    private fun updateTask() {
        val category = binding.categorySpinner.selectedItem.toString()
        val title = binding.titleEdt.text.toString()
        val description = binding.descriptionEdt.text.toString()

        Log.d("UpdateTaskActivity", "updateTask() called")
        Log.d("UpdateTaskActivity", "Category: $category")
        Log.d("UpdateTaskActivity", "Title: $title")
        Log.d("UpdateTaskActivity", "Description: $description")
        Log.d("UpdateTaskActivity", "Final Date: $finalDate")
        Log.d("UpdateTaskActivity", "Final Time: $finalTime")

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val taskId = intent.getLongExtra("TASK_ID", -1L)
                    if (taskId != -1L) {
                        db.todoDao().updateTask(
                            TodoModel(
                                title, description, category, finalDate, finalTime,
                                isFinished = 0, id = taskId
                            )
                        )
                        Log.d("UpdateTaskActivity", "Task successfully updated with ID: $taskId")
                    } else {
                        // Handle case where taskId is -1L
                        Log.e("UpdateTaskActivity", "Invalid task ID: $taskId")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@UpdateTaskActivity, "Error: Task not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UpdateTaskActivity", "Error updating task", e)
                }
            }
            // Return to TaskActivity after update or error
            finish()
        }
    }

    private fun setTimeListener() {  // Corrected function name and structure
        myCalendar = Calendar.getInstance()
        timeSetListener = TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, min: Int ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            myCalendar.set(Calendar.MINUTE, min)
            updateTime()
        }

        val timePickerDialog = TimePickerDialog(
            this, timeSetListener, myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE), false
        )
        timePickerDialog.show()
    }

    private fun updateTime() {  // Corrected function structure
        val myFormat = "h:mm a"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        finalTime = myCalendar.time.time
        binding.timeEdt.setText(sdf.format(myCalendar.time))
        Log.d("UpdateTaskActivity", "Updated Time: ${sdf.format(myCalendar.time)}")
    }

    private fun setDateListener() {  // Corrected function name and structure
        myCalendar = Calendar.getInstance()

        dateSetListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDate()
        }

        val datePickerDialog = DatePickerDialog(
            this, dateSetListener, myCalendar.get(Calendar.YEAR),
            myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDate() {  // Corrected function structure
        val myFormat = "EEE, d MMM yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        finalDate = myCalendar.time.time
        binding.dateEdt.setText(sdf.format(myCalendar.time))
        Log.d("UpdateTaskActivity", "Updated Date: ${sdf.format(myCalendar.time)}")
    }
}
