package ru.raspgleb.p2p;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// Адаптер для отображения сохранённых элементов
public class CounterAdapter extends ArrayAdapter<Stage> {
    private String STAGE_KEY = "STAGE"; // Ключ к БД
    private DatabaseReference myDataBase = FirebaseDatabase.getInstance().getReference(STAGE_KEY); // Получение БД
    private Context context;
    private ArrayList<Stage> stages; // Массив объектов сохранения

    public CounterAdapter(Context context, ArrayList<Stage> stages) {
        super(context, R.layout.save_stage, stages);
        this.context = context;
        this.stages = stages;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.save_stage, parent, false);

        // Отображение названия
        TextView name = view.findViewById(R.id.name_stage);
        name.setText(this.stages.get(position).getName());
        // Отображение числа
        TextView numeric = view.findViewById(R.id.num_stage);
        numeric.setText(this.stages.get(position).getNumeric());

        // Обработка нажатия на сохранённый элемент
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(context);

                View promptsView = li.inflate(R.layout.window, null);
                //Создаем AlertDialog
                AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);

                //Настраиваем window.xml для нашего AlertDialog:
                mDialogBuilder.setView(promptsView);


                //Настраиваем отображение поля для ввода текста в открытом диалоге:
                final TextView title = promptsView.findViewById(R.id.tv);
                title.setText("Редактирование данных");
                // Пользовательские значения из ввода
                final EditText userInput = promptsView.findViewById(R.id.input_text);
                final EditText userInput_num = promptsView.findViewById(R.id.input_text2);
                userInput.setHint(CounterAdapter.this.stages.get(position).getName());
                userInput.setText(CounterAdapter.this.stages.get(position).getName());
                userInput_num.setHint(CounterAdapter.this.stages.get(position).getNumeric());
                userInput_num.setText(CounterAdapter.this.stages.get(position).getNumeric());

                //Настраиваем сообщение в диалоговом окне:
                mDialogBuilder
                        .setCancelable(false)
                        // Подтверждение на редактирование позиции
                        .setPositiveButton("Сохранить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ValueEventListener stageListener1 = new ValueEventListener() {
                                            // Счётчик элементов БД
                                            int i = 0;
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                // Перебор элементов БД
                                                for (DataSnapshot stageSnapshot: dataSnapshot.getChildren()) {
                                                    Stage stage_db = stageSnapshot.getValue(Stage.class);
                                                    assert stage_db != null;
                                                    System.out.println(position);
                                                    if(i==position){
                                                        stageSnapshot.getRef().child("name").setValue(userInput.getText().toString());
                                                        stageSnapshot.getRef().child("numeric").setValue(userInput_num.getText().toString());
                                                        stages.set(position,new Stage(userInput.getText().toString(),userInput_num.getText().toString()));
                                                    }
                                                    i++;
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                // Getting Post failed, log a message
                                                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                            }
                                        };
                                        myDataBase.addValueEventListener(stageListener1);
                                    }

                                })
                        // Удаление из БД выбранной позиции
                        .setNegativeButton("УДАЛИТЬ",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ValueEventListener stageListener = new ValueEventListener() {
                                            // Счётчик элементов БД
                                            int i = 0;
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                // Перебор элементов БД
                                                for (DataSnapshot stageSnapshot: dataSnapshot.getChildren()) {
                                                    System.out.println(i);
                                                    System.out.println(position);
                                                    Stage stage_db = stageSnapshot.getValue(Stage.class);
                                                    assert stage_db != null;
                                                    if(i==position){
                                                        stageSnapshot.getRef().removeValue();
                                                        stages.remove(position);
                                                    }
                                                    i++;
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                // Getting Post failed, log a message
                                                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                            }
                                        };
                                        myDataBase.addValueEventListener(stageListener);
                                        dialog.cancel();
                                    }
                                });

                //Создаем AlertDialog:
                AlertDialog alertDialog = mDialogBuilder.create();
                //и отображаем его:
                alertDialog.show();
                // Модификация цвета для кнопок
                Button okey = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                okey.setTextColor(Color.YELLOW);
                Button cancel = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                cancel.setTextColor(Color.GRAY);
                // Обработка автоматического закрытия диалогового окна
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        boolean wantToCloseDialog = false;
                        // Сообщение об ошибке
                        TextView error_message = promptsView.findViewById(R.id.error_message);
                        // Условие для числового поля
                        if(!userInput_num.getText().toString().startsWith("0") && !userInput_num.getText().toString().equals("")){
                            ValueEventListener stageListener1 = new ValueEventListener() {
                                // Счётчик элементов БД
                                int i = 0;
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Перебор элементов БД
                                    for (DataSnapshot stageSnapshot: dataSnapshot.getChildren()) {
                                        Stage stage_db = stageSnapshot.getValue(Stage.class);
                                        assert stage_db != null;
                                        if(i==position){
                                            stageSnapshot.getRef().child("name").setValue(userInput.getText().toString());
                                            stageSnapshot.getRef().child("numeric").setValue(userInput_num.getText().toString());
                                            stages.set(position,new Stage(userInput.getText().toString(),userInput_num.getText().toString()));
                                        }
                                        i++;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Getting Post failed, log a message
                                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                }
                            };
                            myDataBase.addValueEventListener(stageListener1);
                            error_message.setText("");
                            wantToCloseDialog = true;
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


        return view;

    }
}
