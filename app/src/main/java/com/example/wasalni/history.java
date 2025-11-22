package com.example.wasalni;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class history extends AppCompatActivity {

    private LinearLayout containerTrajets;
    private String selectedTrajet = "";
    private List<TrajetData> allTrajets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Masquer l'ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.history);

        containerTrajets = findViewById(R.id.containerTrajets);

        // Charger les données (SANS VÉLO)
        chargerDonnees();

        // Afficher tous les trajets
        afficherTrajets(allTrajets);

        // Configurer les filtres
        setupFiltres();

        // Bouton refaire trajet
        findViewById(R.id.btnRefaireTrajet).setOnClickListener(v -> {
            if (!selectedTrajet.isEmpty()) {
                Toast.makeText(this, "Refaire: " + selectedTrajet, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sélectionnez un trajet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chargerDonnees() {
        // ✅ UNIQUEMENT BUS ET TAXI (Vélo supprimé)

        allTrajets.add(new TrajetData("22 sept. 2024, 08:30", "15 min", "🚌 Bus Express",
                "Centre Ville", "Aéroport", "3,50 €", "🚌", "#2196F3", "Septembre", "Bus", "Aéroport"));

        allTrajets.add(new TrajetData("20 sept. 2024, 14:00", "25 min", "🚕 Taxi Premium",
                "Gare", "Hôtel Central", "12,00 €", "🚕", "#FFC107", "Septembre", "Taxi", "Hôtel Central"));

        allTrajets.add(new TrajetData("15 oct. 2024, 18:30", "20 min", "🚌 Bus Rapide",
                "Université", "Centre Commercial", "2,80 €", "🚌", "#2196F3", "Octobre", "Bus", "Centre Commercial"));

        allTrajets.add(new TrajetData("12 oct. 2024, 07:45", "35 min", "🚕 Taxi Standard",
                "Domicile", "Bureau", "15,50 €", "🚕", "#FFC107", "Octobre", "Taxi", "Bureau"));

        allTrajets.add(new TrajetData("05 nov. 2024, 16:20", "18 min", "🚌 Bus Express",
                "Gare", "Aéroport", "4,00 €", "🚌", "#2196F3", "Novembre", "Bus", "Aéroport"));

        allTrajets.add(new TrajetData("03 nov. 2024, 10:15", "30 min", "🚕 Taxi VIP",
                "Aéroport", "Hôtel", "18,00 €", "🚕", "#FFC107", "Novembre", "Taxi", "Hôtel"));

        allTrajets.add(new TrajetData("01 nov. 2024, 09:00", "22 min", "🚌 Bus Urbain",
                "Centre Ville", "Gare", "2,50 €", "🚌", "#2196F3", "Novembre", "Bus", "Gare"));
    }

    private void setupFiltres() {
        Button btnFilterMonth = findViewById(R.id.btnFilterMonth);
        Button btnFilterTransport = findViewById(R.id.btnFilterTransport);
        Button btnFilterDestination = findViewById(R.id.btnFilterDestination);

        // Filtre par Mois
        btnFilterMonth.setOnClickListener(v -> {
            String[] mois = {"Tous", "Septembre", "Octobre", "Novembre"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("📅 Filtrer par mois");
            builder.setItems(mois, (dialog, which) -> {
                String moisSelectionne = mois[which];

                if (moisSelectionne.equals("Tous")) {
                    afficherTrajets(allTrajets);
                    Toast.makeText(this, "Affichage de tous les trajets", Toast.LENGTH_SHORT).show();
                } else {
                    List<TrajetData> trajetsFiltres = new ArrayList<>();
                    for (TrajetData trajet : allTrajets) {
                        if (trajet.mois.equals(moisSelectionne)) {
                            trajetsFiltres.add(trajet);
                        }
                    }
                    afficherTrajets(trajetsFiltres);
                    Toast.makeText(this, "Trajets de " + moisSelectionne + " (" + trajetsFiltres.size() + ")", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        });

        // Filtre par Type de Transport (SANS VÉLO)
        btnFilterTransport.setOnClickListener(v -> {
            String[] types = {"Tous", "Bus", "Taxi"};  // ✅ Vélo supprimé

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("🚌 Filtrer par type");
            builder.setItems(types, (dialog, which) -> {
                String typeSelectionne = types[which];

                if (typeSelectionne.equals("Tous")) {
                    afficherTrajets(allTrajets);
                    Toast.makeText(this, "Affichage de tous les types", Toast.LENGTH_SHORT).show();
                } else {
                    List<TrajetData> trajetsFiltres = new ArrayList<>();
                    for (TrajetData trajet : allTrajets) {
                        if (trajet.type.equals(typeSelectionne)) {
                            trajetsFiltres.add(trajet);
                        }
                    }
                    afficherTrajets(trajetsFiltres);
                    Toast.makeText(this, "Trajets en " + typeSelectionne + " (" + trajetsFiltres.size() + ")", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        });

        // Filtre par Destination
        btnFilterDestination.setOnClickListener(v -> {
            // Récupérer toutes les destinations uniques
            List<String> destinations = new ArrayList<>();
            destinations.add("Tous");
            for (TrajetData trajet : allTrajets) {
                if (!destinations.contains(trajet.destination)) {
                    destinations.add(trajet.destination);
                }
            }

            String[] destArray = destinations.toArray(new String[0]);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("📍 Filtrer par destination");
            builder.setItems(destArray, (dialog, which) -> {
                String destinationSelectionnee = destArray[which];

                if (destinationSelectionnee.equals("Tous")) {
                    afficherTrajets(allTrajets);
                    Toast.makeText(this, "Affichage de toutes les destinations", Toast.LENGTH_SHORT).show();
                } else {
                    List<TrajetData> trajetsFiltres = new ArrayList<>();
                    for (TrajetData trajet : allTrajets) {
                        if (trajet.destination.equals(destinationSelectionnee)) {
                            trajetsFiltres.add(trajet);
                        }
                    }
                    afficherTrajets(trajetsFiltres);
                    Toast.makeText(this, "Trajets vers " + destinationSelectionnee + " (" + trajetsFiltres.size() + ")", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        });
    }

    private void afficherTrajets(List<TrajetData> trajets) {
        // Vider le container
        containerTrajets.removeAllViews();

        // Ajouter les trajets filtrés
        for (TrajetData trajet : trajets) {
            ajouterTrajet(trajet);
        }
    }

    private void ajouterTrajet(TrajetData trajet) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_trajet, containerTrajets, false);

        TextView tvDate = itemView.findViewById(R.id.tvDate);
        TextView tvDuree = itemView.findViewById(R.id.tvDuree);
        TextView tvTypeTransport = itemView.findViewById(R.id.tvTypeTransport);
        TextView tvTrajet = itemView.findViewById(R.id.tvTrajet);
        TextView tvPrix = itemView.findViewById(R.id.tvPrix);
        TextView tvIconVehicule = itemView.findViewById(R.id.tvIconVehicule);

        tvDate.setText(trajet.date);
        tvDuree.setText("⏱️ " + trajet.duree);
        tvTypeTransport.setText(trajet.typeTransport);
        tvTrajet.setText("Départ: " + trajet.depart + " → Arrivée: " + trajet.arrivee);
        tvPrix.setText(trajet.prix);
        tvIconVehicule.setText(trajet.icone);

        itemView.setOnClickListener(v -> {
            selectedTrajet = trajet.depart + " → " + trajet.arrivee;
            Toast.makeText(this, "Trajet sélectionné", Toast.LENGTH_SHORT).show();
        });

        containerTrajets.addView(itemView);
    }

    // Classe interne pour stocker les données
    private static class TrajetData {
        String date, duree, typeTransport, depart, arrivee, prix, icone, couleur;
        String mois, type, destination;

        TrajetData(String date, String duree, String typeTransport, String depart,
                   String arrivee, String prix, String icone, String couleur,
                   String mois, String type, String destination) {
            this.date = date;
            this.duree = duree;
            this.typeTransport = typeTransport;
            this.depart = depart;
            this.arrivee = arrivee;
            this.prix = prix;
            this.icone = icone;
            this.couleur = couleur;
            this.mois = mois;
            this.type = type;
            this.destination = destination;
        }
    }
}