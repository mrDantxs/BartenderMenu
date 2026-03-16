package com.will.bartendermenu.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.will.bartendermenu.model.Drink;
import com.will.bartendermenu.model.Menu;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Informações do banco
    private static final String DATABASE_NAME = "bartender.db";
    private static final int DATABASE_VERSION = 6;

    // Nome da tabela
    public static final String TABLE_DRINKS = "drinks";

    // Colunas da tabela drinks
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_IS_SELECTED = "is_selected";
    public static final String COLUMN_TASTING_ORDER = "tasting_order";
    public static final String COLUMN_DISPLAY_ORDER = "display_order";
    public static final String TABLE_MENUS = "menus";
    public static final String COLUMN_MENU_ID = "menu_id";
    public static final String COLUMN_MENU_NAME = "name";
    private static final String TAG = "DatabaseHelper";
    public static final String TABLE_MENU_DRINK = "menu_drink";
    public static final String COLUMN_DRINK_ID = "drink_id";

    // SQL para criar a tabela menus
    private static final String CREATE_TABLE_MENUS =
            "CREATE TABLE " + TABLE_MENUS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_MENU_NAME + " TEXT NOT NULL"
                    + ")";

    // SQL para criar a tabela drinks
    private static final String CREATE_TABLE_DRINKS =
            "CREATE TABLE " + TABLE_DRINKS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT NOT NULL,"
                    + COLUMN_DESCRIPTION + " TEXT,"
                    + COLUMN_IMAGE_PATH + " TEXT,"
                    + COLUMN_IS_SELECTED + " INTEGER DEFAULT 0,"
                    + COLUMN_MENU_ID + " INTEGER,"
                    + COLUMN_TASTING_ORDER + " INTEGER DEFAULT NULL"
                    + ")";

    private static final String CREATE_TABLE_MENU_DRINK =
            "CREATE TABLE " + TABLE_MENU_DRINK + " (" +
                    COLUMN_MENU_ID + " INTEGER," +
                    COLUMN_DRINK_ID + " INTEGER," +
                    COLUMN_DISPLAY_ORDER + " INTEGER," +
                    "PRIMARY KEY (" + COLUMN_MENU_ID + ", " + COLUMN_DRINK_ID + ")" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Criando tabela drinks...");
        db.execSQL(CREATE_TABLE_DRINKS);
        db.execSQL(CREATE_TABLE_MENUS);
        db.execSQL(CREATE_TABLE_MENU_DRINK);
        Log.d(TAG, "Tabela drinks e menus criada com sucesso!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Só adiciona tasting_order se vier de versão antiga
        if (oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_MENU_DRINK +
                        " ADD COLUMN " + COLUMN_DISPLAY_ORDER + " INTEGER;");
            } catch (Exception e) {
                Log.w(TAG, "Coluna display_order já existe.");
            }
        }

        if (oldVersion < 5) {
            try {
                db.execSQL(CREATE_TABLE_MENU_DRINK);
            } catch (Exception e) {
                Log.w(TAG, "Tabela menu_drink já existe.");
            }
        }
        // NÃO tente recriar is_selected, porque ela já existe no CREATE TABLE
        Log.d(TAG, "Banco atualizado de versão " + oldVersion + " para " + newVersion);
    }

    // ============ MÉTODO addDrink ============
    public long addDrink(Drink drink) {
        Log.d(TAG, "Adicionando drink: " + drink.getName());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, drink.getName());
        values.put(COLUMN_DESCRIPTION, drink.getDescription());

        // Se a imagem for nula, coloca string vazia
        if (drink.getImagePath() != null) {
            values.put(COLUMN_IMAGE_PATH, drink.getImagePath());
        } else {
            values.put(COLUMN_IMAGE_PATH, "");
        }

        values.put(COLUMN_IS_SELECTED, drink.isSelected() ? 1 : 0);

        // Inserir no banco
        long id = db.insert(TABLE_DRINKS, null, values);
        db.close();

        Log.d(TAG, "Drink adicionado com ID: " + id);
        return id;
    }

    // ============ MÉTODO SIMPLES para testar ============
    public void testAddDrink() {
        Log.d(TAG, "Testando adição de drink...");

        Drink testDrink = new Drink("Drink Teste", "Descrição teste");
        long id = addDrink(testDrink);

        Log.d(TAG, "Teste concluído. ID gerado: " + id);
    }

    // ============ Outros métodos (opcional por enquanto) ============
    public List<Drink> getAllDrinks() {
        List<Drink> drinks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_DRINKS,
                    new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_IMAGE_PATH, COLUMN_IS_SELECTED},
                    null, null, null, null, COLUMN_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Drink drink = new Drink();
                    drink.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    drink.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                    drink.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    drink.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
                    drink.setSelected(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SELECTED)) == 1);

                    drinks.add(drink);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar drinks: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return drinks;
    }

    // ============ MÉTODO DELETE DRINK ============
    public boolean deleteDrink(long id) {
        Log.d(TAG, "Excluindo drink com ID: " + id);

        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_DRINKS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();

        boolean success = rowsAffected > 0;
        Log.d(TAG, "Drink excluído. Sucesso: " + success);
        return success;
    }

    // ============ MÉTODO UPDATE DRINK SELECTION ============
    public void updateDrinkSelection(int drinkId, boolean isSelected) {
        Log.d(TAG, "Atualizando seleção do drink ID: " + drinkId + " para: " + isSelected);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_SELECTED, isSelected ? 1 : 0);

        db.update(TABLE_DRINKS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(drinkId)});
        db.close();
    }

    // ============ MÉTODO UPDATE DRINK COMPLETO ============
    public int updateDrink(Drink drink) {
        Log.d(TAG, "Atualizando drink: " + drink.getName());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, drink.getName());
        values.put(COLUMN_DESCRIPTION, drink.getDescription());


        if (drink.getImagePath() != null) {
            values.put(COLUMN_IMAGE_PATH, drink.getImagePath());
        }

        values.put(COLUMN_IS_SELECTED, drink.isSelected() ? 1 : 0);

        int rowsAffected = db.update(TABLE_DRINKS, values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(drink.getId())});
        db.close();

        Log.d(TAG, "Drink atualizado. Linhas afetadas: " + rowsAffected);
        return rowsAffected;
    }


    // ============ MÉTODO GET SELECTED DRINKS ============
    public List<Drink> getSelectedDrinks() {
        List<Drink> drinks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_DRINKS,
                    new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_IMAGE_PATH, COLUMN_IS_SELECTED},
                    COLUMN_IS_SELECTED + " = ?",
                    new String[]{"1"},
                    null, null, COLUMN_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Drink drink = new Drink();
                    drink.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    drink.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                    drink.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    drink.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
                    drink.setSelected(true);

                    drinks.add(drink);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar drinks selecionados: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return drinks;
    }

    public boolean deleteDrinkWithImage(int id) {
        Log.d(TAG, "Excluindo drink com ID: " + id + " (com imagem)");

        // Primeiro buscar o drink para obter o caminho da imagem
        Drink drink = getDrinkById(id);

        if (drink != null && drink.getImagePath() != null && !drink.getImagePath().isEmpty()) {
            // Tentar excluir o arquivo de imagem
            File imageFile = new File(drink.getImagePath());
            if (imageFile.exists()) {
                boolean imageDeleted = imageFile.delete();
                Log.d(TAG, "Imagem excluída: " + drink.getImagePath() + " - Sucesso: " + imageDeleted);
            }
        }

        // Agora excluir do banco
        return deleteDrink(id);
    }

    // ============ MÉTODO GET DRINK BY ID ============
    public Drink getDrinkById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Drink drink = null;

        try {
            cursor = db.query(TABLE_DRINKS,
                    new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_IMAGE_PATH, COLUMN_IS_SELECTED},
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                drink = new Drink();
                drink.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                drink.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                drink.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                drink.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
                drink.setSelected(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SELECTED)) == 1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar drink por ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return drink;
    }

    public int getSelectedDrinksCount() {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // USE O NOME CORRETO DA COLUNA: COLUMN_IS_SELECTED
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DRINKS +
                    " WHERE " + COLUMN_IS_SELECTED + " = 1", null);

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Erro ao contar drinks selecionados", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    public List<Drink> getSelectedDrinksForTasting() {
        List<Drink> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_DRINKS,
                null,
                COLUMN_IS_SELECTED + " = ?",
                new String[]{"1"},
                null, null,
                COLUMN_NAME + " ASC"
        );


        if (cursor != null && cursor.moveToFirst()) {
            do {
                Drink d = new Drink();
                d.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                d.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                d.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                d.setSelected(true);
                d.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));
                list.add(d);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return list;
    }

    public void clearTastingOrder() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull(COLUMN_TASTING_ORDER);
        db.update(TABLE_DRINKS, values, null, null);

    }

    public void updateTastingOrder(int drinkId, int order) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TASTING_ORDER, order);
        db.update(TABLE_DRINKS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(drinkId)});

    }

    public List<Drink> getDrinksInTastingOrder() {
        List<Drink> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_DRINKS,
                null,
                COLUMN_IS_SELECTED + " = ? AND " + COLUMN_TASTING_ORDER + " IS NOT NULL",
                new String[]{"1"},
                null, null,
                COLUMN_TASTING_ORDER + " ASC"
        );


        if (cursor != null && cursor.moveToFirst()) {
            do {
                Drink d = new Drink();
                d.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                d.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                d.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                d.setSelected(true);
                d.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));
                list.add(d);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return list;
    }

    public void addDrinkToMenu(int drinkId, int menuId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_DRINK_ID, drinkId);
        values.put(COLUMN_MENU_ID, menuId);

        db.insertWithOnConflict(
                TABLE_MENU_DRINK,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );

        db.close();
    }

    public void removeDrinkFromMenu(int drinkId, int menuId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(
                TABLE_MENU_DRINK,
                COLUMN_DRINK_ID + "=? AND " + COLUMN_MENU_ID + "=?",
                new String[]{String.valueOf(drinkId), String.valueOf(menuId)}
        );

        db.close();
    }

    public List<Drink> getDrinksByMenu(int menuId) {
        List<Drink> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT d.* FROM " + TABLE_DRINKS + " d " +
                        "INNER JOIN " + TABLE_MENU_DRINK + " md " +
                        "ON d." + COLUMN_ID + " = md." + COLUMN_DRINK_ID + " " +
                        "WHERE md." + COLUMN_MENU_ID + " = ? " +
                        "ORDER BY md.display_order ASC, d." + COLUMN_NAME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(menuId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Drink d = new Drink();
                d.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                d.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                d.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                d.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
                d.setSelected(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SELECTED)) == 1);

                list.add(d);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return list;
    }

    public List<Integer> getMenuIdsByDrink(int drinkId) {
        List<Integer> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_MENU_DRINK,
                new String[]{COLUMN_MENU_ID},
                COLUMN_DRINK_ID + "=?",
                new String[]{String.valueOf(drinkId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursor.getInt(0));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return list;
    }

    public long addMenu(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_MENU_NAME, name);

        long id = db.insert(TABLE_MENUS, null, values);
        db.close();

        return id;
    }

    public List<Menu> getAllMenus() {
        List<Menu> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_MENUS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_MENU_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Menu m = new Menu();
                m.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                m.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MENU_NAME)));
                list.add(m);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return list;
    }

    public void deleteMenu(int menuId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // remove relações primeiro
        db.delete(TABLE_MENU_DRINK,
                COLUMN_MENU_ID + "=?",
                new String[]{String.valueOf(menuId)});

        // remove menu
        db.delete(TABLE_MENUS,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(menuId)});

        db.close();
    }

    public void updateMenuDrinkOrder(int menuId, int drinkId, int order) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DISPLAY_ORDER, order);

        db.update(
                TABLE_MENU_DRINK,
                values,
                COLUMN_MENU_ID + "=? AND " + COLUMN_DRINK_ID + "=?",
                new String[]{
                        String.valueOf(menuId),
                        String.valueOf(drinkId)
                }
        );

        db.close();
    }


}