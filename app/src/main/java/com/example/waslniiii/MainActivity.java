package com.example.waslniiii;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // GIF
        ImageView gifView = findViewById(R.id.gifView);
        Glide.with(this).asGif().load(R.drawable.animations).into(gifView);

        // Login views
        emailInput = findViewById(R.id.et_email);
        passwordInput = findViewById(R.id.et_motpasse);
        loginBtn = findViewById(R.id.button);

        loginBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                loginTaxi();
            }
        });
    }

    private boolean validateInputs() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Vérifier si l'email est vide
        if (email.isEmpty()) {
            emailInput.setError("L'email est obligatoire");
            emailInput.requestFocus();
            return false;
        }

        // Vérifier le format de l'email (sauf si contient "client" ou "taxi")
        if (!email.toLowerCase().contains("client") &&
                !email.toLowerCase().contains("taxi") &&
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Format d'email invalide");
            emailInput.requestFocus();
            return false;
        }

        // Vérifier si le mot de passe est vide
        if (password.isEmpty()) {
            passwordInput.setError("Le mot de passe est obligatoire");
            passwordInput.requestFocus();
            return false;
        }

        // Vérifier la longueur minimale du mot de passe (sauf si contient "client" ou "taxi")
        if (!password.toLowerCase().contains("client") &&
                !password.toLowerCase().contains("taxi") &&
                password.length() < 6) {
            passwordInput.setError("Le mot de passe doit contenir au moins 6 caractères");
            passwordInput.requestFocus();
            return false;
        }

        return true;
    }

    private void loginTaxi() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // ✅ VÉRIFICATION MOT "CLIENT" - Redirection directe vers Acuielle
        if (email.toLowerCase().contains("client") || password.toLowerCase().contains("client")) {
            Toast.makeText(MainActivity.this, "Bienvenue client !", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this, Acuielle.class);
            startActivity(i);
            finish();
            return; // Arrêter l'exécution ici
        }

        // ✅ VÉRIFICATION MOT "TAXI" - Redirection directe vers SelectionCourse
        if (email.toLowerCase().contains("taxi") || password.toLowerCase().contains("taxi")) {
            Toast.makeText(MainActivity.this, "Bienvenue chauffeur !", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this, SelectionCourse.class);
            startActivity(i);
            finish();
            return; // Arrêter l'exécution ici
        }

        // Désactiver le bouton pendant la connexion
        loginBtn.setEnabled(false);

        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.12.82/waslni/login_taxi.php");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000); // Timeout de 10 secondes
                conn.setReadTimeout(10000);

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
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Bienvenue chauffeur !", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MainActivity.this, SelectionCourse.class);
                        startActivity(i);
                        finish(); // Fermer l'activité de connexion
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                        loginBtn.setEnabled(true); // Réactiver le bouton
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show();
                    loginBtn.setEnabled(true); // Réactiver le bouton
                });
            }
        }).start();
    }
}