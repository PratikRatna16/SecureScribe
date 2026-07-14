package com.example.securescribe;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class AddEditNoteActivity extends BaseSecureActivity {
Toolbar toolbar;
EditText Title,multiline;
    int noteId = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_note);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Title = findViewById(R.id.Title);
        multiline = findViewById(R.id.multiline);

        noteId = getIntent().getIntExtra("note_id", -1);
        String existingTitle = getIntent().getStringExtra("note_title");
        String existingContent = getIntent().getStringExtra("note_content");

        if (noteId != -1) {
            Title.setText(existingTitle);
            multiline.setText(existingContent);
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {

            @Override
            public void handleOnBackPressed() {
                String title = Title.getText().toString().trim();
                String content = multiline.getText().toString().trim();

                if (!title.isEmpty()) {
                    NoteDatabase db = NoteDatabase.getInstance(AddEditNoteActivity.this);
                    if (db == null) {
                        finish();
                        return;
                    }

                    NoteDatabase.databaseWriteExecutor.execute(() -> {
                        try {
                            if (noteId == -1) {
                                Note note = new Note(title, content, System.currentTimeMillis());
                                db.noteDao().insert(note);
                            } else {
                                Note note = db.noteDao().getNoteById(noteId);
                                if (note != null) {
                                    note.title = title;
                                    note.content = content;
                                    note.timestamp = System.currentTimeMillis();
                                    db.noteDao().update(note);
                                }
                            }
                            runOnUiThread(AddEditNoteActivity.this::finish);
                        } catch (Exception e) {
                            NoteDatabase.dbError.postValue(e);
                        }
                    });
                } else {
                    finish();
                }
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.action_save) {
            String title = Title.getText().toString().trim();
            String content = multiline.getText().toString().trim();

            if (title.isEmpty()) {
                Title.setError(getString(R.string.error_title_required));
                return true;
            }

            NoteDatabase db = NoteDatabase.getInstance(this);
            if (db == null) return true;

            NoteDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    if (noteId == -1) {
                        Note note = new Note(title, content, System.currentTimeMillis());
                        db.noteDao().insert(note);
                    } else {
                        Note note = db.noteDao().getNoteById(noteId);
                        if (note != null) {
                            note.title = title;
                            note.content = content;
                            note.timestamp = System.currentTimeMillis();
                            db.noteDao().update(note);
                        }
                    }
                    runOnUiThread(this::finish);
                } catch (Exception e) {
                    NoteDatabase.dbError.postValue(e);
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
