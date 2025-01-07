package com.example.labmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class TakeAttendanceActivity extends AppCompatActivity {
    Spinner subjectSpinner;
    Button btn_startAttendanceSession;
    String[] subjectArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_attendance);

        Init();
        Buttons();
    }

    private void Buttons() {
        btn_startAttendanceSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSelectionValid()) {
                    String selectedSubject = subjectSpinner.getSelectedItem().toString();

                    Intent intent = new Intent(getApplicationContext(), activity_session.class);

                    intent.putExtra("Course Code", selectedSubject);

                    startActivity(intent);
                } else {
                    Toast.makeText(TakeAttendanceActivity.this, "Please select all three options", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isSelectionValid() {
        return
                (subjectSpinner.getSelectedItemPosition() > 0);
    }

    private void Init() {
        subjectSpinner = findViewById(R.id.subjectSpinner);

        btn_startAttendanceSession = findViewById(R.id.btn_startAttendanceSession);

        subjectArray = getResources().getStringArray(R.array.subject_array);
        ArrayAdapter<String> subjectArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_list, subjectArray);
        subjectArrayAdapter.setDropDownViewResource(R.layout.spinner_list);
        subjectSpinner.setAdapter(subjectArrayAdapter);
        subjectSpinner.setSelection(0, false);
    }

}