package com.example.securescribe;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.List;
import java.util.Objects;

public class ArchiveActivity extends BaseSecureActivity implements NoteActionListener {
RecyclerView recyclerView;
Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_archive);
        recyclerView = findViewById(R.id.rvNotes);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Archive");
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        NoteDatabase.databaseWriteExecutor.execute(() -> {
            List<Note> notes = NoteDatabase.getInstance(this).noteDao().getArchivedNotes();
            runOnUiThread(() -> {
                ArchiveAdapter adapter = new ArchiveAdapter(notes, this);
                recyclerView.setAdapter(adapter);
            });
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onNoteDeleted(Note note, int position) {}

    @Override
    public void onNoteArchived(Note note, int position) {}

    @Override
    public void onNoteUnarchived(Note note, int position) {
        note.setArchived(false);
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            NoteDatabase.getInstance(this).noteDao().update(note);
            runOnUiThread(() -> {
                if (recyclerView.getAdapter() instanceof ArchiveAdapter) {
                    ArchiveAdapter adapter = (ArchiveAdapter) recyclerView.getAdapter();
                    adapter.removeItem(position);
                }
            });
        });
    }

    @Override
    public void onNoteRestored(Note note, int position) {}

    @Override
    public void onNotePermanentlyDeleted(Note note, int position) {}
}