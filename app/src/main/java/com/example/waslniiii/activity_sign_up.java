package com.example.waslniiii;// <<<<<<<< DOUBLE CHECK THIS LINE AGAINST YOUR MANIFEST!

import android.content.Intent; // <<< ADDED IMPORT
import android.content.SharedPreferences; 
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class activity_sign_up extends AppCompatActivity {

    private Spinner roleSpinner;
    private LinearLayout extraFields;
    private EditText vehicleNumber, licenseNumber, route;
    private EditText firstName, lastName, phone, email, password;
    private Button btnSignup;
    private ImageView gifView;

    // Key constants for Shared Preferences (Used for local login in MainActivity)
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_ROLE = "role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure R.layout.activity_sign_up is correct
        setContentView(R.layout.activity_sign_up); 

        // Views Initialization - IDs verified to match your XML
        gifView = findViewById(R.id.gifView);
        Glide.with(this).asGif().load(R.drawable.animations).into(gifView);

        roleSpinner = findViewById(R.id.spinner_role);
        extraFields = findViewById(R.id.extraFields);

        // Extra fields
        vehicleNumber = findViewById(R.id.et_vehicle_number);
        licenseNumber = findViewById(R.id.et_license);
        route = findViewById(R.id.et_route);

        // Main user fields
        firstName = findViewById(R.id.et_first_name);
        lastName = findViewById(R.id.et_last_name);
        phone = findViewById(R.id.et_phone);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);

        btnSignup = findViewById(R.id.btn_signup);

        initRoleSelection();

        btnSignup.setOnClickListener(v -> validateForm());
    }

    private void initRoleSelection() {
        String[] roles = {"User", "Taxi Driver", "Bus Driver"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String role = roles[position];

                if (role.equals("User")) {
                    extraFields.setVisibility(View.GONE);
                } else if (role.equals("Taxi Driver")) {
                    extraFields.setVisibility(View.VISIBLE);
                    vehicleNumber.setVisibility(View.VISIBLE);
                    licenseNumber.setVisibility(View.VISIBLE);
                    route.setVisibility(View.GONE);
                } else { // Bus Driver
                    extraFields.setVisibility(View.VISIBLE);
                    vehicleNumber.setVisibility(View.GONE);
                    licenseNumber.setVisibility(View.GONE);
                    route.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void validateForm() {
        // Validation logic is identical to previous versions and is correct.
        String fn = firstName.getText().toString().trim();
        String ln = lastName.getText().toString().trim();
        String ph = phone.getText().toString().trim();
        String em = email.getText().toString().trim();
        String pw = password.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        String nameRegex = "^[a-zA-ZÀ-ÿ\\s]+$";
        String phoneRegex = "^[0-9]+$";

        if (fn.isEmpty() || !fn.matches(nameRegex)) {
            firstName.setError(fn.isEmpty() ? "Prénom requis" : "Le prénom ne doit contenir que des lettres");
            return;
        }
        // ... (other validation checks) ...
        if (ln.isEmpty() || !ln.matches(nameRegex)) {
            lastName.setError(ln.isEmpty() ? "Nom requis" : "Le nom ne doit contenir que des lettres");
            return;
        }
        if (ph.isEmpty() || !ph.matches(phoneRegex) || ph.length() < 8) {
            phone.setError(ph.isEmpty() ? "Numéro requis" : ph.length() < 8 ? "Numéro invalide (min 8 chiffres)" : "Le numéro doit contenir uniquement des chiffres");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
            email.setError("Email invalide");
            return;
        }
        if (pw.length() < 6) {
            password.setError("Mot de passe (min 6 caractères)");
            return;
        }

        String extraField1 = "";
        String extraField2 = "";

        if (role.equals("Taxi Driver")) {
            extraField1 = vehicleNumber.getText().toString().trim();
            extraField2 = licenseNumber.getText().toString().trim();
            if (extraField1.isEmpty()) { vehicleNumber.setError("Numéro du véhicule requis"); return; }
            if (extraField2.isEmpty()) { licenseNumber.setError("Numéro de licence requis"); return; }
        }
        if (role.equals("Bus Driver")) {
            extraField1 = route.getText().toString().trim(); 
            if (extraField1.isEmpty()) { route.setError("Ligne requise"); return; }
        }

        // Data Storage (Shared Preferences)
        try {
            SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putString("firstName", fn);
            editor.putString("lastName", ln);
            editor.putString("phone", ph);
            editor.putString(KEY_EMAIL, em);
            editor.putString(KEY_PASSWORD, pw); 
            editor.putString(KEY_ROLE, role);

            if (role.equals("Taxi Driver")) {
                editor.putString("vehicleNumber", extraField1);
                editor.putString("licenseNumber", extraField2);
            } else if (role.equals("Bus Driver")) {
                editor.putString("route", extraField1);
            }

            editor.apply();

            Toast.makeText(this, "Inscription réussie ✅ Redirection vers la page de connexion.", Toast.LENGTH_LONG).show();

            // Navigate to MainActivity (Login Screen) and pass credentials
            // Assuming your Login Activity is named MainActivity.class
            Intent intent = new Intent(activity_sign_up.this, MainActivity.class); 
            intent.putExtra("PREFILL_EMAIL", em); 
            intent.putExtra("PREFILL_PASSWORD", pw); 
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Erreur de sauvegarde: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}