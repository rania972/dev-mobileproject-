package com.example.waslniiii;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.MapView;
import android.os.Bundle;
import com.example.waslniiii.R;





public class SelectionCourse extends AppCompatActivity {

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obligatoire pour osmdroid
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));

        setContentView(R.layout.selection_course);

        mapView = findViewById(R.id.map_view);

        mapView.setMultiTouchControls(true);

        // Vérifier permission GPS
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }

        // Activer GPS réel / simulé
        GpsMyLocationProvider provider = new GpsMyLocationProvider(this);

        locationOverlay = new MyLocationNewOverlay(provider, mapView);
        locationOverlay.enableMyLocation();       // Active GPS
        locationOverlay.enableFollowLocation();   // Suit ta position
        mapView.getOverlays().add(locationOverlay);

        // Zoom sur ta position quand le GPS devient prêt
        locationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            GeoPoint p = locationOverlay.getMyLocation();
            if (p != null) {
                mapView.getController().setZoom(17.0);
                mapView.getController().animateTo(p);
            }
        }));
    }
}
