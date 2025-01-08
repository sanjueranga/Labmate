package com.example.labmate;

import static java.lang.Thread.sleep;

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
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SubmitAttendanceActivity extends AppCompatActivity {
    Button btn_logout;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_attendance);
        Init();
        locationAccess();
    }

    private void checkWifi() {
        mDatabase.child("attendance_sessions").child("UniversityBssid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String savedBSSID = dataSnapshot.getValue(String.class);

                if (savedBSSID != null && isConnectedToSpecificNetwork(savedBSSID)) {
                    Log.d("Fetched SSID",savedBSSID);
                    startActivity(new Intent(getApplicationContext(), SelectAttendanceActivity.class));
                } else {
                    Toast.makeText(SubmitAttendanceActivity.this, "Not connected to the University Network.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SubmitAttendanceActivity.this, "Failed to fetch network information. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isConnectedToSpecificNetwork(String specificBSSID) {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String currentBSSID = wifiInfo.getBSSID();

            Log.d("SubmitAttendance", "Fetched BSSID of phone: " + currentBSSID);

            return specificBSSID.equals(currentBSSID);
        }
        return false;
    }

    private void Init () {
        btn_logout = findViewById(R.id.btn_logout);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });
        Log.d("SubmitAttendance", "Init Complete");
    }

    public void logOut() {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        FirebaseAuth.getInstance().signOut();
    }

    private void locationAccess(){
        if (hasLocationPermissions()) {
            checkWifi();
        } else {
            requestLocationPermissions();
        }
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
                checkWifi();
            } else {
                Toast.makeText(this, "Please enable Location permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
