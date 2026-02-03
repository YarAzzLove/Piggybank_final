package com.example.piggybank;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "piggybank.db";
    private static final int DATABASE_VERSION = 1;

    // Название таблицы и столбцов
    private static final String TABLE_HISTORY = "coin_history";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOMINAL = "nominal"; // Номинал монеты: 0.5, 1, 2, 5, 10
    private static final String COLUMN_TIMESTAMP = "timestamp";

    // Фиксированные номиналы монет как в курсовой копилке
    public static final double[] COIN_NOMINALS = {0.5, 1.0, 2.0, 5.0, 10.0};
    public static final String CURRENCY = "RUB"; // Валюта как в курсовой

    // SQL-запрос для создания таблицы
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_HISTORY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMINAL + " REAL NOT NULL, " +
                    COLUMN_TIMESTAMP + " TEXT NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    // Метод для добавления монеты определенного номинала
    public boolean addCoin(double nominal) {
        // Проверяем, что номинал допустимый (соответствует копилке)
        if (!isValidNominal(nominal)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NOMINAL, nominal);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        cv.put(COLUMN_TIMESTAMP, sdf.format(new Date()));

        long result = db.insert(TABLE_HISTORY, null, cv);
        db.close();
        return result != -1; // вернёт true если запись добавлена
    }

    // Проверка корректности номинала
    private boolean isValidNominal(double nominal) {
        for (double validNominal : COIN_NOMINALS) {
            if (Math.abs(validNominal - nominal) < 0.001) {
                return true;
            }
        }
        return false;
    }

    // Метод для получения всей истории
    public ArrayList<CoinEntry> getAllHistory() {
        ArrayList<CoinEntry> historyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_HISTORY,
                new String[]{COLUMN_ID, COLUMN_NOMINAL, COLUMN_TIMESTAMP},
                null, null, null, null, COLUMN_TIMESTAMP + " DESC"); // Сортировка по дате (новые сверху)

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                double nominal = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NOMINAL));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));

                // Создаем запись для каждой монеты (count = 1)
                historyList.add(new CoinEntry(id, nominal, 1, timestamp));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return historyList;
    }

    // Метод для получения общей суммы
    public double getTotalAmount() {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_NOMINAL + ") FROM " + TABLE_HISTORY, null);
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    // Метод для получения статистики по номиналам (как в курсовой копилке)
    public HashMap<Double, Integer> getCoinStatistics() {
        HashMap<Double, Integer> stats = new HashMap<>();

        // Инициализируем все номиналы нулями
        for (double nominal : COIN_NOMINALS) {
            stats.put(nominal, 0);
        }

        SQLiteDatabase db = this.getReadableDatabase();
        // Группируем по номиналам и считаем количество
        String query = "SELECT " + COLUMN_NOMINAL + ", COUNT(*) as count FROM " +
                TABLE_HISTORY + " GROUP BY " + COLUMN_NOMINAL;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                double nominal = cursor.getDouble(0);
                int count = cursor.getInt(1);
                stats.put(nominal, count);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return stats;
    }

    // Метод для получения количества монет каждого номинала (для отображения)
    public HashMap<Double, CoinStats> getDetailedStatistics() {
        HashMap<Double, CoinStats> detailedStats = new HashMap<>();

        // Инициализируем структуру для каждого номинала
        for (double nominal : COIN_NOMINALS) {
            detailedStats.put(nominal, new CoinStats(nominal, 0, 0.0));
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_NOMINAL + ", COUNT(*) as count FROM " +
                TABLE_HISTORY + " GROUP BY " + COLUMN_NOMINAL;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                double nominal = cursor.getDouble(0);
                int count = cursor.getInt(1);
                double totalValue = nominal * count;
                detailedStats.put(nominal, new CoinStats(nominal, count, totalValue));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return detailedStats;
    }

    // Метод для очистки истории (аналог сброса статистики на устройстве)
    public void clearHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, null, null);
        db.close();
    }

    // Метод для получения последних N записей
    public ArrayList<CoinEntry> getRecentHistory(int limit) {
        ArrayList<CoinEntry> historyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_HISTORY,
                new String[]{COLUMN_ID, COLUMN_NOMINAL, COLUMN_TIMESTAMP},
                null, null, null, null,
                COLUMN_TIMESTAMP + " DESC",
                String.valueOf(limit)); // Ограничиваем количество записей

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                double nominal = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NOMINAL));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                historyList.add(new CoinEntry(id, nominal, 1, timestamp));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return historyList;
    }

    // Вспомогательный класс для хранения статистики по номиналу
    public static class CoinStats {
        public double nominal;
        public int count;
        public double totalValue;

        public CoinStats(double nominal, int count, double totalValue) {
            this.nominal = nominal;
            this.count = count;
            this.totalValue = totalValue;
        }
    }
}