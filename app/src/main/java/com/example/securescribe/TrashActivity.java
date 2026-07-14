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

public class TrashActivity extends BaseSecureActivity implements NoteActionListener {
RecyclerView recyclerView;
Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trash);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Trash");
        recyclerView = findViewById(R.id.rvNotes);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        NoteDatabase db = NoteDatabase.getInstance(this);
        if (db == null) return;

        NoteDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long cutoff = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
                db.noteDao().purgeOldDeletedNotes(cutoff);
                List<Note> notes = db.noteDao().getDeletedNotes();
                runOnUiThread(() -> {
                    TrashAdapter adapter = new TrashAdapter(notes, this);
                    recyclerView.setAdapter(adapter);
                });
            } catch (Exception e) {
                NoteDatabase.dbError.postValue(e);
            }
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
    public void onNoteUnarchived(Note note, int position) {}

    @Override
    public void onNoteRestored(Note note, int position) {
        NoteDatabase db = NoteDatabase.getInstance(this);
        if (db == null) return;

        note.setDeleted(false);
        note.setDeletedAt(0);
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.noteDao().update(note);
                runOnUiThread(() -> {
                    if (recyclerView.getAdapter() instanceof TrashAdapter) {
                        TrashAdapter adapter = (TrashAdapter) recyclerView.getAdapter();
                        adapter.removeItem(position);
                    }
                });
            } catch (Exception e) {
                NoteDatabase.dbError.postValue(e);
            }
        });
    }

    @Override
    public void onNotePermanentlyDeleted(Note note, int position) {
        NoteDatabase db = NoteDatabase.getInstance(this);
        if (db == null) return;

        NoteDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.noteDao().delete(note);
                runOnUiThread(() -> {
                    if (recyclerView.getAdapter() instanceof TrashAdapter) {
                        TrashAdapter adapter = (TrashAdapter) recyclerView.getAdapter();
                        adapter.removeItem(position);
                    }
                });
            } catch (Exception e) {
                NoteDatabase.dbError.postValue(e);
            }
        });
    }
}