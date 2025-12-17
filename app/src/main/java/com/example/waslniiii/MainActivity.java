package com.example.waslniiii;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView gifView = findViewById(R.id.gifView);
        Glide.with(this).asGif().load(R.drawable.animations).into(gifView);

        emailInput = findViewById(R.id.et_email);
        passwordInput = findViewById(R.id.et_motpasse);
        loginBtn = findViewById(R.id.button);
        signupLink = findViewById(R.id.tv_go_to_signup);

        loginBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                performLogin();
            }
        });

        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, activity_sign_up.class);
            startActivity(intent);
        });

        if (getIntent().hasExtra("PREFILL_EMAIL")) {
            emailInput.setText(getIntent().getStringExtra("PREFILL_EMAIL"));
            passwordInput.setText(getIntent().getStringExtra("PREFILL_PASSWORD"));
        }
    }

    private void performLogin() {
        String email = emailInput.getText().toString().trim().toLowerCase();
        String password = passwordInput.getText().toString().trim();

        // 1. CHECK SAVED ACCOUNTS (From Sign Up)
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("email", "").toLowerCase();
        String savedPw = prefs.getString("password", "");
        String savedRole = prefs.getString("role", "");

        if (email.equals(savedEmail) && password.equals(savedPw)) {
            handleRedirection(savedRole);
            return;
        }

        // 2. SHORTCUT LOGIC (For testing)
        if (email.contains("client")) {
            handleRedirection("User");
            return;
        }

        if (email.contains("taxi")) {
            handleRedirection("Taxi Driver");
            return;
        }

        // ✅ ADDED: BUS DRIVER SHORTCUT
        if (email.contains("bus") || email.contains("chauffeur")) {
            handleRedirection("Bus Driver");
            return;
        }

        // 3. SERVER LOGIN
        loginServer(email, password);
    }

    private void handleRedirection(String role) {
        Intent intent;

        // Redirect based on the role string
        if (role.equalsIgnoreCase("User") || role.equalsIgnoreCase("client")) {
            Toast.makeText(this, "Bienvenue Passager !", Toast.LENGTH_SHORT).show();
            intent = new Intent(MainActivity.this, Acuielle.class);

        } else if (role.equalsIgnoreCase("Bus Driver")) {
            // ✅ REDIRECT TO YOUR NEW BUS DRIVER ACTIVITY
            Toast.makeText(this, "Session Chauffeur Bus Active", Toast.LENGTH_SHORT).show();
            intent = new Intent(MainActivity.this, BusDriverActivity.class);

        } else {
            // Default for Taxi Drivers or others
            Toast.makeText(this, "Bienvenue chauffeur Taxi !", Toast.LENGTH_SHORT).show();
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
                    // You can check the role from JSON here if your PHP supports it
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
                    Toast.makeText(MainActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
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