package com.example.waslniiii;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class Acuielle extends AppCompatActivity {

    private MapView map;
    private MyLocationNewOverlay locationOverlay;
    private static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ CONFIGURATION OSMDROID
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.acuielle);

        // --- MAP INITIALIZATION ---
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // ❌ Hide Zoom Buttons
        map.setBuiltInZoomControls(false);
        map.getZoomController().setVisibility(
                org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER
        );

        // ✅ Set initial zoom level
        map.getController().setZoom(15.0);

        // --- NAVIGATION LOGIC ---

        // 1. Mon Itinéraire (button10) -> Open RouteActivity
        Button btnItineraire = findViewById(R.id.button10);
        btnItineraire.setOnClickListener(v -> {
            Intent intent = new Intent(Acuielle.this, RouteActivity.class);
            startActivity(intent);
        });

        // 2. Trouver un Taxi (button2) -> Open choose_taxi
        Button btnTrouverTaxi = findViewById(R.id.button2);
        btnTrouverTaxi.setOnClickListener(v -> {
            // Updated to use the correct class name for your taxi selection
            Intent intent = new Intent(Acuielle.this, ChooseTaxiActivity.class);
            startActivity(intent);
        });

        // 3. Historique (button8) -> Open history
        Button btnHistorique = findViewById(R.id.button8);
        btnHistorique.setOnClickListener(v -> {
            Intent intent = new Intent(Acuielle.this, history.class);
            startActivity(intent);
        });

        // 4. Lignes de Bus (button9) -> Open BusListActivity
        Button btnBus = findViewById(R.id.button9);
        btnBus.setOnClickListener(v -> {
            Intent intent = new Intent(Acuielle.this, BusListActivity.class);
            startActivity(intent);
        });


        // --- LOCATION PERMISSION CHECK ---
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            activerLocalisation();
        }
    }

    private void activerLocalisation() {
        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();

        map.getOverlays().add(locationOverlay);

        locationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            IGeoPoint myPos = locationOverlay.getMyLocation();
            if (myPos != null) {
                map.getController().setZoom(16.0);
                map.getController().animateTo((GeoPoint) myPos);
            }
        }));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            activerLocalisation();
        } else {
            Toast.makeText(this, "Permission de localisation requise pour la carte", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (map != null) {
            map.onDetach();
        }
    }
}