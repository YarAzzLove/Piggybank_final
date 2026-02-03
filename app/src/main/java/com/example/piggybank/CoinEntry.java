package com.example.piggybank;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CoinEntry {
    private int id;
    private double nominal; // номинал монеты: 0.5, 1, 2, 5, 10
    private int count;      // количество монет (всегда 1 для каждой записи)
    private String timestamp;

    // Конструктор для создания новой записи
    public CoinEntry(double nominal) {
        this.nominal = nominal;
        this.count = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        this.timestamp = sdf.format(new Date());
    }

    // Конструктор для загрузки из БД
    public CoinEntry(int id, double nominal, int count, String timestamp) {
        this.id = id;
        this.nominal = nominal;
        this.count = count;
        this.timestamp = timestamp;
    }

    // Геттеры
    public int getId() { return id; }
    public double getNominal() { return nominal; }
    public int getCount() { return count; }
    public String getTimestamp() { return timestamp; }
    public double getTotalValue() { return nominal * count; }

    // Для отображения в истории
    public String getDisplayText() {
        return String.format(Locale.getDefault(), "%s: %.2f руб", timestamp, nominal);
    }
}