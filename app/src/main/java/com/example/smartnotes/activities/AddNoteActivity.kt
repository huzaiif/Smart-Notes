package com.example.smartnotes.activities

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartnotes.api.GeminiHelper
import com.example.smartnotes.database.NoteDatabase
import com.example.smartnotes.databinding.ActivityAddNoteBinding
import com.example.smartnotes.model.Note
import com.example.smartnotes.receiver.ReminderReceiver
import kotlinx.coroutines.launch
import java.util.*

class AddNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private var reminderCalendar: Calendar? = null
    private val geminiHelper = GeminiHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGenerateAI.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            if (title.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                geminiHelper.generateText("Act as a professional note-taking assistant. Write a clear, concise, and well-structured note for the topic: \"$title\". " +
                        "Include a brief definition, key points, and a short conclusion. Keep it under 120 words and make it easy for a student to understand.") { description ->
                    binding.progressBar.visibility = View.GONE
                    binding.editTextDescription.setText(description)
                }
            } else {
                Toast.makeText(this, "Enter a title first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSetReminder.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnSaveNote.setOnClickListener {
            saveNote()
        }
    }

    private fun showDateTimePicker() {
        val currentCalendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                reminderCalendar = calendar
                Toast.makeText(this, "Reminder set for: ${calendar.time}", Toast.LENGTH_SHORT).show()
            }, currentCalendar.get(Calendar.HOUR_OF_DAY), currentCalendar.get(Calendar.MINUTE), true).show()
        }, currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveNote() {
        val title = binding.editTextTitle.text.toString()
        val description = binding.editTextDescription.text.toString()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val reminderTime = reminderCalendar?.timeInMillis ?: 0L
        val note = Note(title = title, description = description, reminderTime = reminderTime)

        lifecycleScope.launch {
            NoteDatabase.getDatabase(this@AddNoteActivity).noteDao().insertNote(note)
            if (reminderTime > System.currentTimeMillis()) {
                scheduleReminder(note)
            }
            finish()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleReminder(note: Note) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("TITLE", note.title)
            putExtra("DESCRIPTION", note.description)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            note.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            note.reminderTime,
            pendingIntent
        )
    }
}
