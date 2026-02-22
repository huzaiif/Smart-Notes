package com.example.smartnotes.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartnotes.adapter.NoteAdapter
import com.example.smartnotes.database.NoteDatabase
import com.example.smartnotes.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteAdapter = NoteAdapter { note ->
            lifecycleScope.launch {
                NoteDatabase.getDatabase(this@MainActivity).noteDao().deleteNote(note)
            }
        }

        binding.recyclerViewNotes.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        binding.fabAddNote.setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java))
        }

        lifecycleScope.launch {
            NoteDatabase.getDatabase(this@MainActivity).noteDao().getAllNotes()
                .collectLatest { notes ->
                    noteAdapter.submitList(notes)
                }
        }
    }
}
