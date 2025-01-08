package com.example.labmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

                    if (hasLocationPermissions()) {
                        saveAttendanceSession(selectedSubject);
                    }

                    Intent intent = new Intent(getApplicationContext(), activity_session.class);

                    intent.putExtra("courseCode", selectedSubject);

                    startActivity(intent);
                } else {
                    Toast.makeText(TakeAttendanceActivity.this, "Please select option", Toast.LENGTH_SHORT).show();
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

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void saveAttendanceSession(String courseCode) {
        String currentBSSID = getCurrentBSSID();
        if (currentBSSID != null) {
            mDatabase.child("attendance_sessions")
                    .child("UniversityBssid")
                    .setValue(currentBSSID)
                    .addOnSuccessListener(aVoid -> Log.d("TakeAttendance", "BSSID saved successfully" + currentBSSID))
                    .addOnFailureListener(e -> Log.e("TakeAttendance", "Failed to save BSSID: " + e.getMessage()));
        } else {
            Toast.makeText(this, "Unable to fetch Wi-Fi BSSID. Make sure you are connected to a network.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentBSSID() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            Log.d("SubmitAttendance", "Fetched BSSID: " + wifiInfo.getBSSID() + " 2 " + wifiInfo.getSSID() + " 3 "+ wifiInfo.getMacAddress() + " 4 " + wifiInfo.getNetworkId());
            return wifiInfo.getBSSID();
        }
        return null;
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123; // You can use any value

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveAttendanceSession(subjectSpinner.getSelectedItem().toString());
            } else {
                Toast.makeText(this, "Please enable Location permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
