package com.example.waslniiii;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Static-only History screen (no API/DB).
 * - Header with filters (Mois / Type / Destination)
 * - RecyclerView with programmatic item cards (no extra layout file)
 * - Tap a card to select it
 * - "REFARE CE TRAJET" duplicates the selected trip with current timestamp/month (in-memory)
 */
public class history extends AppCompatActivity {

    private RecyclerView rvTrips;
    private HistoryAdapter adapter;

    // In-memory data (static)
    private final List<Trip> allTrips = new ArrayList<>();
    private final List<Trip> visibleTrips = new ArrayList<>();

    // Filters
    private String currentMois = null;
    private String currentType = null;
    private String currentDestination = null;

    // Current selection
    private Trip selectedTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.history);

        // Seed demo trips, then show them
        seedTrips();
        visibleTrips.addAll(allTrips);

        // RecyclerView
        rvTrips = findViewById(R.id.rvTrips);
        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(visibleTrips, t -> {
            selectedTrip = t;
            Toast.makeText(this, "Trajet sélectionné", Toast.LENGTH_SHORT).show();
        });
        rvTrips.setAdapter(adapter);

        // Filters + repeat button
        setupFilters();
        setupRepeatButton();
    }

    // Button: duplicate selected trip with current datetime/month
    private void setupRepeatButton() {
        Button btnRefaire = findViewById(R.id.btnRefaireTrajet);
        btnRefaire.setOnClickListener(v -> {
            if (selectedTrip == null) {
                Toast.makeText(this, "Sélectionnez un trajet", Toast.LENGTH_SHORT).show();
                return;
            }

            Trip newTrip = new Trip();

            // Current timestamp (MySQL-like DATETIME)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            newTrip.date_time = sdf.format(new Date());

            // Current month name in French (capitalize first letter)
            String[] moisNames = new DateFormatSymbols(Locale.FRENCH).getMonths();
            Calendar cal = Calendar.getInstance();
            String moisNow = moisNames[cal.get(Calendar.MONTH)];
            if (moisNow != null && moisNow.length() > 0) {
                moisNow = Character.toUpperCase(moisNow.charAt(0)) + moisNow.substring(1);
            }
            newTrip.mois = moisNow;

            // Copy other fields
            newTrip.id = allTrips.size() + 1;
            newTrip.duration_minutes = selectedTrip.duration_minutes;
            newTrip.transport_type = selectedTrip.transport_type;
            newTrip.depart = selectedTrip.depart;
            newTrip.arrivee = selectedTrip.arrivee;
            newTrip.price_eur = selectedTrip.price_eur;
            newTrip.destination = selectedTrip.destination;
            newTrip.service_name = selectedTrip.service_name;

            // Add new trip on top and refresh filtered view
            allTrips.add(0, newTrip);
            applyFilters();
            Toast.makeText(this, "Trajet refait (local, sans DB)", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupFilters() {
        Button btnFilterMonth = findViewById(R.id.btnFilterMonth);
        Button btnFilterTransport = findViewById(R.id.btnFilterTransport);
        Button btnFilterDestination = findViewById(R.id.btnFilterDestination);

        btnFilterMonth.setOnClickListener(v -> {
            String[] mois = {"Tous", "Septembre", "Octobre", "Novembre", "Décembre", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août"};
            new AlertDialog.Builder(this)
                    .setTitle("📅 Filtrer par mois")
                    .setItems(mois, (d, w) -> {
                        currentMois = mois[w].equals("Tous") ? null : mois[w];
                        applyFilters();
                    }).show();
        });

        btnFilterTransport.setOnClickListener(v -> {
            String[] types = {"Tous", "BUS", "TAXI"};
            new AlertDialog.Builder(this)
                    .setTitle("🚌 Filtrer par type")
                    .setItems(types, (d, w) -> {
                        currentType = types[w].equals("Tous") ? null : types[w];
                        applyFilters();
                    }).show();
        });

        btnFilterDestination.setOnClickListener(v -> {
            String[] dests = {"Tous", "Aéroport", "Hôtel Central", "Centre Commercial", "Bureau", "Hôtel", "Gare"};
            new AlertDialog.Builder(this)
                    .setTitle("📍 Filtrer par destination")
                    .setItems(dests, (d, w) -> {
                        currentDestination = dests[w].equals("Tous") ? null : dests[w];
                        applyFilters();
                    }).show();
        });
    }

    private void applyFilters() {
        visibleTrips.clear();
        for (Trip t : allTrips) {
            if (currentMois != null && (t.mois == null || !t.mois.equalsIgnoreCase(currentMois))) continue;
            if (currentType != null && (t.transport_type == null || !t.transport_type.equalsIgnoreCase(currentType))) continue;
            if (currentDestination != null && (t.destination == null || !t.destination.equalsIgnoreCase(currentDestination))) continue;
            visibleTrips.add(t);
        }
        adapter.notifyDataSetChanged();
    }

    // Demo data
    private void seedTrips() {
        allTrips.clear();
        allTrips.add(make(1, "2024-09-22 08:30:00", 15, "BUS",  "Centre Ville", "Aéroport",          3.50, "Septembre", "Aéroport",         "Bus Express"));
        allTrips.add(make(2, "2024-09-20 14:00:00", 25, "TAXI", "Gare",         "Hôtel Central",    12.00, "Septembre", "Hôtel Central",    "Taxi Premium"));
        allTrips.add(make(3, "2024-10-15 18:30:00", 20, "BUS",  "Université",   "Centre Commercial", 2.80, "Octobre",   "Centre Commercial", "Bus Rapide"));
        allTrips.add(make(4, "2024-10-12 07:45:00", 35, "TAXI", "Domicile",     "Bureau",           15.50, "Octobre",   "Bureau",           "Taxi Standard"));
        allTrips.add(make(5, "2024-11-05 16:20:00", 18, "BUS",  "Gare",         "Aéroport",          4.00, "Novembre",  "Aéroport",         "Bus Express"));
        allTrips.add(make(6, "2024-11-03 10:15:00", 30, "TAXI", "Aéroport",     "Hôtel",            18.00, "Novembre",  "Hôtel",            "Taxi VIP"));
        allTrips.add(make(7, "2024-11-01 09:00:00", 22, "BUS",  "Centre Ville", "Gare",              2.50, "Novembre",  "Gare",             "Bus Urbain"));
    }

    private Trip make(int id, String dt, int mins, String type, String dep, String arr,
                      double price, String mois, String dest, String service) {
        Trip t = new Trip();
        t.id = id;
        t.date_time = dt;
        t.duration_minutes = mins;
        t.transport_type = type;
        t.depart = dep;
        t.arrivee = arr;
        t.price_eur = price;
        t.mois = mois;
        t.destination = dest;
        t.service_name = service;
        return t;
    }

    // Data model
    public static class Trip {
        public int id;
        public String date_time;
        public int duration_minutes;
        public String transport_type; // BUS or TAXI
        public String depart;
        public String arrivee;
        public double price_eur;
        public String mois;
        public String destination;
        public String service_name;
    }

    // Adapter with programmatic CardView item
    public static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

        public interface OnTripClick { void onClick(Trip t); }

        private final List<Trip> data;
        private final OnTripClick onTripClick;

        public HistoryAdapter(List<Trip> data, OnTripClick onTripClick) {
            this.data = data;
            this.onTripClick = onTripClick;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            // Card root
            CardView card = new CardView(parent.getContext());
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            int m = dp(parent, 8);
            lp.setMargins(m, m, m, m);
            card.setLayoutParams(lp);
            card.setUseCompatPadding(true);
            card.setRadius(dp(parent, 14));
            card.setCardElevation(dp(parent, 6));

            // Content container
            LinearLayout root = new LinearLayout(parent.getContext());
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(dp(parent, 12), dp(parent, 12), dp(parent, 12), dp(parent, 12));
            card.addView(root);

            // Row 1: date (left) + icon (right)
            LinearLayout row1 = new LinearLayout(parent.getContext());
            row1.setOrientation(LinearLayout.HORIZONTAL);
            row1.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView tvDate = new TextView(parent.getContext());
            tvDate.setTextColor(Color.parseColor("#0D1B3E"));
            tvDate.setTextSize(16);
            tvDate.setGravity(Gravity.START);
            LinearLayout.LayoutParams dateLP = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tvDate.setLayoutParams(dateLP);
            tvDate.setTypeface(tvDate.getTypeface(), android.graphics.Typeface.BOLD);

            TextView tvIcon = new TextView(parent.getContext());
            tvIcon.setTextSize(20);
            tvIcon.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            row1.addView(tvDate);
            row1.addView(tvIcon);
            root.addView(row1);

            // Service / type
            TextView tvType = new TextView(parent.getContext());
            tvType.setTextColor(Color.parseColor("#5670A1"));
            LinearLayout.LayoutParams typeLP = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            typeLP.topMargin = dp(parent, 4);
            tvType.setLayoutParams(typeLP);
            root.addView(tvType);

            // Trajet
            TextView tvTrajet = new TextView(parent.getContext());
            LinearLayout.LayoutParams trajetLP = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            trajetLP.topMargin = dp(parent, 4);
            tvTrajet.setLayoutParams(trajetLP);
            root.addView(tvTrajet);

            // Row 2: duration (left) + price (right)
            LinearLayout row2 = new LinearLayout(parent.getContext());
            row2.setOrientation(LinearLayout.HORIZONTAL);
            row2.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams leftLP = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            TextView tvDuree = new TextView(parent.getContext());
            tvDuree.setLayoutParams(leftLP);

            TextView tvPrix = new TextView(parent.getContext());
            tvPrix.setTextColor(Color.parseColor("#1D58D6"));
            tvPrix.setTypeface(tvPrix.getTypeface(), android.graphics.Typeface.BOLD);

            row2.addView(tvDuree);
            row2.addView(tvPrix);
            LinearLayout.LayoutParams row2LP = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            row2LP.topMargin = dp(parent, 8);
            row2.setLayoutParams(row2LP);
            root.addView(row2);

            return new VH(card, tvDate, tvType, tvTrajet, tvDuree, tvPrix, tvIcon);
        }

        @Override
        public void onBindViewHolder(VH h, int position) {
            Trip t = data.get(position);

            h.tvDate.setText(formatDate(t.date_time));
            h.tvTypeTransport.setText((equalsIgnoreCase(t.transport_type, "BUS") ? "🚌 " : "🚕 ")
                    + (t.service_name != null ? t.service_name : safe(t.transport_type)));
            h.tvTrajet.setText("Départ : " + safe(t.depart) + "  →  Arrivée : " + safe(t.arrivee));
            h.tvDuree.setText("Durée : " + t.duration_minutes + " min");

            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.FRANCE);
            h.tvPrix.setText("Coût : " + nf.format(t.price_eur));
            h.tvIconVehicule.setText(equalsIgnoreCase(t.transport_type, "BUS") ? "🚌" : "🚕");

            h.itemView.setOnClickListener(v -> {
                if (onTripClick != null) onTripClick.onClick(t);
            });
        }

        @Override
        public int getItemCount() { return data.size(); }

        // ViewHolder
        static class VH extends RecyclerView.ViewHolder {
            final TextView tvDate, tvTypeTransport, tvTrajet, tvDuree, tvPrix, tvIconVehicule;
            VH(View itemView, TextView tvDate, TextView tvTypeTransport, TextView tvTrajet,
               TextView tvDuree, TextView tvPrix, TextView tvIconVehicule) {
                super(itemView);
                this.tvDate = tvDate;
                this.tvTypeTransport = tvTypeTransport;
                this.tvTrajet = tvTrajet;
                this.tvDuree = tvDuree;
                this.tvPrix = tvPrix;
                this.tvIconVehicule = tvIconVehicule;
            }
        }

        // Utils
        private static boolean equalsIgnoreCase(String a, String b) {
            return a != null && a.equalsIgnoreCase(b);
        }
        private static String safe(String s) { return s == null ? "" : s; }

        private static String formatDate(String dt) {
            if (dt == null || dt.isEmpty()) return "";
            String[] patterns = { "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd HH:mm:ss" };
            for (String p : patterns) {
                try {
                    SimpleDateFormat in = new SimpleDateFormat(p, Locale.US);
                    in.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    Date d = in.parse(dt);
                    SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH);
                    return out.format(d);
                } catch (ParseException ignored) {}
            }
            return dt.replace(".000Z", "").replace('T', ' ');
        }

        private static int dp(View v, int dp) {
            float d = v.getResources().getDisplayMetrics().density;
            return Math.round(dp * d);
        }
    }
}