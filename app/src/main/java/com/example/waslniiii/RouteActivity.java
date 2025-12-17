package com.example.waslniiii;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class RouteActivity extends AppCompatActivity {

    MapView map;
    EditText etDestination;
    Button btnRoute;
    TextView tvResult;

    GeoPoint startPoint, endPoint;
    Marker startMarker, endMarker;

    FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // Define the default Tunisian center for fallback
    private static final GeoPoint DEFAULT_TUNIS_CENTER = new GeoPoint(36.8065, 10.1815);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this,
                PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_route);

        map = findViewById(R.id.map);
        etDestination = findViewById(R.id.etDestination);
        btnRoute = findViewById(R.id.btnRoute);
        tvResult = findViewById(R.id.tvResult);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        map.getController().setCenter(DEFAULT_TUNIS_CENTER);
        map.getController().setZoom(9.0);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        requestLocation();
        setupMapTap();

        btnRoute.setOnClickListener(v -> getRoute());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume active location tracking if permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop receiving location updates when the app is in the background
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }
    }


    /* 📍 Get current location (FINAL: Active Location Request Logic) */
    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Permission is granted, proceed to setup and start updates
        setupLocationCallbackAndRequest();
        startLocationUpdates();
    }

    private void setupLocationCallbackAndRequest() {
        // Define the request settings
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(10));
        locationRequest.setFastestInterval(TimeUnit.SECONDS.toMillis(5));
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Define the callback logic
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();

                if (location != null && (location.getLatitude() != 0.0 || location.getLongitude() != 0.0)) {
                    // Location found: set startPoint, update UI, and stop updates
                    startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    map.getController().setCenter(startPoint);
                    showMarker(startPoint, true);
                    Toast.makeText(RouteActivity.this, "Localisation GPS actuelle trouvée.", Toast.LENGTH_SHORT).show();

                    // Stop actively hunting for location once we have a fix
                    stopLocationUpdates();
                }
            }
        };

        // Initial Check: Set default location immediately if a real location is not available
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(location -> {
                if (startPoint == null) {
                    if (location != null) {
                        startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    } else {
                        startPoint = DEFAULT_TUNIS_CENTER;
                    }
                    map.getController().setCenter(startPoint);
                    showMarker(startPoint, true);
                    Toast.makeText(this, "Position initiale définie.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationCallback == null || locationRequest == null) {
                setupLocationCallbackAndRequest();
            }
            locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }


    /* 👆 Tap on map to select destination */
    private void setupMapTap() {
        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                endPoint = p;
                showMarker(p, false);
                etDestination.setText(p.getLatitude() + "," + p.getLongitude());
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        map.getOverlays().add(new MapEventsOverlay(receiver));
    }

    /* 📌 Show marker */
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

    /* 🔍 Parse typed destination (Restricted to Tunisia) */
    private GeoPoint parseDestination(String input) {
        try {
            // 1️⃣ Try lat,lon format
            String[] parts = input.split(",");
            if (parts.length == 2) {
                double lat = Double.parseDouble(parts[0].trim());
                double lon = Double.parseDouble(parts[1].trim());

                if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                    return new GeoPoint(lat, lon);
                }
            }

            // 2️⃣ Try Android Geocoder (Bias to Tunisia using Locale)
            Geocoder geocoder = new Geocoder(this, new Locale("fr", "TN"));
            List<Address> results = geocoder.getFromLocationName(input, 1);

            if (results != null && !results.isEmpty()) {
                Address a = results.get(0);
                return new GeoPoint(a.getLatitude(), a.getLongitude());
            }

            // 3️⃣ Fallback: Nominatim (OpenStreetMap, restricted to Tunisia)
            String urlStr =
                    "https://nominatim.openstreetmap.org/search?q="
                            + input.replace(" ", "%20")
                            + "&format=json&limit=1&countrycodes=tn";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "AndroidApp");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) json.append(line);

            JSONArray array = new JSONArray(json.toString());

            if (array.length() > 0) {
                JSONObject obj = array.getJSONObject(0);
                double lat = obj.getDouble("lat");
                double lon = obj.getDouble("lon");
                return new GeoPoint(lat, lon);
            }

        } catch (Exception ignored) {
            // Log the exception here for debugging if needed
        }

        return null;
    }


    /* 🚗 Calculate route and display prices (FINAL ROUTING LOGIC) */
    private void getRoute() {

        if (startPoint == null) {
            runOnUiThread(() -> Toast.makeText(this, "Erreur: Le point de départ est inconnu. Relancez l'app.", Toast.LENGTH_SHORT).show());
            return;
        }

        // Final sanity check: if the start point is the default, inform the user
        if (startPoint.equals(DEFAULT_TUNIS_CENTER)) {
            Toast.makeText(this, "INFO: Calculant à partir du centre de Tunis (Par défaut).", Toast.LENGTH_SHORT).show();
        }

        String input = etDestination.getText().toString().trim();

        if (endPoint == null && input.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Entrez une destination ou tapez sur la carte.", Toast.LENGTH_SHORT).show());
            return;
        }

        // Logic fix: If user types a place name, clear previous destination to force re-geocoding.
        if (!input.isEmpty() && !input.contains(",")) {
            endPoint = null;
        }

        final String[] finalUrl = {null};

        new Thread(() -> {
            try {

                // STEP 1️⃣ Resolve destination if endPoint is null
                if (endPoint == null) {
                    GeoPoint parsed = parseDestination(input);
                    if (parsed == null) {
                        runOnUiThread(() ->
                                Toast.makeText(this, "Adresse introuvable en Tunisie", Toast.LENGTH_LONG).show());
                        return;
                    }

                    endPoint = parsed;
                    runOnUiThread(() -> {
                        showMarker(endPoint, false);
                        map.getController().animateTo(endPoint);
                    });
                }

                if (endPoint == null) return;


                // STEP 2️⃣ Call OSRM
                String urlStr =
                        "https://router.project-osrm.org/route/v1/driving/"
                                + startPoint.getLongitude() + "," + startPoint.getLatitude()
                                + ";"
                                + endPoint.getLongitude() + "," + endPoint.getLatitude()
                                + "?overview=false";

                finalUrl[0] = urlStr;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) json.append(line);

                JSONObject routeJson = new JSONObject(json.toString());

                // Check for OSRM error codes
                if (!routeJson.getString("code").equals("Ok")) {

                    String routeErrorCode;
                    try {
                        routeErrorCode = routeJson.getString("code");
                    } catch (JSONException jsonException) {
                        routeErrorCode = "Erreur de décodage de la réponse OSRM.";
                    }

                    final String finalErrorMessage = routeErrorCode;

                    runOnUiThread(() -> {
                        // OSRM usually returns "NoRoute" if the points are too far or invalid
                        Toast.makeText(this, "OSRM Erreur: " + finalErrorMessage, Toast.LENGTH_LONG).show();
                    });

                    return;
                }

                // Get the first route object
                JSONObject route = routeJson.getJSONArray("routes").getJSONObject(0);

                double distanceKm = route.getDouble("distance") / 1000;
                int durationMin = (int) (route.getDouble("duration") / 60);

                // --- TUNISIAN FARE RATES ---
                double startingFareTND = 0.50;
                double costPerKMTND = 0.80;
                double taxiTND = startingFareTND + distanceKm * costPerKMTND;


                runOnUiThread(() -> tvResult.setText(
                        "📏 Distance: " + String.format("%.2f", distanceKm) + " km\n" +
                                "⏱ Durée: " + durationMin + " min\n\n" +
                                "🚖 Taxi: " + String.format("%.2f", taxiTND) + " DT\n" +
                                "🚌 Bus: 3.50 DT (Exemple)\n" +
                                "🚶 Marche: 0 DT"
                ));

            } catch (Exception e) {
                // If a network or serious parsing error occurs
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Erreur Critique. Code: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // If permission is granted, start the location request setup
            requestLocation();
        } else if (requestCode == 1) {
            // If permission is denied, fallback to the default Tunis center
            startPoint = DEFAULT_TUNIS_CENTER;
            map.getController().setCenter(startPoint);
            showMarker(startPoint, true);
            Toast.makeText(this, "Autorisation de localisation refusée. Utilisant Tunis par défaut.", Toast.LENGTH_LONG).show();
        }
    }
}