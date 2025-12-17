package com.example.waslniiii;

import android.content.Intent;
import android.content.SharedPreferences; // For local account storage
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginBtn;
    TextView signupLink; // The "link" to open Sign Up

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Header GIF
        ImageView gifView = findViewById(R.id.gifView);
        Glide.with(this).asGif().load(R.drawable.animations).into(gifView);

        // 2. Initialize Views
        emailInput = findViewById(R.id.et_email);
        passwordInput = findViewById(R.id.et_motpasse);
        loginBtn = findViewById(R.id.button);
        signupLink = findViewById(R.id.tv_go_to_signup);

        // 3. Login Button Logic
        loginBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                performLogin();
            }
        });

        // 4. Navigate to Sign Up Activity
        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, activity_sign_up.class);
            startActivity(intent);
        });

        // 5. Auto-fill if returning from successful Sign Up
        if (getIntent().hasExtra("PREFILL_EMAIL")) {
            emailInput.setText(getIntent().getStringExtra("PREFILL_EMAIL"));
            passwordInput.setText(getIntent().getStringExtra("PREFILL_PASSWORD"));
        }
    }

    private void performLogin() {
        String email = emailInput.getText().toString().trim().toLowerCase();
        String password = passwordInput.getText().toString().trim();

        // ✅ CHECK NEW ACCOUNTS (Saved in SharedPreferences by Sign Up Activity)
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("email", "").toLowerCase();
        String savedPw = prefs.getString("password", "");
        String savedRole = prefs.getString("role", "");

        if (email.equals(savedEmail) && password.equals(savedPw)) {
            handleRedirection(savedRole);
            return;
        }

        // ✅ KEEP FAKE ACCOUNTS (Your "shortcut" logic)
        if (email.contains("client") || password.contains("client")) {
            handleRedirection("User");
            return;
        }

        if (email.contains("taxi") || password.contains("taxi")) {
            handleRedirection("Taxi Driver");
            return;
        }

        // ✅ SERVER LOGIN (Fallback to PHP script)
        loginServer(email, password);
    }

    private void handleRedirection(String role) {
        Intent intent;
        // Check if the user is a normal User/Client
        if (role.equalsIgnoreCase("User") || role.equalsIgnoreCase("client")) {
            Toast.makeText(this, "Bienvenue sur la carte !", Toast.LENGTH_SHORT).show();
            // Redirects to your Map Activity
            intent = new Intent(MainActivity.this, Acuielle.class);
        } else {
            // Redirects to Driver selection screen
            Toast.makeText(this, "Bienvenue chauffeur !", Toast.LENGTH_SHORT).show();
            intent = new Intent(MainActivity.this, SelectionCourse.class);
        }
        startActivity(intent);
        finish();
    }

    private void loginServer(String email, String password) {
        loginBtn.setEnabled(false);
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.12.82/waslni/login_taxi.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);

                String data = "email=" + email + "&password=" + password;
                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                String status = json.getString("status");

                if (status.equals("success")) {
                    runOnUiThread(() -> handleRedirection("Taxi Driver"));
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                        loginBtn.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show();
                    loginBtn.setEnabled(true);
                });
            }
        }).start();
    }

    private boolean validateInputs() {
        if (emailInput.getText().toString().isEmpty()) {
            emailInput.setError("Email requis");
            return false;
        }
        if (passwordInput.getText().toString().isEmpty()) {
            passwordInput.setError("Mot de passe requis");
            return false;
        }
        return true;
    }
}