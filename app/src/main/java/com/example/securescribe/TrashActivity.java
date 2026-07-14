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

public class TrashActivity extends AppCompatActivity {
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

        NoteDatabase.databaseWriteExecutor.execute(() -> {
            long cutoff = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
            NoteDatabase.getInstance(this).noteDao().purgeOldDeletedNotes(cutoff);
            List<Note> notes = NoteDatabase.getInstance(this).noteDao().getDeletedNotes();
            runOnUiThread(() -> {
                TrashAdapter adapter = new TrashAdapter(notes);
                recyclerView.setAdapter(adapter);
            });
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}