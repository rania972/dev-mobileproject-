package com.example.waslniiii;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.List;


public class RouteActivity extends AppCompatActivity {

    MapView map;
    EditText etDestination;
    TextView tvResult;

    GeoPoint startPoint, endPoint;
    Marker startMarker, endMarker;

    FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final GeoPoint DEFAULT_TUNIS_CENTER = new GeoPoint(36.8065, 10.1815);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Modern, robust osmdroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setOsmdroidBasePath(getCacheDir());
        Configuration.getInstance().setOsmdroidTileCache(new File(getCacheDir(), "osmdroid"));

        setContentView(R.layout.activity_route);

        map = findViewById(R.id.map);
        etDestination = findViewById(R.id.etDestination);
        tvResult = findViewById(R.id.tvResult);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        map.getController().setCenter(DEFAULT_TUNIS_CENTER);
        map.getController().setZoom(9.0);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        requestLocation();
        setupMapTap();

        etDestination.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                getRoute();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (locationClient != null && locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }
    }


    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        setupLocationCallbackAndRequest();
        startLocationUpdates();
    }

    private void setupLocationCallbackAndRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    map.getController().animateTo(startPoint);
                    showMarker(startPoint, true);
                    Toast.makeText(RouteActivity.this, "Localisation GPS actuelle trouvée.", Toast.LENGTH_SHORT).show();
                    stopLocationUpdates();
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (startPoint == null) {
                if (location != null) {
                    startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                } else {
                    startPoint = DEFAULT_TUNIS_CENTER;
                    Toast.makeText(this, "Recherche de la position en cours...", Toast.LENGTH_LONG).show();
                }
                map.getController().setCenter(startPoint);
                showMarker(startPoint, true);
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationCallback == null || locationRequest == null) {
                setupLocationCallbackAndRequest();
            }
            locationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    private void setupMapTap() {
        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                endPoint = p;
                showMarker(p, false);
                etDestination.setText(String.format(Locale.US, "%.6f,%.6f", p.getLatitude(), p.getLongitude()));
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) { return false; }
        };
        map.getOverlays().add(new MapEventsOverlay(receiver));
    }

    private void showMarker(GeoPoint point, boolean isStart) {
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(isStart ? "Départ" : "Destination");

        if (isStart) {
            if (startMarker != null) map.getOverlays().remove(startMarker);
            startMarker = marker;
        } else {
            if (endMarker != null) map.getOverlays().remove(endMarker);
            endMarker = marker;
        }

        map.getOverlays().add(marker);
        map.invalidate();
    }

    private GeoPoint parseDestination(String input) {
        try {
            String[] parts = input.split(",");
            if (parts.length == 2) {
                return new GeoPoint(Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()));
            }

            Geocoder geocoder = new Geocoder(this, new Locale("fr", "TN"));
            List<Address> results = geocoder.getFromLocationName(input, 1, 33.1, 7.5, 37.6, 11.7); // Tunisia bounding box
            if (results != null && !results.isEmpty()) {
                return new GeoPoint(results.get(0).getLatitude(), results.get(0).getLongitude());
            }

        } catch (Exception ignored) {}
        return null;
    }

    private void getRoute() {
        if (startPoint == null) {
            Toast.makeText(this, "Le point de départ est inconnu.", Toast.LENGTH_SHORT).show();
            return;
        }

        String input = etDestination.getText().toString().trim();
        if (endPoint == null && input.isEmpty()) {
            Toast.makeText(this, "Entrez une destination.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!input.isEmpty() && !input.contains(",")) {
            endPoint = null;
        }

        new Thread(() -> {
            try {
                GeoPoint finalDestination = endPoint;
                if (finalDestination == null) {
                    finalDestination = parseDestination(input);
                }

                if (finalDestination == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Adresse introuvable.", Toast.LENGTH_LONG).show());
                    return;
                }

                final GeoPoint capturedDest = finalDestination;
                runOnUiThread(() -> showMarker(capturedDest, false));

                URL url = new URL(String.format(Locale.US,
                        "https://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=false",
                        startPoint.getLongitude(), startPoint.getLatitude(),
                        capturedDest.getLongitude(), capturedDest.getLatitude()));

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) json.append(line);

                JSONObject routeJson = new JSONObject(json.toString());
                // Safely check for the OSRM response code to prevent crashes
                String osrmCode = routeJson.optString("code", "Unknown");
                if (!"Ok".equals(osrmCode)) {
                    runOnUiThread(() -> Toast.makeText(this, "Erreur OSRM: " + osrmCode, Toast.LENGTH_LONG).show());
                    return;
                }

                JSONObject route = routeJson.getJSONArray("routes").getJSONObject(0);
                double distanceKm = route.getDouble("distance") / 1000;
                int durationMin = (int) (route.getDouble("duration") / 60);

                double taxiTND = 0.50 + distanceKm * 0.80;

                runOnUiThread(() -> tvResult.setText(
                        String.format(Locale.US, "Distance: %.2f km\nDurée: %d min\n\nTaxi: %.2f DT",
                                distanceKm, durationMin, taxiTND)
                ));

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                startPoint = DEFAULT_TUNIS_CENTER;
                map.getController().setCenter(startPoint);
                showMarker(startPoint, true);
                Toast.makeText(this, "Autorisation refusée.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
