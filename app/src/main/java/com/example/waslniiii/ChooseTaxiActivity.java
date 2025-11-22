package com.example.waslniiii;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class ChooseTaxiActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize osmdroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());


        setContentView(R.layout.activity_choose_taxi);


        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);


        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(35.8256, 10.63699); // Example start
        GeoPoint endPoint = new GeoPoint(35.8320, 10.6425);   // Example end
        mapController.setCenter(startPoint);

        // Add markers and route
        addMarker(startPoint, "Départ");
        addMarker(endPoint, "Destination");
        drawRoute(startPoint, endPoint);
    }

    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

    private void drawRoute(GeoPoint start, GeoPoint end) {
        Polyline line = new Polyline();
        List<GeoPoint> pts = new ArrayList<>();
        pts.add(start);
        pts.add(end);
        line.setPoints(pts);
        line.setWidth(8f);
        line.setColor(0xFF007AFF); // blue line
        mapView.getOverlayManager().add(line);
    }
}
