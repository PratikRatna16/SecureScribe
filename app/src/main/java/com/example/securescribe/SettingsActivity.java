package com.example.securescribe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
TextView tvChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        tvChangePassword = findViewById(R.id.tvChangePassword);
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_settings);

        tvChangePassword.setOnClickListener(v -> {
            android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);

            android.widget.EditText etCurrent = new android.widget.EditText(this);
            etCurrent.setHint(R.string.hint_current_password);
            etCurrent.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

            android.widget.EditText etNew = new android.widget.EditText(this);
            etNew.setHint(R.string.hint_new_password);
            etNew.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

            android.widget.EditText etConfirm = new android.widget.EditText(this);
            etConfirm.setHint(R.string.hint_confirm_new_password);
            etConfirm.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

            layout.addView(etCurrent);
            layout.addView(etNew);
            layout.addView(etConfirm);

            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_change_password)
                    .setView(layout)
                    .setPositiveButton(R.string.action_save, null)
                    .setNegativeButton(R.string.action_cancel, null)
                    .create();

            dialog.show();

            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(btnView -> {
                String current = etCurrent.getText().toString().trim();
                String newPass = etNew.getText().toString().trim();
                String confirm = etConfirm.getText().toString().trim();

                android.content.SharedPreferences prefs =
                        getSharedPreferences("SecureScribe", MODE_PRIVATE);
                String saved = prefs.getString("password", null);

                if (!current.equals(saved)) {
                    etCurrent.setError(getString(R.string.error_wrong_password));
                } else if (newPass.isEmpty()) {
                    etNew.setError(getString(R.string.error_enter_new_password));
                } else if (!newPass.equals(confirm)) {
                    etConfirm.setError(getString(R.string.error_passwords_dont_match));
                } else {
                    prefs.edit().putString("password", newPass).apply();
                    android.widget.Toast.makeText(this, R.string.msg_password_changed,
                            android.widget.Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}