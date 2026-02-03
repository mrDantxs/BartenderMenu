package com.will.bartendermenu.ui.intro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.will.bartendermenu.R;
import com.will.bartendermenu.ui.tasting.TastingActivity;
import com.will.bartendermenu.database.DatabaseHelper;
import com.will.bartendermenu.ui.main.MainActivity;

import androidx.appcompat.app.AppCompatActivity;

public class IntroSlideActivity extends AppCompatActivity {

    public static final String EXTRA_NOME = "extra_nome_contratante";
    private static final String TAG = "IntroSlideActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_slide);

        Log.d(TAG, "Activity criada");

        // Fade ao ENTRAR na tela
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        String nome = getIntent().getStringExtra(EXTRA_NOME);
        if (nome == null || nome.trim().isEmpty()) nome = "Convidado";

        TextView textGreeting = findViewById(R.id.textGreeting);
        textGreeting.setText("Olá " + nome + ",");

        View root = findViewById(R.id.introRoot);
        root.setOnClickListener(v -> {
            Log.d(TAG, "Clique detectado, indo para TastingActivity");
            irParaDegustacao();
        });

        // Também adiciona clique no texto de dica
        TextView textHint = findViewById(R.id.textHint);
        if (textHint != null) {
            textHint.setOnClickListener(v -> {
                Log.d(TAG, "Clique no hint, indo para TastingActivity");
                irParaDegustacao();
            });
        }
    }

    private void irParaDegustacao() {
        try {
            Log.d(TAG, "Tentando iniciar TastingActivity");

            DatabaseHelper dbHelper = new DatabaseHelper(this);
            int selectedDrinksCount = dbHelper.getSelectedDrinksCount();
            Log.d(TAG, "Drinks selecionados: " + selectedDrinksCount);

            Intent intent;
            if (selectedDrinksCount == 0) {
                Log.d(TAG, "Nenhum drink selecionado, indo para MainActivity");
                intent = new Intent(IntroSlideActivity.this, MainActivity.class);
            } else {
                intent = new Intent(IntroSlideActivity.this, TastingActivity.class);
            }

            startActivity(intent);
            // APENAS a animação, SEM finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            // NÃO CHAME finish() AQUI

        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar activity: " + e.getMessage(), e);
            Intent intent = new Intent(IntroSlideActivity.this, MainActivity.class);
            startActivity(intent);
            // Também não precisa de finish() aqui
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity destruída");
    }
}