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
                    SharedPreferences prefs = getSharedPreferences("SecureScribe", MODE_PRIVATE);
                    String savedPassword = prefs.getString("password", null);

                    if (savedPassword == null) {
                        // first launch — save this as master password
                        prefs.edit().putString("password", password).apply();
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    } else if (savedPassword.equals(password)) {
                        // correct password
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        // wrong password
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
}