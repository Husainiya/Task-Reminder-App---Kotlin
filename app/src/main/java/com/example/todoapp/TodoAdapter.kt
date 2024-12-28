package com.example.todoapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.ItemTodoBinding
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(
    var list: List<TodoModel>,
    private val onUpdateClickListener: (TodoModel) -> Unit // Click listener for the update button
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemId(position: Int): Long {
        return list[position].id
    }

    inner class TodoViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(todoModel: TodoModel) {
            with(binding) {
                // Set a random color for the viewColorTag
                val colors = root.context.resources.getIntArray(R.array.random_color)
                val randomColor = colors[Random().nextInt(colors.size)]
                viewColorTag.setBackgroundColor(randomColor)

                // Set text values for the task details
                txtShowTitle.text = todoModel.title
                txtShowTask.text = todoModel.description
                txtShowCategory.text = todoModel.category

                // Format and set time and date
                updateTime(todoModel.time)
                updateDate(todoModel.date) // Ensure this is a Long timestamp

                // Set up click listener for the "Update" button
                btnUpdate.setOnClickListener {
                    onUpdateClickListener(todoModel)
                }
            }
        }

        private fun updateDate(timestamp: Long) { // Change parameter to Long
            val myformat = "EEE, d MMM yyyy"
            val sdf = SimpleDateFormat(myformat, Locale.getDefault())
            binding.txtShowDate.text = sdf.format(Date(timestamp)) // Create Date with Long
        }

        private fun updateTime(timestamp: Long) {
            val myformat = "h:mm a"
            val sdf = SimpleDateFormat(myformat, Locale.getDefault())
            binding.txtShowTime.text = sdf.format(Date(timestamp))
        }
    }
}
