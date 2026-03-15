package com.example.myhelloapp

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.view.View
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var taskContainer: LinearLayout
    private lateinit var addButton: FloatingActionButton
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var saveButton: FloatingActionButton
    private lateinit var upButton: FloatingActionButton
    private val tasks = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskContainer = findViewById(R.id.taskContainer)
        addButton = findViewById(R.id.addButton)
        saveButton = findViewById(R.id.saveButton)

        sharedPreferences = getSharedPreferences("Tasks", MODE_PRIVATE) // ?

        loadTasks()

        addButton.setOnClickListener {
            showAddTaskDialog()
        }

        saveButton.setOnClickListener {
            saveTasks()
        }

    }

    private fun loadTasks() {
        val tasksJson = sharedPreferences.getString("tasks", null)
        if (!tasksJson.isNullOrEmpty()) {
            val tasksType = object : TypeToken<List<String>>() {}.type
            val loaded = Gson().fromJson<List<String>>(tasksJson, tasksType)
            tasks.clear()
            loaded.reversed().forEach { addTask(it) }
        }
    }

    private fun saveTasks() {
        val tasksJson = Gson().toJson(tasks)
        sharedPreferences.edit().putString("tasks", tasksJson).apply()
        showSnackbar(taskContainer, "Successfully saved")
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null)
        val editText = dialogView.findViewById<EditText>(R.id.editText)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        var validTask = true

        editText.requestFocus()

        val dialog = AlertDialog.Builder(this, R.style.CustomDialogStyle)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val taskText = editText.text.toString()
            validTask = addTask(taskText)
            dialog.dismiss()
            if (validTask) { saveTasks() }
        }

        dialog.show()

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    @SuppressLint("InflateParams")
    private fun addTask(taskText: String): Boolean {
        val trimmedText = taskText.trim()
        if (trimmedText.isNotBlank()) {
            val taskView = LayoutInflater.from(this).inflate(R.layout.task_item, taskContainer, false )
            val textView = taskView.findViewById<TextView>(R.id.taskTextView)
            textView.text = trimmedText

            // Tap to edit
            taskView.setOnClickListener {
                showEditTaskDialog(textView)
            }

            // Long press to delete
            taskView.setOnLongClickListener {
                deleteTask(taskView)
                true
            }

            taskContainer.addView(taskView, 0)

            tasks.add(0, trimmedText)

            return true
        }
        return false
    }


    private fun deleteTask(taskView: View) {
        val textView = taskView.findViewById<TextView>(R.id.taskTextView)
        tasks.remove(textView.text.toString())
        taskView.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction { taskContainer.removeView(taskView) }
    }


    private fun showEditTaskDialog(textView: TextView) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null)
        val editText = dialogView.findViewById<EditText>(R.id.editText)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        val upButton = dialogView.findViewById<Button>(R.id.upButton)

        upButton.visibility = View.VISIBLE;

        editText.setText(textView.text)

        editText.requestFocus()

        val dialog = AlertDialog.Builder(this, R.style.CustomDialogStyle)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val taskText = editText.text.toString()
            editTask(textView, taskText)
            dialog.dismiss()
            saveTasks()
        }

        upButton.setOnClickListener {
            val index = tasks.indexOf(textView.text.toString())
            if (index > 0) {
                // Move to top in data list
                val item = tasks.removeAt(index)
                tasks.add(0, item)
                // Move to top in views
                val taskView = taskContainer.getChildAt(index)
                taskContainer.removeView(taskView)
                taskContainer.addView(taskView, 0)
            }
            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    private fun editTask(textView: TextView, editedText: String) {
        val trimmedText = editedText.trim()
        val index = tasks.indexOf(textView.text.toString())
        if (trimmedText.isNotBlank()) {
            textView.text = trimmedText
            if (index != -1) tasks[index] = trimmedText
        } else {
            if (index != -1) tasks.removeAt(index)
            taskContainer.removeView(textView.parent as View)
        }
    }

    fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .show()
    }
}
