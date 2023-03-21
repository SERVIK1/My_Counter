package ru.raspgleb.p2p;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Context context = this;
    // Поле счётчика
    TextView num_text;
    // Инкремент, Декремент, Сброс, Сохранить
    Button click, dec, reset, save;
    private String STAGE_KEY = "STAGE"; // Ключ к БД
    private DatabaseReference myDataBase;
    private ArrayList<Stage> stages = new ArrayList<>(); // Массив объектов сохранения
    private ListView main_list;
    private CounterAdapter counterAdapter; // Адаптер

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_list = findViewById(R.id.main_list);
        myDataBase = FirebaseDatabase.getInstance().getReference(STAGE_KEY);
        counterAdapter = new CounterAdapter(context,stages);
        main_list.setAdapter(counterAdapter);
        // Выгрузка элементов из БД
        ValueEventListener stageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Очистка для добавления существующих
                if(stages.size() > 0)
                    stages.clear();
                // Перебор элементов БД
                for (DataSnapshot stageSnapshot: dataSnapshot.getChildren()) {
                    Stage stage_db = stageSnapshot.getValue(Stage.class);
                    assert stage_db != null;

                    stages.add(new Stage(stage_db.getName(), stage_db.getNumeric()));
                    counterAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        myDataBase.addValueEventListener(stageListener);


        setTitle("TabHost");

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();
        // Вкладка счётчика
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.counter);
        tabSpec.setIndicator("СЧЁТЧИК");
        tabHost.addTab(tabSpec);

        // Вкладка сохранённых значений
        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.list);
        tabSpec.setIndicator("Список");
        tabHost.addTab(tabSpec);
        num_text = findViewById(R.id.numbers);
        click = findViewById(R.id.button_click);
        dec = findViewById(R.id.button_dec);
        reset = findViewById(R.id.button_recet);
        save = findViewById(R.id.button_save);
        click.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int val = Integer.parseInt(num_text.getText().toString());
                // Установка лимита для счётчика
                if(val<999999){
                    val+=1;
                    num_text.setText(String.format(Locale.US,"%d",val));
                }

            }
        });
        dec.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int val = Integer.parseInt(num_text.getText().toString());
                if(val>1){
                    val-=1;
                    num_text.setText(String.format(Locale.US,"%d",val));
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                    num_text.setText("1");
                }
        });
        // Сохранение значения
        save.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        LayoutInflater li = LayoutInflater.from(context);

                                        View promptsView = li.inflate(R.layout.window, null);

                                        //Создаем AlertDialog
                                        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);

                                        //Настраиваем window.xml для нашего AlertDialog:
                                        mDialogBuilder.setView(promptsView);

                                        //Настраиваем отображение поля для ввода текста в открытом диалоге:
                                        final EditText userInput = promptsView.findViewById(R.id.input_text);
                                        final EditText userInput_num =  promptsView.findViewById(R.id.input_text2);
                                        userInput_num.setHint(num_text.getText().toString());
                                        userInput_num.setText(num_text.getText().toString());

                                        //Настраиваем сообщение в диалоговом окне:
                                        mDialogBuilder
                                                .setCancelable(false)
                                                // Подтверждение на добавление новой задачи
                                                .setPositiveButton("OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                // Добавление в БД
                                                                myDataBase.push().setValue(new Stage(userInput.getText().toString(),userInput_num.getText().toString()));
                                                                // Занесение в список объектов
                                                                stages.add(new Stage(userInput.getText().toString(),userInput_num.getText().toString()));
                                                                counterAdapter.notifyDataSetChanged();
                                                            }

                                                        })
                                                .setNegativeButton("Отмена",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                            }
                                                        });

                                        //Создаем AlertDialog:
                                        AlertDialog alertDialog = mDialogBuilder.create();
                                        //и отображаем его:
                                        alertDialog.show();
                                        Button okey = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                        // Модификация цвета
                                        okey.setTextColor(Color.YELLOW);
                                        Button cancel = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                                        cancel.setTextColor(Color.GRAY);
                                        // Обработка автоматического закрытия диалогового окна
                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                boolean wantToCloseDialog = false;
                                                // Сообщение об ошибке
                                                TextView error_message = promptsView.findViewById(R.id.error_message);
                                                // Условие для числового поля
                                                if(!userInput_num.getText().toString().startsWith("0") && !userInput_num.getText().toString().equals("")){
                                                    error_message.setText("");
                                                    wantToCloseDialog = true;
                                                    // Добавление в БД
                                                    myDataBase.push().setValue(new Stage(userInput.getText().toString(),userInput_num.getText().toString()));
                                                    // Занесение в список объектов
                                                    stages.add(new Stage(userInput.getText().toString(),userInput_num.getText().toString()));
                                                    counterAdapter.notifyDataSetChanged();
                                                    alertDialog.dismiss();
                                                }
                                                else{
                                                    error_message.setText("Введите корректное значение!");
                                                    wantToCloseDialog = false;

                                                }
                                            }
                                        });
                                    }
                                });

        tabHost.setCurrentTab(0);

    }
}