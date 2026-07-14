package com.example.securescribe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
EditText pass;
Button Unlock;
    private int failedAttempts = 0;
    private long lastAttemptTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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
                    long currentTime = System.currentTimeMillis();
                    long lockoutTime = (long) Math.pow(2, failedAttempts - 3) * 1000; // Delay after 3 failed attempts
                    if (failedAttempts >= 3 && currentTime - lastAttemptTime < lockoutTime) {
                        long remaining = (lockoutTime - (currentTime - lastAttemptTime)) / 1000;
                        pass.setError("Too many attempts. Try again in " + remaining + "s");
                        return;
                    }

                    SharedPreferences prefs = SecurityUtils.getEncryptedSharedPreferences(MainActivity.this);
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
                        onSuccessfulUnlock();
                    } else if (savedPassword.equals(password)) {
                        // correct password
                        onSuccessfulUnlock();
                    } else {
                        // wrong password
                        failedAttempts++;
                        lastAttemptTime = System.currentTimeMillis();
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

    private void onSuccessfulUnlock() {
        failedAttempts = 0;
        SharedPreferences prefs = SecurityUtils.getEncryptedSharedPreferences(this);
        prefs.edit().putLong("last_unlocked", System.currentTimeMillis()).apply();
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }
}