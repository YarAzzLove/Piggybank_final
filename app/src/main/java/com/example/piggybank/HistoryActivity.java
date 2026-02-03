package com.example.piggybank;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private ListView listView;
    private TextView tvEmpty;
    private DatabaseHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> historyStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listView = findViewById(R.id.lv_history);
        tvEmpty = findViewById(R.id.tv_empty);
        Button btnClear = findViewById(R.id.btn_clear_history);
        dbHelper = new DatabaseHelper(this);

        // Загрузка истории и отображение
        loadHistory();

        // Кнопка очистки истории
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle("Подтверждение")
                        .setMessage("Удалить всю историю внесений?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper.clearHistory();
                                loadHistory(); // Обновляем пустой список
                                Toast.makeText(HistoryActivity.this, "История очищена", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Нет", null)
                        .show();
            }
        });
    }

    private void loadHistory() {
        // Получаем список объектов CoinEntry из БД
        ArrayList<CoinEntry> historyList = dbHelper.getAllHistory();
        historyStrings = new ArrayList<>();

        if (historyList.isEmpty()) {
            // Если история пуста, показываем сообщение
            tvEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            historyStrings.add("История пуста");
        } else {
            tvEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

            for (CoinEntry entry : historyList) {
                // Форматируем запись для отображения
                String record = String.format(Locale.getDefault(),
                        "%s: %.1f руб",
                        entry.getTimestamp(),
                        entry.getNominal());
                historyStrings.add(record);
            }
        }

        // Создаём простой адаптер для ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyStrings);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем историю при возвращении на экран
        loadHistory();
    }
}