package com.it342.standupsync.adapter

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.it342.standupsync.R
import com.it342.standupsync.model.Task

class TaskAdapter(
    private val items: MutableList<Task> = mutableListOf(),
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskCardRoot: LinearLayout = view.findViewById(R.id.taskCardRoot)
        val tvTaskTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvTaskDescription: TextView = view.findViewById(R.id.tvTaskDescription)
        val btnDeleteTask: ImageButton = view.findViewById(R.id.btnDeleteTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = items[position]
        holder.tvTaskTitle.text = task.title
        holder.tvTaskDescription.text = task.description ?: ""
        holder.tvTaskDescription.visibility = if (task.description.isNullOrBlank()) View.GONE else View.VISIBLE

        // Set background based on status
        if (task.status == "blocker") {
            holder.taskCardRoot.setBackgroundResource(R.drawable.task_card_blocker_background)
        } else {
            holder.taskCardRoot.setBackgroundResource(R.drawable.task_card_background)
        }

        // Tap to edit
        holder.itemView.setOnClickListener { onEdit(task) }
        holder.btnDeleteTask.setOnClickListener { onDelete(task) }

        // Long press to start drag
        holder.itemView.setOnLongClickListener { view ->
            val clipData = ClipData.newPlainText("task_id", task.id.toString())
            val shadow = View.DragShadowBuilder(view)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(clipData, shadow, task, 0)
            } else {
                @Suppress("DEPRECATION")
                view.startDrag(clipData, shadow, task, 0)
            }
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun setData(data: List<Task>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun addItem(task: Task) {
        items.add(task)
        notifyItemInserted(items.size - 1)
    }

    fun updateItem(task: Task) {
        val index = items.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            items[index] = task
            notifyItemChanged(index)
        }
    }

    fun removeItem(taskId: Long) {
        val index = items.indexOfFirst { it.id == taskId }
        if (index >= 0) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
