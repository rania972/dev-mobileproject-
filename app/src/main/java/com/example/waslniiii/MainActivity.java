package com.example.waslniiii;

import android.content.Intent;
import android.os.Bundle;
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

        loginBtn.setOnClickListener(v -> loginTaxi());
    }

    private void loginTaxi() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

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

                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Bienvenue chauffeur !", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MainActivity.this, SelectionCourse.class);
                        startActivity(i);
                    });

                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
