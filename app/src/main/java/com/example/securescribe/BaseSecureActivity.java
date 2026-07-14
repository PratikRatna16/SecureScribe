package com.example.securescribe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseSecureActivity extends AppCompatActivity {

    private static final long SESSION_TIMEOUT = 30000; // 30 seconds

    @Override
    protected void onResume() {
        super.onResume();
        checkSession();
    }

    protected void checkSession() {
        SharedPreferences prefs = SecurityUtils.getEncryptedSharedPreferences(this);
        long lastUnlocked = prefs.getLong("last_unlocked", 0);
        if (System.currentTimeMillis() - lastUnlocked > SESSION_TIMEOUT) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Update last unlocked to keep session alive
            prefs.edit().putLong("last_unlocked", System.currentTimeMillis()).apply();
        }
    }
}
