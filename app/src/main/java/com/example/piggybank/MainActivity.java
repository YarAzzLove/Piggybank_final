package com.example.piggybank;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView tvTotal;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация UI элементов
        tvTotal = findViewById(R.id.tv_total);
        dbHelper = new DatabaseHelper(this);

        // Инициализация кнопок и установка слушателей
        Button btnSync = findViewById(R.id.btn_sync);
        Button btnHistory = findViewById(R.id.btn_history);
        Button btnStats = findViewById(R.id.btn_stats);
        Button btnGoals = findViewById(R.id.btn_goals);

        // Кнопки для добавления монет (фиксированные номиналы)
        Button btnCoin05 = findViewById(R.id.btn_coin_05);
        Button btnCoin1 = findViewById(R.id.btn_coin_1);
        Button btnCoin2 = findViewById(R.id.btn_coin_2);
        Button btnCoin5 = findViewById(R.id.btn_coin_5);
        Button btnCoin10 = findViewById(R.id.btn_coin_10);

        // 1. Обновляем отображение общей суммы при запуске
        updateTotal();

        // 2. Кнопка "Синхронизировать" (имитация)
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // В будущем здесь будет код Bluetooth для связи с реальной копилкой
                updateTotal();
                Toast.makeText(MainActivity.this, "Данные обновлены (имитация)", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Кнопки для добавления монет фиксированных номиналов
        btnCoin05.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCoin(0.5);
            }
        });

        btnCoin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCoin(1.0);
            }
        });

        btnCoin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCoin(2.0);
            }
        });

        btnCoin5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCoin(5.0);
            }
        });

        btnCoin10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCoin(10.0);
            }
        });

        // 4. Кнопка перехода на экран истории
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // 5. Кнопка перехода на экран статистики
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StatsActivity.class);
                startActivity(intent);
            }
        });

        // 6. Кнопка перехода на экран целей (заглушка)
        btnGoals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Раздел 'Цели' в разработке", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(MainActivity.this, GoalsActivity.class);
                // startActivity(intent);
            }
        });
    }

    // Метод для добавления монеты
    private void addCoin(double nominal) {
        boolean isAdded = dbHelper.addCoin(nominal);
        if (isAdded) {
            String message = String.format("Добавлена монета: %.1f руб", nominal);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            updateTotal();
        } else {
            Toast.makeText(this, "Ошибка: недопустимый номинал", Toast.LENGTH_SHORT).show();
        }
    }

    // Вспомогательный метод для обновления отображения общей суммы
    private void updateTotal() {
        double total = dbHelper.getTotalAmount();
        String formattedTotal = String.format("Общая сумма: %.2f руб", total);
        tvTotal.setText(formattedTotal);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем сумму, когда возвращаемся на главный экран
        updateTotal();
    }
}