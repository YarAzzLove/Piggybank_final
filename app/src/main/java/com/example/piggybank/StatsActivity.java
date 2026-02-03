package com.example.piggybank;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity {

    private TextView tvSummary;
    private TextView tvDistribution;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        tvSummary = findViewById(R.id.tv_stats_summary);
        tvDistribution = findViewById(R.id.tv_distribution);
        dbHelper = new DatabaseHelper(this);

        // Рассчитываем и показываем статистику
        showStatistics();
    }

    private void showStatistics() {
        // 1. Получаем статистику по номиналам
        HashMap<Double, DatabaseHelper.CoinStats> stats = dbHelper.getDetailedStatistics();
        double totalAmount = dbHelper.getTotalAmount();
        int totalCoins = 0;

        // 2. Подсчитываем общее количество монет
        for (DatabaseHelper.CoinStats coinStats : stats.values()) {
            totalCoins += coinStats.count;
        }

        // 3. Формируем текстовую сводку
        String summary = String.format(Locale.getDefault(),
                "💰 Общая сумма: %.2f руб\n" +
                        "🔢 Всего монет: %d\n" +
                        "🏦 Валюта: %s",
                totalAmount, totalCoins, DatabaseHelper.CURRENCY);

        tvSummary.setText(summary);

        if (totalCoins == 0) {
            tvDistribution.setText("Нет данных для анализа.\nДобавьте монеты на главном экране.");
            return;
        }

        // 4. Формируем текст распределения
        StringBuilder distributionText = new StringBuilder();
        distributionText.append("📊 Распределение по номиналам:\n\n");

        for (double nominal : DatabaseHelper.COIN_NOMINALS) {
            DatabaseHelper.CoinStats coinStats = stats.get(nominal);
            if (coinStats.count > 0) {
                float percentage = (float) ((coinStats.totalValue / totalAmount) * 100);
                distributionText.append(String.format(Locale.getDefault(),
                        "• %.1f руб: %d шт. = %.2f руб (%.1f%%)\n",
                        coinStats.nominal,
                        coinStats.count,
                        coinStats.totalValue,
                        percentage));
            }
        }

        // 5. Добавляем среднюю монету
        if (totalCoins > 0) {
            double averageCoin = totalAmount / totalCoins;
            distributionText.append(String.format(Locale.getDefault(),
                    "\n📈 Средняя монета: %.2f руб", averageCoin));
        }

        tvDistribution.setText(distributionText.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем статистику при возвращении на экран
        showStatistics();
    }
}