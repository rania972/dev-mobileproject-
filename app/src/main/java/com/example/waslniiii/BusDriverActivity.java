package com.example.waslniiii;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class BusDriverActivity extends AppCompatActivity {

    private LinearLayout layoutSelection;
    private CardView layoutDashboard;
    private MapView map;
    private TextView tvCurrentTrip, tvBookedCount, tvEmptyCount;

    // Constant total capacity of the bus
    private final int TOTAL_SEATS = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. CONFIGURATION ---
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_bus_driver);

        // --- 2. INITIALIZE VIEWS ---
        layoutSelection = findViewById(R.id.layoutSelection);
        layoutDashboard = findViewById(R.id.layoutDashboard);
        map = findViewById(R.id.driverMap);
        tvCurrentTrip = findViewById(R.id.tvCurrentTrip);
        tvBookedCount = findViewById(R.id.tvBookedCount);
        tvEmptyCount = findViewById(R.id.tvEmptyCount);

        Button btnRetard = findViewById(R.id.btnRetard);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // --- 3. MAP SETUP ---
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        // --- 4. NAVIGATION / BACK BUTTON ---
        btnBack.setOnClickListener(v -> {
            if (layoutSelection.getVisibility() == View.GONE) {
                // If driving, go back to trip selection
                layoutSelection.setVisibility(View.VISIBLE);
                layoutDashboard.setVisibility(View.GONE);
                map.getOverlays().clear(); // Clear the route line
                map.invalidate();
            } else {
                // If already at selection, close the activity
                finish();
            }
        });

        // --- 5. TRIP SELECTION LOGIC ---
        findViewById(R.id.trip1).setOnClickListener(v -> startService("Ligne 12: Sahloul", 18));
        findViewById(R.id.trip2).setOnClickListener(v -> startService("Ligne 22B: Kalea", 32));

        // --- 6. SIGNALER RETARD LOGIC (THE FIX) ---
        btnRetard.setOnClickListener(v -> {
            String[] options = {"5 Minutes", "15 Minutes", "30 Minutes", "Trajet Annulé"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Signaler un retard");
            builder.setItems(options, (dialog, which) -> {
                String choice = options[which];

                // Update Driver UI
                Toast.makeText(this, "Statut mis à jour : " + choice, Toast.LENGTH_LONG).show();
                tvCurrentTrip.setText("RETARD : " + choice);
                tvCurrentTrip.setTextColor(Color.RED);

                // In a real app, you would send 'choice' to your server/Firebase here
            });
            builder.show();
        });
    }

    /**
     * Prepares the dashboard and draws the bus route
     */
    private void startService(String tripName, int bookedSeats) {
        layoutSelection.setVisibility(View.GONE);
        layoutDashboard.setVisibility(View.VISIBLE);

        // Reset Text Colors and Labels
        tvCurrentTrip.setText(tripName);
        tvCurrentTrip.setTextColor(Color.parseColor("#212121"));

        // Update Reservation Stats
        tvBookedCount.setText(String.valueOf(bookedSeats));
        tvEmptyCount.setText(String.valueOf(TOTAL_SEATS - bookedSeats));

        drawTrajectLine();
    }

    /**
     * Draws the blue line on the map representing the bus path
     */
    private void drawTrajectLine() {
        Polyline line = new Polyline();
        line.getOutlinePaint().setColor(Color.parseColor("#1976D2")); // Professional Blue
        line.getOutlinePaint().setStrokeWidth(12f);

        // Sousse Area Example Coordinates
        List<GeoPoint> pts = new ArrayList<>();
        pts.add(new GeoPoint(35.8248, 10.5964));
        pts.add(new GeoPoint(35.8262, 10.6170));
        pts.add(new GeoPoint(35.8256, 10.6321));

        line.setPoints(pts);
        map.getOverlays().add(line);

        // Zoom and Move to Start
        map.getController().animateTo(pts.get(0));
        map.invalidate(); // Refresh the map view
    }

    // --- LIFECYCLE MANAGEMENT ---
    @Override
    protected void onResume() {
        super.onResume();
        if(map != null) map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(map != null) map.onPause();
    }
}