package com.will.bartendermenu.ui.result;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import java.io.ByteArrayOutputStream;
import android.content.ContentValues;
import android.provider.MediaStore;

import java.io.OutputStream;

import com.will.bartendermenu.model.Drink;
import com.will.bartendermenu.R;
import com.will.bartendermenu.database.DatabaseHelper;
import com.will.bartendermenu.ui.main.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout containerResults;
    private Button buttonRestart;
    private Button buttonGeneratePdf;


    // MUDANÇA: Agora guardamos tanto o drink quanto a avaliação
    private List<DrinkWithRating> evaluatedDrinks = new ArrayList<>();

    private static final int REQUEST_PERMISSION_CODE = 1001;

    // CLASSE AUXILIAR para guardar drink + avaliação
    private static class DrinkWithRating {
        Drink drink;
        String rating;

        DrinkWithRating(Drink drink, String rating) {
            this.drink = drink;
            this.rating = rating;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        setupBackButton();

        dbHelper = new DatabaseHelper(this);
        containerResults = findViewById(R.id.containerResults);
        buttonRestart = findViewById(R.id.buttonRestart);
        buttonGeneratePdf = findViewById(R.id.buttonGeneratePdf);

        loadResults();

        buttonGeneratePdf.setOnClickListener(v -> askForPermission());
        buttonRestart.setOnClickListener(v -> restartApp());
    }

    private void askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            generatePdfReport();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            generatePdfReport();
        } else {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Permissão Necessária")
                    .setMessage("Para salvar o PDF na sua pasta Downloads, preciso da permissão para escrever no armazenamento.\n\nÉ só tocar em PERMITIR na próxima tela!")
                    .setPositiveButton("PEDIR PERMISSÃO", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_PERMISSION_CODE);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
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


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão concedida! Gerando PDF...", Toast.LENGTH_SHORT).show();
                generatePdfReport();
            } else {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Permissão Negada 😔")
                        .setMessage("Sem permissão não consigo salvar o PDF na sua pasta Downloads.\n\n" +
                                "Você pode:\n" +
                                "1. Tentar novamente e permitir\n" +
                                "2. Usar o botão COMPARTILHAR para salvar onde quiser\n\n" +
                                "Quer tentar de novo?")
                        .setPositiveButton("SIM, TENTAR DE NOVO", (dialog, which) -> askForPermission())
                        .setNegativeButton("NÃO, DEPOIS", null)
                        .show();
            }
        }
    }

    private void generatePdfReport() {
        if (evaluatedDrinks.isEmpty()) {
            Toast.makeText(this, "Nenhum drink avaliado para gerar relatório", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Gerando PDF...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                File pdfFile = createPdf();
                runOnUiThread(() -> {
                    Toast.makeText(ResultsActivity.this,
                            "PDF pronto!", Toast.LENGTH_LONG).show();
                    showPdfOptions(pdfFile);
                });
            } catch (Exception e) {
                Log.e("ResultsActivity", "Erro ao gerar PDF", e);
                runOnUiThread(() -> Toast.makeText(ResultsActivity.this,
                        "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void showPdfOptions(File pdfFile) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("PDF Gerado!")
                .setMessage("O que deseja fazer?")
                .setPositiveButton("ABRIR", (dialog, which) -> openPdfFile(pdfFile))
                .setNeutralButton("COMPARTILHAR", (dialog, which) -> sharePdfFile(pdfFile))
                .setNegativeButton("OK", null)
                .show();
    }

    private File createPdf() throws IOException {

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(595, 842, 1).create();

        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        drawPdfContent(canvas);
        document.finishPage(page);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.writeTo(baos);
        document.close();

        byte[] pdfBytes = baos.toByteArray();

        String timeStamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()
        ).format(new Date());

        String fileName = "Degustacao_" + timeStamp + ".pdf";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return savePdfToDownloadsQ(pdfBytes, fileName);
        } else {
            return savePdfLegacy(pdfBytes, fileName);
        }
    }
    private File savePdfLegacy(byte[] pdfBytes, String fileName) throws IOException {

        File downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File appDir = new File(downloadsDir, "BartenderMenu");
        if (!appDir.exists()) appDir.mkdirs();

        File pdfFile = savePdfToDownloadsQ(pdfBytes, fileName);

        FileOutputStream fos = new FileOutputStream(pdfFile);
        fos.write(pdfBytes);
        fos.close();

        return pdfFile;
    }



    private void drawPdfContent(Canvas canvas) {
        Paint paint = new Paint();

        // Título
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(24);
        paint.setColor(Color.parseColor("#F28C28"));
        canvas.drawText("RELATÓRIO DE DEGUSTAÇÃO", 50, 50, paint);

        // Data
        paint.setTextSize(12);
        paint.setColor(Color.GRAY);
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText("Gerado em: " + date, 50, 70, paint);

        // Resumo
        paint.setColor(Color.BLACK);
        canvas.drawText("Total de drinks avaliados: " + evaluatedDrinks.size(), 50, 100, paint);

        // Cabeçalho da tabela
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Nº", 50, 130, paint);
        canvas.drawText("DRINK", 100, 130, paint);
        canvas.drawText("AVALIAÇÃO", 300, 130, paint);

        // Linha divisória
        paint.setStrokeWidth(1);
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(50, 140, 545, 140, paint);

        // Lista de drinks com avaliações
        int y = 160;
        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setColor(Color.BLACK);

        for (int i = 0; i < evaluatedDrinks.size(); i++) {
            DrinkWithRating drinkWithRating = evaluatedDrinks.get(i);

            // Número
            canvas.drawText(String.valueOf(i + 1), 50, y, paint);

            // Nome do drink
            canvas.drawText(drinkWithRating.drink.getName(), 100, y, paint);

            // Avaliação (com cor baseada na categoria)
            paint.setColor(getColorForRating(drinkWithRating.rating));
            canvas.drawText(drinkWithRating.rating, 300, y, paint);

            // Voltar cor preta para os próximos itens
            paint.setColor(Color.BLACK);

            y += 20;

            // Quebra de página se necessário
            if (y > 750 && i < evaluatedDrinks.size() - 1) {
                // Aqui você poderia adicionar nova página se quiser
                canvas.drawText("... continua na próxima página", 50, y, paint);
                break;
            }
        }

        // Resumo por categoria
        y += 30;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.DKGRAY);
        canvas.drawText("RESUMO POR CATEGORIA:", 50, y, paint);

        // Contar avaliações
        int gosteiMuito = 0, gostoMedio = 0, naoGostei = 0;
        for (DrinkWithRating dwr : evaluatedDrinks) {
            switch (dwr.rating) {
                case "AMEI": gosteiMuito++; break;
                case "GOSTEI": gostoMedio++; break;
                case "PASSO": naoGostei++; break;
            }
        }

        y += 20;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setColor(Color.parseColor("#4CAF50"));
        canvas.drawText("• AMEI!: " + gosteiMuito + " drinks", 70, y, paint);

        y += 20;
        paint.setColor(Color.parseColor("#FF9800"));
        canvas.drawText("• GOSTEI: " + gostoMedio + " drinks", 70, y, paint);

        y += 20;
        paint.setColor(Color.parseColor("#F44336"));
        canvas.drawText("• PASSO: " + naoGostei + " drinks", 70, y, paint);

        // Rodapé
        y = 800;
        paint.setTextSize(10);
        paint.setColor(Color.GRAY);
        canvas.drawText("Bartender Menu - Relatório gerado automaticamente", 50, y, paint);
    }

    // Método auxiliar para pegar cor baseada na avaliação
    private int getColorForRating(String rating) {
        switch (rating) {
            case "AMEI!": return Color.parseColor("#4CAF50");
            case "GOSTEI": return Color.parseColor("#FF9800");
            case "PASSO": return Color.parseColor("#F44336");
            default: return Color.BLACK;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private File savePdfToDownloadsQ(byte[] pdfBytes, String fileName) throws IOException {

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BartenderMenu");

        Uri uri = getContentResolver()
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

        if (uri == null) {
            throw new IOException("Falha ao criar arquivo no Downloads");
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            outputStream.write(pdfBytes);
            outputStream.flush();
        }

        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS + "/BartenderMenu"), fileName);
    }


    private void openPdfFile(File pdfFile) {
        if (!pdfFile.exists()) {
            Toast.makeText(this, "Arquivo não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri pdfUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider", pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Instale um leitor de PDF (ex: Adobe Reader)", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir. Tente compartilhar.", Toast.LENGTH_LONG).show();
        }
    }

    private void sharePdfFile(File pdfFile) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider", pdfFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Meu Relatório de Degustação");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "📤 Compartilhar PDF"));
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao compartilhar", Toast.LENGTH_SHORT).show();
        }
    }

    private void restartApp() {
        Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void loadResults() {
        Intent intent = getIntent();
        ArrayList<Integer> drinkIds = intent.getIntegerArrayListExtra("DRINK_IDS");
        ArrayList<String> ratings = intent.getStringArrayListExtra("RATINGS");

        if (drinkIds == null || ratings == null || drinkIds.isEmpty()) {
            showNoResults();
            return;
        }

        evaluatedDrinks.clear();
        Map<String, List<Drink>> categorizedDrinks = new HashMap<>();
        categorizedDrinks.put("AMEI!", new ArrayList<>());
        categorizedDrinks.put("GOSTEI", new ArrayList<>());
        categorizedDrinks.put("PASSO", new ArrayList<>());

        List<Drink> allDrinks = dbHelper.getAllDrinks();
        Map<Integer, Drink> drinkMap = new HashMap<>();
        for (Drink drink : allDrinks) {
            drinkMap.put(drink.getId(), drink);
        }

        for (int i = 0; i < drinkIds.size(); i++) {
            Drink drink = drinkMap.get(drinkIds.get(i));
            String rating = ratings.get(i);

            if (drink != null && rating != null) {
                List<Drink> list = categorizedDrinks.get(rating);
                if (list != null) {
                    list.add(drink);
                    // AGORA SALVAMOS O DRINK JUNTO COM A AVALIAÇÃO
                    evaluatedDrinks.add(new DrinkWithRating(drink, rating));
                }
            }
        }

        displayCategory("AMEI!", "⭐", categorizedDrinks.get("AMEI!"));
        displayCategory("GOSTEI", "〰️", categorizedDrinks.get("GOSTEI"));
        displayCategory("PASSO", "❌", categorizedDrinks.get("PASSO"));
    }

    private void showNoResults() {
        TextView noResults = new TextView(this);
        noResults.setText("Nenhuma avaliação encontrada.");
        noResults.setTextColor(0xFF8b949e);
        noResults.setTextSize(16);
        noResults.setPadding(0, 24, 0, 24);
        noResults.setGravity(View.TEXT_ALIGNMENT_CENTER);
        containerResults.addView(noResults);
    }

    private void displayCategory(String categoryTitle, String icon, List<Drink> drinks) {
        if (drinks == null || drinks.isEmpty()) return;

        TextView categoryHeader = new TextView(this);
        categoryHeader.setText(categoryTitle + " (" + drinks.size() + ")");
        categoryHeader.setTextColor(getColorForCategory(categoryTitle));
        categoryHeader.setTextSize(18);
        categoryHeader.setTypeface(Typeface.DEFAULT_BOLD);
        categoryHeader.setPadding(32, 24, 32, 8);
        categoryHeader.setGravity(View.TEXT_ALIGNMENT_CENTER);
        containerResults.addView(categoryHeader);

        for (Drink drink : drinks) {
            addDrinkItem(drink, icon, categoryTitle);
        }

        View space = new View(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 16));
        containerResults.addView(space);
    }

    private void addDrinkItem(Drink drink, String icon, String category) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_result, null);

        TextView textViewIcon = itemView.findViewById(R.id.textViewIcon);
        TextView textViewDrinkName = itemView.findViewById(R.id.textViewDrinkName);
        TextView textViewRating = itemView.findViewById(R.id.textViewRating);

        textViewIcon.setText(icon);
        textViewDrinkName.setText(drink.getName());
        textViewRating.setText(category);
        itemView.setBackgroundColor(getBackgroundColorForCategory(category));
        textViewDrinkName.setTextColor(getColorForCategory(category));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);
        itemView.setLayoutParams(params);

        containerResults.addView(itemView);
    }

    private int getColorForCategory(String category) {
        switch (category) {
            case "AMEI!": return Color.parseColor("#4CAF50");
            case "GOSTEI": return Color.parseColor("#FF9800");
            case "PASSO": return Color.parseColor("#F44336");
            default: return Color.parseColor("#2196F3");
        }
    }

    private int getBackgroundColorForCategory(String category) {
        switch (category) {
            case "AMEI!": return Color.argb(30, 76, 175, 80);
            case "GOSTEI": return Color.argb(30, 255, 152, 0);
            case "PASSO": return Color.argb(30, 244, 67, 54);
            default: return Color.argb(30, 33, 150, 243);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}