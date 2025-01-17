package com.example.labmate;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SelectAttendanceActivity extends AppCompatActivity {
    private Button btnSubmitAttendance;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_attendance);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize button
        btnSubmitAttendance = findViewById(R.id.btn_login);

        btnSubmitAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordAttendance();
            }
        });
    }

    private void recordAttendance() {
        Log.d("record","current User"+ mAuth.getCurrentUser());
        String userId = mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();
        Log.d("record","current User email"+ userId);
        String currentDate = new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(new Date());

        Calendar calendar = Calendar.getInstance();
        String amPm;
        if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
            amPm = "AM";
        } else {
            amPm = "PM";
        }
        String currentTime=calendar.get(Calendar.HOUR_OF_DAY)+" "+amPm;

//        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create attendance record
        AttendanceRecord record = new AttendanceRecord(
                userEmail,
                currentDate,
                currentTime
        );
        Log.d("record","record:"+ record);
        // Save to Firebase
        mDatabase.child("attendance")
                .child(userId)
                .setValue(record)
                .addOnSuccessListener(aVoid -> {
                    // Redirect to presence recorded activity
                    Intent intent = new Intent(SelectAttendanceActivity.this, activity_presence_recorded.class);
                    startActivity(intent);
                    finish(); // Optional: close this activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SelectAttendanceActivity.this,
                            "Failed to record attendance: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
        Log.d("record","done:");
    }

    // Attendance record model class
    private static class AttendanceRecord {
        public String userEmail;
        public String date;
        public String time;

        public AttendanceRecord(String email, String date, String time) {
            this.userEmail = email;
            this.date = date;
            this.time = time;
        }
    }
}