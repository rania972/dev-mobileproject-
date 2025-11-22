package com.example.waslniiii;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class Acuielle extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acuielle);

        // Récupération des boutons
        Button buttonTaxi = findViewById(R.id.button2);
        Button buttonBus = findViewById(R.id.button9);
        Button buttonTrajet = findViewById(R.id.button10);
        Button buttonHistorique = findViewById(R.id.button8);

        // 🔹 Taille désirée des icônes (en pixels)
        int iconSize = 150; // tu peux augmenter cette valeur (ex: 200 pour encore plus grand)

        // 🔹 Appliquer les icônes redimensionnées
        setButtonIcon(buttonTaxi, R.drawable.icon_taxi, iconSize);
        setButtonIcon(buttonBus, R.drawable.icon_bus, iconSize);
        setButtonIcon(buttonTrajet, R.drawable.icon_trajet, iconSize);
        setButtonIcon(buttonHistorique, R.drawable.icon_historique, iconSize);
    }

    // Fonction pour redimensionner une icône vectorielle et l’appliquer à un bouton
    private void setButtonIcon(Button button, int drawableId, int sizePx) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableId);
        if (drawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            // Créer un drawable bitmap redimensionné
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);

            // Définir l’icône au-dessus du texte (gravity top)
            button.setCompoundDrawablesWithIntrinsicBounds(null, bitmapDrawable, null, null);
        }
    }
}
