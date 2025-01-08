package com.example.labmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TakeAttendanceActivity extends AppCompatActivity {
    Spinner subjectSpinner;
    Button btn_startAttendanceSession;
    String[] subjectArray;
    private DatabaseReference mDatabase;

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

                    saveAttendanceSession(selectedSubject);

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
        return (subjectSpinner.getSelectedItemPosition() > 0);
    }

    private void Init() {
        subjectSpinner = findViewById(R.id.subjectSpinner);
        btn_startAttendanceSession = findViewById(R.id.btn_startAttendanceSession);
        subjectArray = getResources().getStringArray(R.array.subject_array);
        ArrayAdapter<String> subjectArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_list, subjectArray);
        subjectArrayAdapter.setDropDownViewResource(R.layout.spinner_list);
        subjectSpinner.setAdapter(subjectArrayAdapter);
        subjectSpinner.setSelection(0, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void saveAttendanceSession(String courseCode) {
        String currentBSSID = getCurrentBSSID();
        if (currentBSSID != null) {
            mDatabase.child("attendance_sessions")
                    .child("UniversityBssid")
                    .setValue(currentBSSID)
                    .addOnSuccessListener(aVoid -> Log.d("TakeAttendance", "BSSID saved successfully"))
                    .addOnFailureListener(e -> Log.e("TakeAttendance", "Failed to save BSSID: " + e.getMessage()));
        } else {
            Toast.makeText(this, "Unable to fetch Wi-Fi BSSID. Make sure you are connected to a network.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentBSSID() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getBSSID();
        }
        return null;
    }
}
