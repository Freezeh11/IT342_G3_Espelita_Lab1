package com.it342.standupsync

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.it342.standupsync.adapter.TaskAdapter
import com.it342.standupsync.api.ApiClient
import com.it342.standupsync.api.AuthApi
import com.it342.standupsync.api.TaskApi
import com.it342.standupsync.model.Task
import com.it342.standupsync.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProjectActivity : AppCompatActivity() {

    private lateinit var tvProjectName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvAvatarLetter: TextView
    private lateinit var btnAddTask: LinearLayout
    private lateinit var btnGenerateReport: Button
    private lateinit var profileArea: LinearLayout

    private lateinit var rvInProgress: RecyclerView
    private lateinit var rvBlockers: RecyclerView
    private lateinit var rvDone: RecyclerView

    private lateinit var sectionInProgress: LinearLayout
    private lateinit var sectionBlockers: LinearLayout
    private lateinit var sectionDone: LinearLayout

    private lateinit var inProgressAdapter: TaskAdapter
    private lateinit var blockersAdapter: TaskAdapter
    private lateinit var doneAdapter: TaskAdapter

    private var projectId: Long = -1
    private var projectName: String = "My Project"
    private var allTasks: MutableList<Task> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_project)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        projectName = intent.getStringExtra("projectName") ?: "My Project"
        projectId = intent.getLongExtra("projectId", -1)

        tvProjectName = findViewById(R.id.tvProjectName)
        tvUsername = findViewById(R.id.tvUsername)
        tvAvatarLetter = findViewById(R.id.tvAvatarLetter)
        btnAddTask = findViewById(R.id.btnAddTask)
        btnGenerateReport = findViewById(R.id.btnGenerateReport)
        profileArea = findViewById(R.id.profileArea)
        rvInProgress = findViewById(R.id.rvInProgress)
        rvBlockers = findViewById(R.id.rvBlockers)
        rvDone = findViewById(R.id.rvDone)
        sectionInProgress = findViewById(R.id.sectionInProgress)
        sectionBlockers = findViewById(R.id.sectionBlockers)
        sectionDone = findViewById(R.id.sectionDone)

        tvProjectName.text = projectName

        // Logo click -> back to dashboard
        findViewById<View>(R.id.tvLogo).setOnClickListener { finish() }

        inProgressAdapter = TaskAdapter(onEdit = { showUpdateTaskDialog(it) }, onDelete = { deleteTask(it) })
        blockersAdapter = TaskAdapter(onEdit = { showUpdateTaskDialog(it) }, onDelete = { deleteTask(it) })
        doneAdapter = TaskAdapter(onEdit = { showUpdateTaskDialog(it) }, onDelete = { deleteTask(it) })

        rvInProgress.layoutManager = LinearLayoutManager(this)
        rvInProgress.adapter = inProgressAdapter
        rvBlockers.layoutManager = LinearLayoutManager(this)
        rvBlockers.adapter = blockersAdapter
        rvDone.layoutManager = LinearLayoutManager(this)
        rvDone.adapter = doneAdapter

        btnAddTask.setOnClickListener { showAddTaskDialog() }
        btnGenerateReport.setOnClickListener { showReportDialog() }
        profileArea.setOnClickListener { showProfileMenu(it) }

        // Setup drag-and-drop targets
        setupDragTargets()

        loadUser()
        loadTasks()
    }

    // ─── Drag-and-Drop ─────────────────────────────────────────────────

    private fun setupDragTargets() {
        val highlightColor = Color.argb(30, 157, 128, 255)

        fun makeDragListener(targetStatus: String): View.OnDragListener {
            return View.OnDragListener { view, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> true
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        view.setBackgroundColor(highlightColor)
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        view.setBackgroundColor(Color.TRANSPARENT)
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        view.setBackgroundColor(Color.TRANSPARENT)
                        val task = event.localState as? Task
                        if (task != null && task.status != targetStatus) {
                            if (targetStatus == "blocker") {
                                // Prompt for blocker reason
                                showBlockerReasonDialog(task)
                            } else {
                                val updatedTask = task.copy(status = targetStatus, blockerReason = "")
                                updateTask(updatedTask)
                            }
                        }
                        true
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        view.setBackgroundColor(Color.TRANSPARENT)
                        true
                    }
                    else -> false
                }
            }
        }

        sectionInProgress.setOnDragListener(makeDragListener("inProgress"))
        sectionBlockers.setOnDragListener(makeDragListener("blocker"))
        sectionDone.setOnDragListener(makeDragListener("done"))
    }

    private fun showBlockerReasonDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_blocker_reason, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etReason = dialogView.findViewById<EditText>(R.id.etBlockerReason)
        dialogView.findViewById<View>(R.id.btnConfirmBlocker).setOnClickListener {
            val reason = etReason.text.toString().trim()
            if (reason.isEmpty()) {
                Toast.makeText(this, "Please enter a blocker reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updatedTask = task.copy(status = "blocker", blockerReason = reason)
            updateTask(updatedTask)
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btnCancelBlocker).setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.let { w ->
            val lp = w.attributes
            lp.gravity = android.view.Gravity.CENTER
            w.attributes = lp
        }
    }

    // ─── Data Loading ──────────────────────────────────────────────────

    private fun loadUser() {
        val api = ApiClient.getRetrofit(this).create(AuthApi::class.java)
        api.getCurrentUser().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    tvUsername.text = user.username
                    tvAvatarLetter.text = user.username.first().uppercaseChar().toString()
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {}
        })
    }

    private fun loadTasks() {
        val api = ApiClient.getRetrofit(this).create(TaskApi::class.java)
        api.getTasks(projectId).enqueue(object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                if (response.isSuccessful) {
                    allTasks = (response.body() ?: emptyList()).toMutableList()
                    updateTaskLists()
                }
            }
            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                Toast.makeText(this@ProjectActivity, "Could not load tasks", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTaskLists() {
        inProgressAdapter.setData(allTasks.filter { it.status == "inProgress" })
        blockersAdapter.setData(allTasks.filter { it.status == "blocker" })
        doneAdapter.setData(allTasks.filter { it.status == "done" })
    }

    // ─── Add Task Dialog ───────────────────────────────────────────────

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_StandupSync)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val etTaskDescription = dialogView.findViewById<EditText>(R.id.etTaskDescription)

        dialogView.findViewById<View>(R.id.btnCreateTask).setOnClickListener {
            val title = etTaskName.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a task name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val task = Task(
                title = title,
                description = etTaskDescription.text.toString().trim(),
                status = "inProgress",
                projectId = projectId,
                blockerReason = ""
            )
            createTask(task)
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.let { w ->
            val lp = w.attributes
            lp.gravity = android.view.Gravity.CENTER
            w.attributes = lp
        }
    }

    private fun createTask(task: Task) {
        val api = ApiClient.getRetrofit(this).create(TaskApi::class.java)
        api.createTask(task).enqueue(object : Callback<Task> {
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                if (response.isSuccessful && response.body() != null) {
                    allTasks.add(response.body()!!)
                    updateTaskLists()
                }
            }
            override fun onFailure(call: Call<Task>, t: Throwable) {
                Toast.makeText(this@ProjectActivity, "Failed to create task", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ─── Update Task Dialog ────────────────────────────────────────────

    private fun showUpdateTaskDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_task, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_StandupSync)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setGravity(android.view.Gravity.CENTER)

        val tvStatusBadge = dialogView.findViewById<TextView>(R.id.tvStatusBadge)
        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val etTaskDescription = dialogView.findViewById<EditText>(R.id.etTaskDescription)
        val btnProgress = dialogView.findViewById<Button>(R.id.btnStatusProgress)
        val btnBlocker = dialogView.findViewById<Button>(R.id.btnStatusBlocker)
        val btnDone = dialogView.findViewById<Button>(R.id.btnStatusDone)
        val layoutBlockerReason = dialogView.findViewById<LinearLayout>(R.id.layoutBlockerReason)
        val etBlockerReason = dialogView.findViewById<EditText>(R.id.etBlockerReason)

        etTaskName.setText(task.title)
        etTaskDescription.setText(task.description ?: "")
        etBlockerReason.setText(task.blockerReason ?: "")

        var selectedStatus = task.status

        fun updateStatusUI() {
            val purpleColor = ContextCompat.getColor(this, R.color.purple_accent)
            val redColor = ContextCompat.getColor(this, R.color.red_accent)
            val greenColor = ContextCompat.getColor(this, R.color.green_accent)
            val dimColor = ContextCompat.getColor(this, R.color.text_hint)

            val statusLabels = mapOf("inProgress" to "PROGRESS", "blocker" to "BLOCKER", "done" to "DONE")
            val statusColors = mapOf("inProgress" to purpleColor, "blocker" to redColor, "done" to greenColor)
            val color = statusColors[task.status] ?: purpleColor

            tvStatusBadge.text = statusLabels[task.status] ?: "PROGRESS"
            tvStatusBadge.setTextColor(color)
            val badgeBg = GradientDrawable()
            badgeBg.cornerRadius = 12f
            badgeBg.setColor(Color.argb(34, Color.red(color), Color.green(color), Color.blue(color)))
            badgeBg.setStroke(1, Color.argb(68, Color.red(color), Color.green(color), Color.blue(color)))
            tvStatusBadge.background = badgeBg

            listOf(btnProgress to "inProgress", btnBlocker to "blocker", btnDone to "done").forEach { (btn, status) ->
                val c = statusColors[status] ?: purpleColor
                if (selectedStatus == status) {
                    val bg = GradientDrawable()
                    bg.cornerRadius = 8f
                    bg.setColor(Color.argb(34, Color.red(c), Color.green(c), Color.blue(c)))
                    bg.setStroke(1, c)
                    btn.background = bg
                    btn.setTextColor(c)
                } else {
                    val bg = GradientDrawable()
                    bg.cornerRadius = 8f
                    bg.setColor(Color.argb(10, 255, 255, 255))
                    bg.setStroke(1, Color.argb(26, 255, 255, 255))
                    btn.background = bg
                    btn.setTextColor(dimColor)
                }
            }
            layoutBlockerReason.visibility = if (selectedStatus == "blocker") View.VISIBLE else View.GONE
        }

        updateStatusUI()

        btnProgress.setOnClickListener { selectedStatus = "inProgress"; updateStatusUI() }
        btnBlocker.setOnClickListener { selectedStatus = "blocker"; updateStatusUI() }
        btnDone.setOnClickListener { selectedStatus = "done"; updateStatusUI() }

        dialogView.findViewById<View>(R.id.btnUpdateTask).setOnClickListener {
            val updatedTask = task.copy(
                title = etTaskName.text.toString().trim().ifEmpty { task.title },
                description = etTaskDescription.text.toString().trim(),
                status = selectedStatus,
                blockerReason = if (selectedStatus == "blocker") etBlockerReason.text.toString().trim() else ""
            )
            updateTask(updatedTask)
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.let { w ->
            val lp = w.attributes
            lp.gravity = android.view.Gravity.CENTER
            w.attributes = lp
        }
    }

    private fun updateTask(task: Task) {
        val api = ApiClient.getRetrofit(this).create(TaskApi::class.java)
        api.updateTask(task.id!!, task).enqueue(object : Callback<Task> {
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                if (response.isSuccessful && response.body() != null) {
                    val updated = response.body()!!
                    val index = allTasks.indexOfFirst { it.id == updated.id }
                    if (index >= 0) allTasks[index] = updated
                    updateTaskLists()
                }
            }
            override fun onFailure(call: Call<Task>, t: Throwable) {
                Toast.makeText(this@ProjectActivity, "Failed to update task", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteTask(task: Task) {
        val api = ApiClient.getRetrofit(this).create(TaskApi::class.java)
        api.deleteTask(task.id!!).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                allTasks.removeAll { it.id == task.id }
                updateTaskLists()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProjectActivity, "Failed to delete task", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ─── Report Dialog (with draggable tasks) ──────────────────────────

    private fun showReportDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_report, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_StandupSync)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setGravity(android.view.Gravity.CENTER)

        val layoutYesterday = dialogView.findViewById<LinearLayout>(R.id.layoutYesterday)
        val layoutToday = dialogView.findViewById<LinearLayout>(R.id.layoutToday)
        val layoutBlockersSection = dialogView.findViewById<LinearLayout>(R.id.layoutBlockersSection)
        val layoutBlockers = dialogView.findViewById<LinearLayout>(R.id.layoutBlockers)
        val tvSummary = dialogView.findViewById<TextView>(R.id.tvSummary)

        val doneTasks = allTasks.filter { it.status == "done" }.toMutableList()
        val inProgressTasks = allTasks.filter { it.status == "inProgress" }.toMutableList()
        val blockerTasks = allTasks.filter { it.status == "blocker" }.toMutableList()

        // Track which tasks are in yesterday vs today (for dragging)
        val yesterdayTasks = doneTasks.toMutableList()
        val todayTasks = inProgressTasks.toMutableList()

        fun buildSummary(): String {
            val y = yesterdayTasks.joinToString(", ") { it.title }
            val t = todayTasks.joinToString(", ") { it.title }
            val b = blockerTasks.joinToString(", ") { it.blockerReason ?: it.title }
            return buildString {
                if (y.isNotBlank()) append("Yesterday, I $y. ")
                if (t.isNotBlank()) append("Today, I am focused on $t. ")
                if (b.isNotBlank()) append("Currently, I am blocked by $b.")
            }.trim()
        }

        fun refreshReport() {
            layoutYesterday.removeAllViews()
            layoutToday.removeAllViews()

            fun createDraggableTaskItem(task: Task, section: String): TextView {
                val tv = TextView(this)
                tv.text = "• ${task.title}${if (!task.description.isNullOrBlank()) " — ${task.description}" else ""}"
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                tv.textSize = 13f
                tv.setPadding(0, 8, 0, 8)
                tv.tag = task

                // Long press to drag between yesterday/today
                tv.setOnLongClickListener { v ->
                    val clipData = ClipData.newPlainText("report_task", task.id.toString())
                    val shadow = View.DragShadowBuilder(v)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        v.startDragAndDrop(clipData, shadow, Pair(task, section), 0)
                    } else {
                        @Suppress("DEPRECATION")
                        v.startDrag(clipData, shadow, Pair(task, section), 0)
                    }
                    true
                }
                return tv
            }

            // Yesterday items
            yesterdayTasks.forEach { task ->
                layoutYesterday.addView(createDraggableTaskItem(task, "yesterday"))
            }
            if (yesterdayTasks.isEmpty()) {
                val tv = TextView(this)
                tv.text = "No completed tasks"
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
                tv.textSize = 12f
                layoutYesterday.addView(tv)
            }

            // Today items
            todayTasks.forEach { task ->
                layoutToday.addView(createDraggableTaskItem(task, "today"))
            }
            if (todayTasks.isEmpty()) {
                val tv = TextView(this)
                tv.text = "No in-progress tasks"
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
                tv.textSize = 12f
                layoutToday.addView(tv)
            }

            // Update summary
            tvSummary.text = "\"${buildSummary()}\""
        }

        // Setup drag listeners on Yesterday and Today containers
        val highlightColor = Color.argb(25, 157, 128, 255)

        fun makeSectionDragListener(targetSection: String): View.OnDragListener {
            return View.OnDragListener { view, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> true
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        view.setBackgroundColor(highlightColor)
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        view.setBackgroundColor(Color.TRANSPARENT)
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        view.setBackgroundColor(Color.TRANSPARENT)
                        @Suppress("UNCHECKED_CAST")
                        val pair = event.localState as? Pair<Task, String>
                        if (pair != null) {
                            val (task, fromSection) = pair
                            if (fromSection != targetSection) {
                                if (fromSection == "yesterday") {
                                    yesterdayTasks.remove(task)
                                    todayTasks.add(task)
                                } else {
                                    todayTasks.remove(task)
                                    yesterdayTasks.add(task)
                                }
                                refreshReport()
                            }
                        }
                        true
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        view.setBackgroundColor(Color.TRANSPARENT)
                        true
                    }
                    else -> false
                }
            }
        }

        layoutYesterday.setOnDragListener(makeSectionDragListener("yesterday"))
        layoutToday.setOnDragListener(makeSectionDragListener("today"))

        // Populate blockers (not draggable)
        if (blockerTasks.isNotEmpty()) {
            layoutBlockersSection.visibility = View.VISIBLE
            blockerTasks.forEach { task ->
                val tv = TextView(this)
                tv.text = "• ${task.blockerReason ?: task.title}"
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                tv.textSize = 13f
                tv.setPadding(0, 4, 0, 4)
                layoutBlockers.addView(tv)
            }
        }

        refreshReport()

        dialogView.findViewById<View>(R.id.btnCopySummary).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Report", buildSummary()))
            Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        dialogView.findViewById<View>(R.id.btnCloseReport).setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.let { w ->
            val lp = w.attributes
            lp.gravity = android.view.Gravity.CENTER
            w.attributes = lp
        }
    }

    // ─── Profile Menu ──────────────────────────────────────────────────

    private fun showProfileMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("Profile Settings")
        popup.menu.add("Logout")
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Profile Settings" -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                "Logout" -> {
                    ApiClient.clearAuth(this)
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}