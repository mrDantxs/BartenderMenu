package com.will.bartendermenu.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;

import com.will.bartendermenu.model.Drink;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfGenerator {

    private static final String TAG = "PdfGenerator";

    public interface PdfGenerationCallback {
        void onPdfGenerated(File pdfFile);
        void onPdfGenerationFailed(String error);
    }

    public static void generateTastingReport(Context context, List<Drink> drinks,
                                             PdfGenerationCallback callback) {
        new Thread(() -> {
            try {
                File pdfFile = createPdf(context, drinks);
                ((android.app.Activity) context).runOnUiThread(() -> {
                    callback.onPdfGenerated(pdfFile);
                });
            } catch (Exception e) {
                Log.e(TAG, "Erro ao gerar PDF: " + e.getMessage());
                ((android.app.Activity) context).runOnUiThread(() -> {
                    callback.onPdfGenerationFailed(e.getMessage());
                });
            }
        }).start();
    }

    private static File createPdf(Context context, List<Drink> drinks) throws IOException {
        // Criar documento PDF
        PdfDocument document = new PdfDocument();

        // Criar página
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Configurar pintura
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);

        // Desenhar título
        drawTitle(canvas, paint);

        // Desenhar cabeçalho
        drawHeader(canvas, paint);

        // Desenhar drinks
        drawDrinks(canvas, paint, drinks, context);

        // Desenhar rodapé
        drawFooter(canvas, paint);

        // Finalizar página
        document.finishPage(page);

        // Criar nome do arquivo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Degustacao_" + timeStamp + ".pdf";

        // Criar diretório se não existir
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File appDir = new File(downloadsDir, "BartenderMenu");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        File pdfFile = new File(appDir, fileName);

        // Salvar PDF
        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        document.close();
        fos.close();

        return pdfFile;
    }

    private static void drawTitle(Canvas canvas, Paint paint) {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(24);
        paint.setColor(Color.parseColor("#F28C28")); // Laranja
        canvas.drawText("RELATÓRIO DE DEGUSTAÇÃO", 50, 50, paint);

        paint.setTextSize(12);
        paint.setColor(Color.GRAY);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        canvas.drawText("Gerado em: " + sdf.format(new Date()), 50, 70, paint);
    }

    private static void drawHeader(Canvas canvas, Paint paint) {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(14);
        paint.setColor(Color.BLACK);

        int y = 120;
        canvas.drawText("PERFORMANCE COQUETELARIA", 50, y, paint);
        y += 20;
        canvas.drawText("Sistema de Avaliação de Drinks", 50, y, paint);

        // Linha separadora
        paint.setStrokeWidth(2);
        paint.setColor(Color.parseColor("#F28C28"));
        canvas.drawLine(50, y + 10, 545, y + 10, paint);
    }

    private static void drawDrinks(Canvas canvas, Paint paint, List<Drink> drinks, Context context) {
        int y = 180;

        // Cabeçalho da tabela
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(12);
        paint.setColor(Color.WHITE);

        // Fundo do cabeçalho
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#F28C28"));
        canvas.drawRect(50, y - 15, 545, y + 15, paint);

        // Texto do cabeçalho
        canvas.drawText("Nº", 60, y, paint);
        canvas.drawText("DRINK", 100, y, paint);
        canvas.drawText("DESCRIÇÃO", 250, y, paint);
        canvas.drawText("STATUS", 450, y, paint);

        y += 30;

        // Conteúdo da tabela
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setColor(Color.BLACK);

        int numero = 1;
        for (Drink drink : drinks) {
            if (!drink.isSelected()) continue; // Apenas drinks selecionados

            // Linha alternada
            if (numero % 2 == 0) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.parseColor("#F5F5F5"));
                canvas.drawRect(50, y - 10, 545, y + 20, paint);
                paint.setColor(Color.BLACK);
            }

            // Número
            canvas.drawText(String.valueOf(numero), 60, y, paint);

            // Nome do drink (com quebra de linha se necessário)
            String nome = drink.getName();
            if (nome.length() > 20) {
                canvas.drawText(nome.substring(0, 20) + "...", 100, y, paint);
            } else {
                canvas.drawText(nome, 100, y, paint);
            }

            // Descrição (curtada)
            String descricao = drink.getDescription();
            if (descricao == null) descricao = "";
            if (descricao.length() > 30) {
                canvas.drawText(descricao.substring(0, 30) + "...", 250, y, paint);
            } else {
                canvas.drawText(descricao, 250, y, paint);
            }

            // Status
            paint.setColor(drink.isSelected() ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
            String status = drink.isSelected() ? "SELECIONADO" : "NÃO SELEC.";
            canvas.drawText(status, 450, y, paint);
            paint.setColor(Color.BLACK);

            y += 25;
            numero++;

            // Verificar se precisa de nova página
            if (y > 750 && numero < drinks.size()) {
                // TODO: Implementar múltiplas páginas se necessário
                break;
            }
        }

        // Total
        y += 20;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.BLACK);
        long totalSelecionados = drinks.stream().filter(Drink::isSelected).count();
        canvas.drawText("TOTAL DE DRINKS SELECIONADOS: " + totalSelecionados, 50, y, paint);
    }

    private static void drawFooter(Canvas canvas, Paint paint) {
        int y = 800;

        // Linha separadora
        paint.setStrokeWidth(1);
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(50, y, 545, y, paint);

        y += 20;

        // Informações do app
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(10);
        paint.setColor(Color.GRAY);
        canvas.drawText("Bartender Menu v1.0 - Sistema de gerenciamento de drinks", 50, y, paint);
        y += 12;
        canvas.drawText("Relatório gerado automaticamente pelo sistema", 50, y, paint);
        y += 12;
    }
}