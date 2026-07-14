package com.example.securescribe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;

public class MainActivity extends AppCompatActivity {
EditText pass;
Button Unlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Section 3: Recovery check
        handleRekeyRecovery();

        pass = findViewById(R.id.password);
        Unlock = findViewById(R.id.button);
            Unlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String password = pass.getText().toString().trim();
                    if (password.isEmpty()) {
                        pass.setError(getString(R.string.error_password_required));
                        return;
                    }

                    // Brute-force protection
                    SharedPreferences prefs = SecurityUtils.getEncryptedSharedPreferences(MainActivity.this);
                    int failedAttempts = prefs.getInt("failed_attempts", 0);
                    long lastAttemptTime = prefs.getLong("last_attempt_time", 0);

                    long currentTime = System.currentTimeMillis();
                    long lockoutTime = (long) Math.pow(2, failedAttempts - 3) * 1000; // Delay after 3 failed attempts
                    if (failedAttempts >= 3 && currentTime - lastAttemptTime < lockoutTime) {
                        long remaining = (lockoutTime - (currentTime - lastAttemptTime)) / 1000;
                        pass.setError("Too many attempts. Try again in " + remaining + "s");
                        return;
                    }

                    String savedPassword = prefs.getString("password", null);

                    if (savedPassword == null) {
                        // Migration check: if old plaintext exists, move it to encrypted
                        SharedPreferences oldPrefs = getSharedPreferences("SecureScribe", MODE_PRIVATE);
                        savedPassword = oldPrefs.getString("password", null);
                        if (savedPassword != null) {
                            prefs.edit().putString("password", savedPassword).apply();
                            oldPrefs.edit().remove("password").apply();
                        }
                    }

                    if (savedPassword == null) {
                        // first launch — save this as master password
                        prefs.edit().putString("password", password).apply();
                        onSuccessfulUnlock(prefs);
                    } else if (savedPassword.equals(password)) {
                        // correct password
                        onSuccessfulUnlock(prefs);
                    } else if (verifyPasswordWorks(password)) {
                        // Password works but doesn't match prefs (likely interrupted rekey)
                        prefs.edit().putString("password", password).apply();
                        onSuccessfulUnlock(prefs);
                    } else {
                        // wrong password
                        failedAttempts++;
                        prefs.edit()
                                .putInt("failed_attempts", failedAttempts)
                                .putLong("last_attempt_time", System.currentTimeMillis())
                                .apply();
                        pass.setError(getString(R.string.error_wrong_password));
                    }
                }
            });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void onSuccessfulUnlock(SharedPreferences prefs) {
        // Cleanup backup if it still exists after successful login
        File backup = getDatabasePath(NoteDatabase.DB_NAME + ".rekey_backup");
        if (backup.exists()) {
            backup.delete();
        }

        prefs.edit()
                .putInt("failed_attempts", 0)
                .putLong("last_unlocked", System.currentTimeMillis())
                .apply();
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }

    private void handleRekeyRecovery() {
        File backup = getDatabasePath(NoteDatabase.DB_NAME + ".rekey_backup");
        if (!backup.exists()) return;

        SharedPreferences prefs = SecurityUtils.getEncryptedSharedPreferences(this);
        String storedPassword = prefs.getString("password", "");

        // Try the stored password first (might be old or already updated)
        if (verifyPasswordWorks(storedPassword)) {
            backup.delete();
            return;
        }

        // If stored fails, we need to prompt the user to try the one they were setting
        new AlertDialog.Builder(this)
                .setTitle("Update Recovery")
                .setMessage("A previous password change was interrupted. Please enter the password you were trying to set.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    pass.setHint("Enter new password");
                })
                .show();
    }

    private boolean verifyPasswordWorks(String password) {
        if (password == null || password.isEmpty()) return false;
        File dbFile = getDatabasePath(NoteDatabase.DB_NAME);
        if (!dbFile.exists()) return true; // New install

        SQLiteDatabase.loadLibs(this);
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(),
                password, null, SQLiteDatabase.OPEN_READONLY)) {
            db.rawQuery("SELECT count(*) FROM sqlite_master", null).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
