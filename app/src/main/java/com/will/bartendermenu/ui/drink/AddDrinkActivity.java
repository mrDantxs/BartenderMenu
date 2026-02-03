package com.will.bartendermenu.ui.drink;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.will.bartendermenu.model.Drink;
import com.will.bartendermenu.R;
import com.will.bartendermenu.database.DatabaseHelper;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.FileOutputStream;


public class AddDrinkActivity extends AppCompatActivity {

    private static final String TAG = "AddDrinkActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_GALLERY_PERMISSION = 101;


    private EditText editTextDrinkName, editTextDescription;
    private CheckBox checkBoxSelected;
    private ImageView imageViewDrink;
    private Button buttonTakePhoto, buttonChoosePhoto, buttonSave, buttonCancel;

    private DatabaseHelper dbHelper;
    private String currentImagePath = ""; // Para armazenar o caminho da foto depois
    private File photoFile;
    private boolean isEditMode = false;
    private long drinkId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_drink);
        setupBackButton();

        dbHelper = new DatabaseHelper(this);

        // Inicializar views
        initViews();

        checkEditMode();
        // Configurar botões (versão inicial sem fotos)
        setupButtonsBasic();
    }

    private void initViews() {
        editTextDrinkName = findViewById(R.id.editTextDrinkName);
        editTextDescription = findViewById(R.id.editTextDescription);
        checkBoxSelected = findViewById(R.id.checkBoxSelected);
        imageViewDrink = findViewById(R.id.imageViewDrink);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        buttonChoosePhoto = findViewById(R.id.buttonChoosePhoto);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
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


    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent == null) {
            Log.e(TAG, "Intent é nula!");
            return;
        }

        Log.d(TAG, "Intent recebida. Extras disponíveis:");
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.d(TAG, "  " + key + " = " + extras.get(key));
            }
        } else {
            Log.d(TAG, "  Nenhum extra encontrado");
        }

        if (intent.hasExtra("EDIT_MODE")) {
            isEditMode = intent.getBooleanExtra("EDIT_MODE", false);
            Log.d(TAG, "EDIT_MODE encontrado: " + isEditMode);

            if (isEditMode) {
                if (intent.hasExtra("DRINK_ID")) {
                    drinkId = intent.getIntExtra("DRINK_ID", -1);
                    Log.d(TAG, "DRINK_ID encontrado: " + drinkId);

                    // Preencher os campos com os dados existentes
                    if (intent.hasExtra("DRINK_NAME")) {
                        String name = intent.getStringExtra("DRINK_NAME");
                        editTextDrinkName.setText(name);
                        Log.d(TAG, "DRINK_NAME: " + name);
                    }

                    if (intent.hasExtra("DRINK_DESCRIPTION")) {
                        String desc = intent.getStringExtra("DRINK_DESCRIPTION");
                        editTextDescription.setText(desc);
                        Log.d(TAG, "DRINK_DESCRIPTION: " + desc);
                    }

                    if (intent.hasExtra("DRINK_SELECTED")) {
                        boolean selected = intent.getBooleanExtra("DRINK_SELECTED", false);
                        checkBoxSelected.setChecked(selected);
                        Log.d(TAG, "DRINK_SELECTED: " + selected);
                    }

                    if (intent.hasExtra("DRINK_IMAGE_PATH")) {
                        currentImagePath = intent.getStringExtra("DRINK_IMAGE_PATH");
                        Log.d(TAG, "DRINK_IMAGE_PATH: " + currentImagePath);

                        // Carregar imagem se existir
                        if (currentImagePath != null && !currentImagePath.isEmpty()) {
                            loadImageFromPath(currentImagePath);
                        }
                    }

                    // Mudar título do botão salvar para indicar edição
                    buttonSave.setText("ATUALIZAR DRINK");
                    Log.d(TAG, "Modo edição configurado para drink ID: " + drinkId);
                } else {
                    Log.e(TAG, "EDIT_MODE true mas DRINK_ID não encontrado!");
                    Toast.makeText(this, "Erro: ID do drink não encontrado", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.d(TAG, "Modo de adição ativado (EDIT_MODE não encontrado)");
            isEditMode = false;
        }
    }

    private void loadImageFromPath(String imagePath) {
        try {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                // Reduzir resolução para evitar problemas de memória
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4; // Reduz para 1/4 da resolução original

                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                if (bitmap != null) {
                    imageViewDrink.setImageBitmap(bitmap);
                } else {
                    Log.e(TAG, "Não foi possível decodificar a imagem do caminho: " + imagePath);
                }
            } else {
                Log.e(TAG, "Arquivo de imagem não encontrado: " + imagePath);
                Toast.makeText(this, "Imagem não encontrada", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar imagem do caminho: " + e.getMessage());
            Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtonsBasic() {
        // Por enquanto, desabilitar botões de foto
        buttonTakePhoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "Nenhuma câmera disponível", Toast.LENGTH_SHORT).show();
            }
        });
        buttonChoosePhoto.setOnClickListener(v -> {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, REQUEST_PICK_IMAGE);
        });

        // Botão Salvar
        buttonSave.setOnClickListener(v -> saveDrink());

        // Botão Cancelar
        buttonCancel.setOnClickListener(v -> finish());

    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return false;
            }
        return true;
    }


    private boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_GALLERY_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Foto tirada com a câmera
                if (data != null && data.getExtras() != null) {
                    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                    if (imageBitmap != null) {
                        imageViewDrink.setImageBitmap(imageBitmap);

                        // Salvar em local acessível
                        currentImagePath = savePhotoToAppDirectory(imageBitmap);

                        if (!currentImagePath.isEmpty()) {
                            Toast.makeText(this, "Foto salva com sucesso!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Erro ao salvar foto", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                // Foto escolhida da galeria
                Uri selectedImageUri = data.getData();

                try {
                    // Converter URI para bitmap
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    imageViewDrink.setImageBitmap(bitmap);

                    // Salvar em local acessível
                    currentImagePath = savePhotoToAppDirectory(bitmap);

                    if (!currentImagePath.isEmpty()) {
                        Toast.makeText(this, "Foto importada com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar imagem: " + e.getMessage());
                    Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveBitmapToFile(Bitmap bitmap) throws IOException {
        // Para simplificar, vamos apenas criar um nome de arquivo
        String fileName = "drink_photo_" + System.currentTimeMillis() + ".jpg";
        File file = new File(getFilesDir(), fileName);

        // Em uma implementação real, você salvaria o bitmap no arquivo
        // Por enquanto, apenas guardamos o caminho
        currentImagePath = file.getAbsolutePath();
    }
    private void saveDrink() {
        String name = editTextDrinkName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        boolean isSelected = checkBoxSelected.isChecked();

        // Validação básica
        if (name.isEmpty()) {
            editTextDrinkName.setError("Digite o nome do drink");
            editTextDrinkName.requestFocus();
            return;
        }

        Log.d(TAG, "Salvando drink. Caminho da foto: " + currentImagePath);
        // Criar objeto Drink
        Drink drink = new Drink();
        drink.setName(name);
        drink.setDescription(description);
        drink.setSelected(isSelected);
        drink.setImagePath(currentImagePath); // Vazio por enquanto



        if (isEditMode) {
            if (drinkId == -1) {
                Toast.makeText(this, "Erro: ID do drink inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            drink.setId((int)drinkId);

            Log.d(TAG, "Atualizando drink com ID: " + drinkId);
            int rowsAffected = dbHelper.updateDrink(drink);

            if (rowsAffected>0) {
                Toast.makeText(this, "Drink atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Log.e(TAG, "Nenhuma linha afetada no update. ID: " + drinkId);

                Drink existingDrink = dbHelper.getDrinkById((int) drinkId);
                if (existingDrink == null) {
                    Toast.makeText(this, "Erro: Drink não encontrado no banco de dados", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erro ao atualizar drink. Nenhuma linha afetada.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            long newDrinkId = dbHelper.addDrink(drink);
            if (newDrinkId > 0) {
                Toast.makeText(this, "Drink salvo com sucesso!", Toast.LENGTH_SHORT).show();

                // Voltar para a lista de drinks
                Intent intent = new Intent(this, DrinkListActivity.class);
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Erro ao salvar drink", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Adicione este método no AddDrinkActivity
    private String savePhotoToAppDirectory(Bitmap bitmap) {
        try {
            // Criar diretório se não existir
            File storageDir = new File(getFilesDir(), "drink_photos");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            // Criar nome único para o arquivo
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "DRINK_" + timeStamp + ".jpg";
            File imageFile = new File(storageDir, imageFileName);

            // Salvar bitmap como JPEG
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();

            Log.d(TAG, "Foto salva em: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar foto: " + e.getMessage());
            return "";
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