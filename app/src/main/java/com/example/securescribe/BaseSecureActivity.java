package com.example.securescribe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public abstract class BaseSecureActivity extends AppCompatActivity {

    private static final long SESSION_TIMEOUT = 30000; // 30 seconds

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        NoteDatabase.dbError.observe(this, e -> {
            if (e != null) {
                showDatabaseErrorDialog(e);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSession();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Record background time
        SharedPreferences prefs = SecurityUtils.getEncryptedSharedPreferences(this);
        prefs.edit().putLong("last_backgrounded", System.currentTimeMillis()).apply();
    }

    protected void checkSession() {
        SharedPreferences prefs = SecurityUtils.getEncryptedSharedPreferences(this);
        long lastBackgrounded = prefs.getLong("last_backgrounded", 0);
        
        // If it's been more than 30s since we were in foreground
        if (lastBackgrounded != 0 && System.currentTimeMillis() - lastBackgrounded > SESSION_TIMEOUT) {
            redirectToLock();
        }
    }

    private void redirectToLock() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showDatabaseErrorDialog(Exception e) {
        Log.e("SecureScribe", "Database error encountered", e);
        
        new AlertDialog.Builder(this)
                .setTitle("Database Error")
                .setMessage("A critical error occurred while accessing the database. This could be due to a wrong password or file corruption.\n\nWould you like to PERMANENTLY ERASE ALL NOTES to reset the app?")
                .setPositiveButton("Erase Everything", (dialog, which) -> {
                    resetDatabase();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    NoteDatabase.dbError.setValue(null);
                    finishAffinity();
                })
                .setCancelable(false)
                .show();
    }

    private void resetDatabase() {
        NoteDatabase.dbError.setValue(null);
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            // Close DB first
            NoteDatabase.getInstance(this).close();
            
            File dbFile = getDatabasePath(NoteDatabase.DB_NAME);
            File dbJournal = new File(dbFile.getPath() + "-journal");
            File dbWal = new File(dbFile.getPath() + "-wal");
            File dbShm = new File(dbFile.getPath() + "-shm");
            
            if (dbFile.exists()) dbFile.delete();
            if (dbJournal.exists()) dbJournal.delete();
            if (dbWal.exists()) dbWal.delete();
            if (dbShm.exists()) dbShm.delete();

            // Clear password
            SharedPreferences prefs = SecurityUtils.getEncryptedSharedPreferences(this);
            prefs.edit().remove("password").apply();

            runOnUiThread(() -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
