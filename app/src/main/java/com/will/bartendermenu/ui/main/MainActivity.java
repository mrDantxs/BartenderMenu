package com.will.bartendermenu.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import com.will.bartendermenu.ui.drink.DrinkListActivity;
import com.will.bartendermenu.ui.intro.IntroSlideActivity;
import com.will.bartendermenu.R;
import com.will.bartendermenu.database.DatabaseHelper;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BartenderMenu";
    private DatabaseHelper dbHelper;

    private static final String PREFS = "bartender_prefs";
    private static final String KEY_CONTRACTOR_NAMES = "contractor_names";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        setupButtons();

    }

    private void setupButtons() {
        // Botão para abrir lista de drinks (Admin)
        ImageButton btnAdmin = findViewById(R.id.btnAdmin);
        Button btnStartTasting = findViewById(R.id.btnStartTasting);

        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DrinkListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // Botão para iniciar degustação (Cliente)
        btnStartTasting.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            String names = prefs.getString(KEY_CONTRACTOR_NAMES, "");

            if (names == null || names.trim().isEmpty()) {
                names = "Convidado"; // fallback
            }

            Intent intent = new Intent(MainActivity.this, IntroSlideActivity.class);
            intent.putExtra(IntroSlideActivity.EXTRA_NOME, names);
            startActivity(intent);

            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

    }

}