package com.example.todoapp

import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.databinding.ActivityTaskBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TaskActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityTaskBinding
    private lateinit var todoAdapter: TodoAdapter
    private val labels = arrayListOf("Personal", "Business", "Insurance", "Shopping", "Banking")

    private var finalDate: Long = 0L
    private var finalTime: Long = 0L
    private lateinit var myCalendar: Calendar
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener

    private var alarmToneUri: Uri? = null // To store the selected alarm tone URI

    private val db by lazy {
        AppDatabase.getDatabase(this)
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
        private const val SCHEDULE_EXACT_ALARM_PERMISSION_REQUEST_CODE = 101
        private const val ALARM_ACTION = "com.example.todoapp.ALARM_ACTION" // Define an action
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dateEdt.setOnClickListener(this)
        binding.timeEdt.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
        binding.alarmToneBtn?.setOnClickListener(this)

        setUpRecyclerView()
        loadTasks()
        setUpSpinner()

        checkNotificationPermission()
        checkExactAlarmPermission()
    }

    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SCHEDULE_EXACT_ALARM), SCHEDULE_EXACT_ALARM_PERMISSION_REQUEST_CODE)
            } else {
                Log.d("TaskActivity", "Exact alarm permission already granted.")
            }
        }
    }


    private fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No need for this permission on older versions
        }
    }

    private fun setUpRecyclerView() {
        binding.recyclerView?.layoutManager = LinearLayoutManager(this)
        todoAdapter = TodoAdapter(emptyList()) { todoModel ->
            val intent = Intent(this, UpdateTaskActivity::class.java)
            intent.putExtra("TASK_ID", todoModel.id)
            startActivity(intent)
        }
        binding.recyclerView?.adapter = todoAdapter
    }

    private fun setUpSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        labels.sort()
        binding.spinnerCategory.adapter = adapter
    }

    private fun loadTasks() {
        db.todoDao().getTasks().observe(this) { tasks ->
            todoAdapter.list = tasks
            todoAdapter.notifyDataSetChanged()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            binding.dateEdt.id -> setDateListener()
            binding.timeEdt.id -> setTimeListener()
            binding.saveBtn.id -> saveTodo()

        }
    }

    private fun saveTodo() {
        val category = binding.spinnerCategory.selectedItem.toString()
        val title = binding.titleInpLay.editText?.text.toString()
        val description = binding.taskInpLay.editText?.text.toString()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val task = TodoModel(
                    title = title,
                    description = description,
                    category = category,
                    date = finalDate,
                    time = finalTime
                )
                db.todoDao().insertTodo(task)

                if (hasExactAlarmPermission()) {
                    scheduleAlarm(task)
                } else {
                    Log.e("TaskActivity", "Cannot schedule alarm: exact alarm permission not granted.")
                }
                withContext(Dispatchers.Main) {
                    finish() // Close TaskActivity
                }
            } catch (e: Exception) {
                Log.e("TaskActivity", "Error saving task", e)
            }
        }
    }

    private fun scheduleAlarm(task: TodoModel) {
        // Ensure you check if permission is granted before scheduling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasExactAlarmPermission()) {
            Log.e("TaskActivity", "Exact alarm permission not granted")
            return // Don't schedule if permission is not granted
        }

        try {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(ALARM_ACTION).apply {
                putExtra("TITLE", task.title)
                putExtra("MESSAGE", task.description)
                putExtra("ALARM_TONE_URI", alarmToneUri.toString())
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                task.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmTime = task.date + task.time
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
        } catch (e: SecurityException) {
            Log.e("TaskActivity", "SecurityException while setting the alarm", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 999 && resultCode == RESULT_OK) {
            alarmToneUri = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            binding.alarmToneBtn?.text = alarmToneUri.toString()
        }
    }

    private fun setDateListener() {
        myCalendar = Calendar.getInstance()
        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
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

    private fun setTimeListener() {
        myCalendar = Calendar.getInstance()
        timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            myCalendar.set(Calendar.MINUTE, minute)
            updateTime()
        }

        val timePickerDialog = TimePickerDialog(
            this, timeSetListener, myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE), false
        )
        timePickerDialog.show()
    }

    private fun updateDate() {
        val myFormat = "EEE, d MMM yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        finalDate = myCalendar.time.time
        binding.dateEdt.setText(sdf.format(myCalendar.time))
        binding.timeInptLay.visibility = View.VISIBLE
        Log.d("TaskActivity", "Updated Date: ${sdf.format(myCalendar.time)}")
    }

    private fun updateTime() {
        val myFormat = "h:mm a"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        finalTime = myCalendar.time.time
        binding.timeEdt.setText(sdf.format(myCalendar.time))
        Log.d("TaskActivity", "Updated Time: ${sdf.format(myCalendar.time)}")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SCHEDULE_EXACT_ALARM_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TaskActivity", "Exact alarm permission granted.")
                    // You can also trigger scheduling the alarm here if needed
                } else {
                    Log.d("TaskActivity", "Exact alarm permission denied.")
                }
            }
        }
    }
}