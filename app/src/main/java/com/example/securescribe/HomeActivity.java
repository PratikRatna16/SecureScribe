package com.example.securescribe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;


import android.content.SharedPreferences;
import java.util.List;

public class HomeActivity extends BaseSecureActivity implements NoteActionListener {
RecyclerView rv;
FloatingActionButton FloatAction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        rv = findViewById(R.id.rvNotes);

        rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        FloatAction = findViewById(R.id.fabAddNote);
        FloatAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AddEditNoteActivity.class));
            }
        });
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.navigationView);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_archive) {
                startActivity(new Intent(HomeActivity.this, ArchiveActivity.class));
            } else if (id == R.id.nav_trash) {
                startActivity(new Intent(HomeActivity.this, TrashActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onNoteDeleted(Note note, int position) {
        note.setDeleted(true);
        note.setDeletedAt(System.currentTimeMillis());
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            NoteDatabase.getInstance(this).noteDao().update(note);
            runOnUiThread(() -> {
                if (rv.getAdapter() instanceof NoteAdapter) {
                    NoteAdapter adapter = (NoteAdapter) rv.getAdapter();
                    adapter.removeItem(position);
                }
            });
        });
    }

    @Override
    public void onNoteArchived(Note note, int position) {
        note.setArchived(true);
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            NoteDatabase.getInstance(this).noteDao().update(note);
            runOnUiThread(() -> {
                if (rv.getAdapter() instanceof NoteAdapter) {
                    NoteAdapter adapter = (NoteAdapter) rv.getAdapter();
                    adapter.removeItem(position);
                }
            });
        });
    }

    @Override
    public void onNoteUnarchived(Note note, int position) {}

    @Override
    public void onNoteRestored(Note note, int position) {}

    @Override
    public void onNotePermanentlyDeleted(Note note, int position) {}

    @Override
    protected void onResume() {
        super.onResume();

        NoteDatabase.databaseWriteExecutor.execute(() -> {
            List<Note> notes = NoteDatabase.getInstance(this).noteDao().getAllNotes();
            runOnUiThread(() -> {
                if (rv.getAdapter() instanceof NoteAdapter) {
                    ((NoteAdapter) rv.getAdapter()).setNotes(notes);
                } else {
                    NoteAdapter adapter = new NoteAdapter(notes, this);
                    rv.setAdapter(adapter);
                }
            });
        });
    }
}