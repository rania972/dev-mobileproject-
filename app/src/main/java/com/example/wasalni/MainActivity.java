package com.example.wasalni;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    public static class Bus {
        public String number, route, time, status, stops, duration;
        public int color;
        public double lat, lon;
        public List<GeoPoint> routePoints;
        public Bus(String number, String route, String time, String status, int color,
                   String stops, String duration, double lat, double lon, List<GeoPoint> routePoints) {
            this.number = number;
            this.route = route;
            this.time = time;
            this.status = status;
            this.color = color;
            this.stops = stops;
            this.duration = duration;
            this.lat = lat;
            this.lon = lon;
            this.routePoints = routePoints;
        }
    }

    private List<Bus> allBuses;
    private List<Bus> displayedBuses;
    private BusAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Configuration.getInstance().setUserAgentValue(getPackageName());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Real Sousse route coordinates (sample)
        List<GeoPoint> sahloulRoute = Arrays.asList(
                new GeoPoint(35.824822, 10.596491), // Sahloul
                new GeoPoint(35.826200, 10.616969), // Jawhara
                new GeoPoint(35.825638, 10.632137), // Medina
                new GeoPoint(35.830538, 10.631188)  // Centre Ville
        );
        List<GeoPoint> kaleaRoute = Arrays.asList(
                new GeoPoint(35.830294, 10.582848), // Kalea
                new GeoPoint(35.825295, 10.610188), // Ennaser
                new GeoPoint(35.821177, 10.627172)  // Kebili
        );
        List<GeoPoint> sidibouRoute = Arrays.asList(
                new GeoPoint(35.817275, 10.634677), // Sidi Bou
                new GeoPoint(35.820749, 10.646229), // Amir
                new GeoPoint(35.824134, 10.657819)  // Borj
        );

        allBuses = Arrays.asList(
                new Bus("12", "Sahloul → Centre-ville", "10:05", "À l'heure", Color.parseColor("#4CAF50"),
                        "Sahloul, Jawhara, Medina, Centre Ville", "25 min", 35.824822, 10.596491, sahloulRoute),
                new Bus("22B", "Kalea → Sousse", "10:15", "Retard (5 min)", Color.parseColor("#FF9800"),
                        "Kalea, Ennaser, Kebili", "35 min", 35.830294, 10.582848, kaleaRoute),
                new Bus("44", "Sidi Bou → Jawhara", "10:30", "Supprimé", Color.parseColor("#F44336"),
                        "Sidi Bou, Amir, Borj", "40 min", 35.817275, 10.634677, sidibouRoute)
        );
        displayedBuses = new ArrayList<>(allBuses);

        Spinner locationSpinner = findViewById(R.id.locationSpinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Tous", "Sahloul", "Kalea", "Sidi Bou", "Centre-ville", "Ennaser"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(spinnerAdapter);

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = parent.getItemAtPosition(pos).toString();
                filterBuses(selected);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        RecyclerView busList = findViewById(R.id.busList);
        busList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BusAdapter(displayedBuses);
        busList.setAdapter(adapter);
    }

    private void filterBuses(String selected) {
        displayedBuses.clear();
        if (selected.equals("Tous")) {
            displayedBuses.addAll(allBuses);
        } else {
            for (Bus b : allBuses) {
                if (b.route.contains(selected) || b.stops.contains(selected)) {
                    displayedBuses.add(b);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    class BusAdapter extends RecyclerView.Adapter<BusAdapter.BusHolder> {
        private List<Bus> data;
        BusAdapter(List<Bus> d) { data=d; }

        @Override
        public BusHolder onCreateViewHolder(ViewGroup p, int v) {
            View row = LayoutInflater.from(p.getContext()).inflate(R.layout.bus_list_item, p, false);
            return new BusHolder(row);
        }
        @Override
        public void onBindViewHolder(BusHolder h, int i) {
            Bus b = data.get(i);
            h.busNumber.setText(b.number);
            h.busRoute.setText(b.route);
            h.busTime.setText(b.time);
            h.busStatus.setText(b.status);
            h.busStatus.setTextColor(b.color);

            // Show bus photo in the icon
            h.busIcon.setImageResource(R.drawable.ic_bus); // your bus image file
            h.busIcon.clearColorFilter(); // removes any unwanted coloring

            h.busStops.setText("Arrêts: "+b.stops);
            h.busDuration.setText("Durée: "+b.duration);
            h.busDetails.setVisibility(View.GONE);
            h.busMap.setVisibility(View.GONE);

            h.itemView.setOnClickListener(v -> {
                boolean show = h.busDetails.getVisibility() != View.VISIBLE;
                h.busDetails.setVisibility(show?View.VISIBLE:View.GONE);
                h.busMap.setVisibility(show?View.VISIBLE:View.GONE);

                if(show) {
                    h.busMap.setTileSource(TileSourceFactory.MAPNIK);
                    h.busMap.getController().setZoom(14);
                    h.busMap.getOverlayManager().clear();
                    h.busMap.getController().setCenter(new GeoPoint(b.lat, b.lon));

                    if (b.routePoints != null && !b.routePoints.isEmpty()) {
                        Polyline line = new Polyline();
                        line.setPoints(b.routePoints);

                        // Style the line using getPaint()
                        Paint paint = line.getPaint();
                        paint.setColor(Color.parseColor("#1976D2"));
                        paint.setStrokeWidth(14f);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setPathEffect(new android.graphics.DashPathEffect(new float[]{40, 20}, 0));
                        paint.setAlpha(210);
                        paint.setAntiAlias(true);

                        h.busMap.getOverlayManager().add(line);
                    }
                }
            });
        }
        @Override
        public int getItemCount() { return data.size(); }
        class BusHolder extends RecyclerView.ViewHolder {
            TextView busNumber, busRoute, busTime, busStatus, busStops, busDuration;
            ImageView busIcon;
            LinearLayout busDetails;
            MapView busMap;
            BusHolder(View v) {
                super(v);
                busNumber = v.findViewById(R.id.busNumber);
                busRoute = v.findViewById(R.id.busRoute);
                busTime = v.findViewById(R.id.busTime);
                busStatus = v.findViewById(R.id.busStatus);
                busStops = v.findViewById(R.id.busStops);
                busDuration = v.findViewById(R.id.busDuration);
                busIcon = v.findViewById(R.id.busIcon);
                busDetails = v.findViewById(R.id.busDetails);
                busMap = v.findViewById(R.id.busMap);
            }
        }
    }
}