package com.will.bartendermenu.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {

    private static final String TAG = "FileUtils";
    private static final String APP_DIRECTORY = "BartenderMenu";
    private static final String IMAGES_SUBDIRECTORY = "drink_images";

    /**
     * Cria um arquivo único para salvar uma imagem
     */
    public static File createImageFile(Context context) throws IOException {
        // Criar nome do arquivo com timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "DRINK_" + timeStamp + "_";

        // Obter diretório de armazenamento
        File storageDir = getAppImagesDirectory(context);

        // Criar o arquivo
        File imageFile = File.createTempFile(
                imageFileName,  /* prefixo */
                ".jpg",         /* sufixo */
                storageDir      /* diretório */
        );

        Log.d(TAG, "Arquivo criado: " + imageFile.getAbsolutePath());
        return imageFile;
    }

    /**
     * Obtém ou cria o diretório da app para imagens
     */
    public static File getAppImagesDirectory(Context context) {
        File appDir;

        // Primeiro tenta armazenamento externo
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            appDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_DIRECTORY);
        } else {
            // Se não, usa armazenamento interno
            appDir = new File(context.getFilesDir(), APP_DIRECTORY);
        }

        // Criar diretório se não existir
        if (!appDir.exists()) {
            if (appDir.mkdirs()) {
                Log.d(TAG, "Diretório criado: " + appDir.getAbsolutePath());
            } else {
                Log.e(TAG, "Falha ao criar diretório: " + appDir.getAbsolutePath());
            }
        }

        // Criar subdiretório para imagens
        File imagesDir = new File(appDir, IMAGES_SUBDIRECTORY);
        if (!imagesDir.exists()) {
            if (imagesDir.mkdirs()) {
                Log.d(TAG, "Subdiretório de imagens criado: " + imagesDir.getAbsolutePath());
            }
        }

        return imagesDir;
    }

    /**
     * Exclui um arquivo de imagem
     */
    public static boolean deleteImageFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d(TAG, "Arquivo " + filePath + " excluído: " + deleted);
            return deleted;
        }

        return false;
    }

    /**
     * Verifica se um arquivo de imagem existe
     */
    public static boolean imageFileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }
}