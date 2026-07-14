package com.example.securescribe;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecurityUtils {

    private static final String PREF_FILE_NAME = "secure_prefs";
    private static SharedPreferences cachedPrefs;

    public static synchronized SharedPreferences getEncryptedSharedPreferences(Context context) {
        if (cachedPrefs != null) {
            return cachedPrefs;
        }

        try {
            MasterKey masterKey = new MasterKey.Builder(context.getApplicationContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            cachedPrefs = EncryptedSharedPreferences.create(
                    context.getApplicationContext(),
                    PREF_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            return cachedPrefs;
        } catch (GeneralSecurityException | IOException e) {
            // Fail loud - security fallback is a bypass
            throw new RuntimeException("Critical Security Error: Could not initialize EncryptedSharedPreferences", e);
        }
    }
}
