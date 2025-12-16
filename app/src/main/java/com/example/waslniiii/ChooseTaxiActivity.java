package com.example.waslniiii;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler; // NÉCESSAIRE pour simuler l'attente
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// Imports spécifiques à osmdroid
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

// Import Explicite de la classe R
import com.example.waslniiii.R;


public class ChooseTaxiActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MapView mapView;
    private LocationManager locationManager;

    // VARIABLES D'ÉTAT
    private GeoPoint userLocation = null;
    private GeoPoint destinationLocation = null;
    private Marker selectedTaxiMarker = null;

    // ÉLÉMENTS D'INTERFACE ET DE CARTE
    private Marker userMarker;
    private Marker destinationMarker;
    private Polyline routePolyline;
    private final List<Marker> taxiMarkers = new ArrayList<>();
    private Button btnConfirmBooking;
    private View tripInfoCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialisation de la configuration osmdroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_choose_taxi);

        // 2. Configuration des vues et de la carte
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        tripInfoCard = findViewById(R.id.tripInfoCard);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        // Initialiser l'état (caché au départ)
        tripInfoCard.setVisibility(View.GONE);
        btnConfirmBooking.setVisibility(View.GONE);

        // 3. Initialiser les marqueurs et la ligne
        userMarker = new Marker(mapView);
        destinationMarker = new Marker(mapView);
        routePolyline = new Polyline();
        mapView.getOverlayManager().add(userMarker);
        mapView.getOverlayManager().add(destinationMarker);
        mapView.getOverlayManager().add(routePolyline);

        // 4. Configurer la recherche de destination (Simulée)
        setupSearchListener();

        // 5. Demander les permissions de localisation et démarrer les updates
        requestLocationPermissions();
    }

    // --- 1. GESTION DE LA LOCALISATION ET DES PERMISSIONS ---

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Localisation non permise. Utilisation d'un point de départ fixe.", Toast.LENGTH_LONG).show();
                userLocation = new GeoPoint(35.8256, 10.63699); // Point par défaut
                updateMapElements();
                simulateCloseTaxis();
            }
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastKnownLocation != null) {
                userLocation = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            } else if (userLocation == null) {
                userLocation = new GeoPoint(35.8256, 10.63699);
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, this);

            updateMapElements();
            simulateCloseTaxis();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        updateMapElements();
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(@NonNull String provider) {}
    @Override public void onProviderDisabled(@NonNull String provider) {}

    // --- 2. MISE À JOUR DE LA CARTE ET DES MARQUEURS ---

    private void updateMapElements() {
        if (userLocation == null) return;

        updateMarker(userMarker, userLocation, "Votre Position", R.drawable.ic_user_location);

        IMapController mapController = mapView.getController();
        mapController.animateTo(userLocation);

        if (destinationLocation != null) {
            updateMarker(destinationMarker, destinationLocation, "Destination", R.drawable.ic_destination);
            drawRoute(userLocation, destinationLocation);
        } else {
            destinationMarker.setVisible(false);
            routePolyline.setPoints(new ArrayList<>());
        }

        mapView.invalidate();
    }

    private void updateMarker(Marker marker, GeoPoint point, String title, int iconResId) {
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setIcon(ContextCompat.getDrawable(this, iconResId));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setVisible(true);
    }

    private void drawRoute(GeoPoint start, GeoPoint end) {
        List<GeoPoint> pts = new ArrayList<>();
        pts.add(start);
        pts.add(end);

        routePolyline.setPoints(pts);
        routePolyline.setWidth(8f);
        routePolyline.setColor(0xFF007AFF);
    }

    // --- 3. LOGIQUE DE RECHERCHE ÉTENDUE ET MISE À JOUR DU TRAJET ---

    private void updateTripInfoCard(String distance, String duration, String price, String type, String title) {
        ((TextView) findViewById(R.id.tvTripTitle)).setText(title);
        ((TextView) findViewById(R.id.tvDistance)).setText("Estimated route: " + distance + " – " + duration);
        ((TextView) findViewById(R.id.tvPrice)).setText("Estimated price: " + price);
        ((TextView) findViewById(R.id.tvType)).setText("Type: " + type);

        tripInfoCard.setVisibility(View.VISIBLE);
        btnConfirmBooking.setVisibility(View.GONE);
    }

    private void setupSearchListener() {
        EditText etSearch = findViewById(R.id.etSearch);

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().toLowerCase();

                String distance, duration, price;
                String defaultTitle = "🛺 Estimated trip details";

                // LOGIQUE DE SIMULATION DES COORDONNÉES ET DES DÉTAILS
                if (query.contains("sousse") || query.contains("riad")) {
                    destinationLocation = new GeoPoint(35.8300, 10.6300); // Sousse centre
                    distance = "3.5 km";
                    duration = "10 minutes";
                    price = "~3,000 TND";
                } else if (query.contains("aeroport") || query.contains("monastir")) {
                    destinationLocation = new GeoPoint(35.7600, 10.7540); // Aéroport Monastir
                    distance = "15 km";
                    duration = "25 minutes";
                    price = "~12,000 TND";
                } else if (query.contains("tunis") || query.contains("centre ville")) {
                    destinationLocation = new GeoPoint(36.8065, 10.1815); // Tunis Centre
                    distance = "5 km";
                    duration = "15 minutes";
                    price = "~6,000 TND";
                } else if (query.contains("gabes") || query.contains("medina")) {
                    destinationLocation = new GeoPoint(33.8833, 10.0900); // Gabes centre
                    distance = "4 km";
                    duration = "10 minutes";
                    price = "~4,500 TND";
                } else {
                    // Destination par défaut ou non reconnue
                    destinationLocation = new GeoPoint(35.8320, 10.6425);
                    distance = "6.5 km";
                    duration = "12 minutes";
                    price = "~8,500 TND";
                }

                updateTripInfoCard(distance, duration, price, "🚖 Standard Taxi", defaultTitle);
                updateMapElements();

                Toast.makeText(this, "Destination trouvée. Choisissez votre taxi.", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    // --- 4. SIMULATION DES TAXIS ET SÉLECTION AVEC ACCEPTATION ---

    private void simulateCloseTaxis() {
        GeoPoint basePoint = (userLocation != null) ? userLocation : new GeoPoint(35.8256, 10.63699);

        for (Marker marker : taxiMarkers) {
            mapView.getOverlays().remove(marker);
        }
        taxiMarkers.clear();

        List<GeoPoint> taxiPoints = new ArrayList<>();
        taxiPoints.add(new GeoPoint(basePoint.getLatitude() + 0.0005, basePoint.getLongitude() - 0.001));
        taxiPoints.add(new GeoPoint(basePoint.getLatitude() - 0.001, basePoint.getLongitude() + 0.0008));
        taxiPoints.add(new GeoPoint(basePoint.getLatitude() + 0.0015, basePoint.getLongitude() + 0.0003));

        for (int i = 0; i < taxiPoints.size(); i++) {
            final Marker taxiMarker = new Marker(mapView);
            taxiMarker.setPosition(taxiPoints.get(i));
            taxiMarker.setTitle("Taxi " + (i + 1));
            taxiMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.icon_taxi));
            taxiMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(taxiMarker);
            taxiMarkers.add(taxiMarker);

            taxiMarker.setOnMarkerClickListener((marker, map) -> {
                handleTaxiSelection(marker);
                return true;
            });
        }

        mapView.invalidate();
    }

    private void handleTaxiSelection(Marker newSelection) {
        // 1. Réinitialiser l'ancien marqueur sélectionné
        if (selectedTaxiMarker != null) {
            selectedTaxiMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.icon_taxi));
            selectedTaxiMarker.setAlpha(1.0f);
        }

        // 2. Définir la nouvelle sélection
        selectedTaxiMarker = newSelection;
        selectedTaxiMarker.setAlpha(0.6f);

        // 3. Afficher l'état de proximité et le bouton de confirmation
        displayTaxiProximity(newSelection);
        mapView.invalidate();
    }

    private void displayTaxiProximity(Marker taxi) {
        double distanceKm = taxiMarkers.indexOf(taxi) * 0.5 + 0.5;
        int timeMin = (int) (distanceKm * 2);

        String proximityStatus;

        if (distanceKm < 1.0) {
            proximityStatus = "✅ Taxi " + taxi.getTitle() + " : " + timeMin + " min (Très Proche)";
        } else {
            proximityStatus = "⏳ Taxi " + taxi.getTitle() + " : " + timeMin + " min (Arrivée estimée)";
        }

        // Mettre à jour le titre de la carte d'information
        ((TextView) findViewById(R.id.tvTripTitle)).setText(proximityStatus);

        // Afficher le bouton de confirmation
        btnConfirmBooking.setVisibility(View.VISIBLE);
        btnConfirmBooking.setText("Confirmer la réservation (" + taxi.getTitle() + ")");
        btnConfirmBooking.setEnabled(true);

        // LOGIQUE D'ACCEPTATION SIMULÉE
        btnConfirmBooking.setOnClickListener(v -> {
            btnConfirmBooking.setText("⏳ Envoi de la demande...");
            btnConfirmBooking.setEnabled(false);

            ((TextView) findViewById(R.id.tvTripTitle)).setText("⏳ Attente de l'acceptation du taxi...");

            // Simuler l'acceptation après 3 secondes
            new Handler().postDelayed(() -> {
                boolean accepted = true; // Simulation: Le taxi accepte

                if (accepted) {
                    Toast.makeText(this, taxi.getTitle() + " a accepté la course ! Le taxi est en route.", Toast.LENGTH_LONG).show();
                    ((TextView) findViewById(R.id.tvTripTitle)).setText("✅ Course acceptée. Le taxi arrive !");
                    btnConfirmBooking.setText("Voyage en cours...");
                } else {
                    Toast.makeText(this, "Le taxi n'a pas répondu. Veuillez en choisir un autre.", Toast.LENGTH_LONG).show();
                    ((TextView) findViewById(R.id.tvTripTitle)).setText("❌ Demande refusée. Choisissez un autre taxi.");
                    btnConfirmBooking.setText("Choisir un autre taxi");
                    btnConfirmBooking.setEnabled(true);
                }
            }, 3000);
        });
    }

    // --- 5. GESTION DU CYCLE DE VIE ---

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && locationManager != null) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}
