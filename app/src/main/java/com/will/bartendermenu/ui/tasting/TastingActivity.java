package com.will.bartendermenu.ui.tasting;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.will.bartendermenu.model.Drink;
import com.will.bartendermenu.R;
import com.will.bartendermenu.database.DatabaseHelper;
import com.will.bartendermenu.ui.result.ResultsActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TastingActivity extends AppCompatActivity {

    private static final String TAG = "TastingActivity";

    // Views
    private ImageView imageViewDrink;
    private TextView textViewDrinkName;
    private TextView textViewDrinkDescription;
    private TextView textViewProgress;
    private ProgressBar progressBar;
    private TextView buttonLike, buttonMedium, buttonDislike;

    // Dados
    private DatabaseHelper dbHelper;
    private List<Drink> drinksToTaste;
    private Map<Integer, String> ratings; // drinkId -> rating
    private int currentDrinkIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasting);
        setupBackButton();

        // Inicializar views
        initViews();

        // Inicializar banco de dados
        dbHelper = new DatabaseHelper(this);

        // Inicializar mapa de avaliações
        ratings = new HashMap<>();
if (buttonLike == null) {
    Log.e(TAG, "buttonLike é NULL!");
}

        // Carregar drinks selecionados para degustação
        loadDrinksForTasting();

        // Verificar se há drinks para degustar
        if (drinksToTaste.isEmpty()) {
            Toast.makeText(this, "Nenhum drink selecionado para degustação!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Configurar botões
        setupButtons();

        // Mostrar primeiro drink
        showCurrentDrink();
    }

    private void initViews() {
        imageViewDrink = findViewById(R.id.imageViewDrink);
        textViewDrinkName = findViewById(R.id.textViewDrinkName);
        textViewDrinkDescription = findViewById(R.id.textViewDrinkDescription);
        textViewProgress = findViewById(R.id.textViewProgress);
        progressBar = findViewById(R.id.progressBar);
        // Adicione logs para debug
        buttonLike = findViewById(R.id.buttonLike);
        Log.d(TAG, "buttonLike encontrado: " + (buttonLike != null));

        buttonMedium = findViewById(R.id.buttonMedium);
        Log.d(TAG, "buttonMedium encontrado: " + (buttonMedium != null));

        buttonDislike = findViewById(R.id.buttonDislike);
        Log.d(TAG, "buttonDislike encontrado: " + (buttonDislike != null));
    }


    private void setupBackButton() {
        View btn = findViewById(R.id.btnBackTop);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                onBackPressed(); // respeita suas confirmações (ex: Tasting)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            });
        }
    }


    private void loadDrinksForTasting() {
        // Buscar apenas drinks marcados como selecionados
        drinksToTaste = dbHelper.getSelectedDrinks();

        Log.d(TAG, "Drinks para degustação: " + drinksToTaste.size());

        // Se não houver drinks selecionados, usar todos (para teste)
        if (drinksToTaste.isEmpty()) {
            Log.d(TAG, "Nenhum drink selecionado, usando todos os drinks");
            drinksToTaste = dbHelper.getAllDrinks();
        }

        // Configurar barra de progresso
        progressBar.setMax(drinksToTaste.size());
    }

    private void setupButtons() {
        // VERIFICAÇÃO DE SEGURANÇA
        if (buttonLike == null) {
            Log.e(TAG, "buttonLike é NULL! Verifique o ID no layout_main.");
            return; // Não continue se for null
        }
        if (buttonMedium == null) {
            Log.e(TAG, "buttonMedium é NULL! Verifique o ID no layout_main.");
            return;
        }
        if (buttonDislike == null) {
            Log.e(TAG, "buttonDislike é NULL! Verifique o ID no layout_main.");
            return;
        }

        // Agora pode configurar os listeners com segurança
        buttonLike.setOnClickListener(v -> rateDrink("AMEI!"));
        buttonMedium.setOnClickListener(v -> rateDrink("GOSTEI"));
        buttonDislike.setOnClickListener(v -> rateDrink("PASSO"));
    }

    private void showCurrentDrink() {
        if (currentDrinkIndex >= drinksToTaste.size()) {
            // Todos os drinks foram avaliados
            showResults();
            return;
        }

        Drink currentDrink = drinksToTaste.get(currentDrinkIndex);

        // Atualizar progresso
        updateProgress();

        // Mostrar informações do drink
        textViewDrinkName.setText(currentDrink.getName());
        textViewDrinkDescription.setText(currentDrink.getDescription());

        // Tentar carregar imagem
        loadDrinkImage(currentDrink);
    }

    private void loadDrinkImage(Drink drink) {
        Log.d(TAG, "Carregando imagem para: " + drink.getName());
        Log.d(TAG, "Caminho da imagem: " + drink.getImagePath());

        if (drink.getImagePath() != null && !drink.getImagePath().isEmpty()) {
            try {
                File imgFile = new File(drink.getImagePath());

                // Verificar se o arquivo existe
                if (imgFile.exists()) {
                    Log.d(TAG, "Arquivo encontrado. Tamanho: " + imgFile.length() + " bytes");

                    // Redimensionar a imagem para evitar OutOfMemoryError
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2; // Reduz pela metade

                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

                    if (bitmap != null) {
                        imageViewDrink.setImageBitmap(bitmap);
                        Log.d(TAG, "Imagem carregada com sucesso!");
                        return;
                    } else {
                        Log.e(TAG, "Bitmap é null após decodeFile");
                    }
                } else {
                    Log.e(TAG, "Arquivo NÃO existe: " + drink.getImagePath());
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar imagem: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Drink não tem imagem associada");
        }

        // Se chegou aqui, usar placeholder
        Log.d(TAG, "Usando placeholder");
        imageViewDrink.setImageResource(R.drawable.ic_drink_placeholder);
    }

    private void updateProgress() {
        int current = currentDrinkIndex + 1;
        int total = drinksToTaste.size();

        textViewProgress.setText("Drink " + current + " de " + total);
        progressBar.setProgress(current);
    }
    private void rateDrink(String rating) {
        Drink currentDrink = drinksToTaste.get(currentDrinkIndex);

        // Salvar avaliação
        ratings.put(currentDrink.getId(), rating);
        Log.d(TAG, "Drink avaliado: " + currentDrink.getName() + " - " + rating);

        // Mostrar feedback visual (opcional)
        Toast.makeText(this, "Avaliação registrada!", Toast.LENGTH_SHORT).show();

        // Avançar para o próximo drink
        currentDrinkIndex++;

        // Pequeno delay antes de mostrar próximo drink (melhora UX)
        imageViewDrink.postDelayed(() -> showCurrentDrink(), 300);
    }

    private void showResults() {
        Log.d(TAG, "Degustação concluída! Avaliações: " + ratings.size());

        // Criar intent para a tela de resultados
        Intent intent = new Intent(TastingActivity.this, ResultsActivity.class);

        // Passar as avaliações como extras
        ArrayList<Integer> drinkIds = new ArrayList<>();
        ArrayList<String> drinkRatings = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : ratings.entrySet()) {
            drinkIds.add(entry.getKey());
            drinkRatings.add(entry.getValue());
        }

        intent.putIntegerArrayListExtra("DRINK_IDS", drinkIds);
        intent.putStringArrayListExtra("RATINGS", drinkRatings);

        // Iniciar activity de resultados
        startActivity(intent);

        // Finalizar esta activity
        finish();
    }

    @Override
    public void onBackPressed() {
        // Confirmar se o usuário quer cancelar a degustação
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancelar Degustação")
                .setMessage("Tem certeza que deseja cancelar a degustação? Todo o progresso será perdido.")
                .setPositiveButton("Sim, Cancelar", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Continuar", null)
                .show();
    }
}