package com.example.labmate;

import static org.apache.poi.util.StringUtil.length;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.bumptech.glide.Glide;

public class activity_session extends AppCompatActivity {
    String subject;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        ImageView gifImageView = findViewById(R.id.loading);

        Button btn = findViewById(R.id.end_session);

        Glide.with(this).asGif().load(R.drawable.loading).into(gifImageView);
        Intent intent = getIntent();
        if (intent != null) {
            subject = intent.getStringExtra("courseCode");
        }
        createSession();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),AttendanceReportActivity.class);
                intent.putExtra("sessionId", sessionId);
                Log.d("session","session id " +sessionId);
                startActivity(intent);
            }
    });
        }

    private void createSession() {
        DatabaseReference attendanceReportRef = FirebaseDatabase.getInstance().getReference("AttendanceReport");
        attendanceReportRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("report","data date : "+dataSnapshot);
                if (dataSnapshot.exists()) {
                    long nextSessionId = dataSnapshot.getChildrenCount() + 1;
                    sessionId = "attendance_session_id_" + nextSessionId;
                    Log.d("report","nextSessionId : "+nextSessionId);
                    DatabaseReference newReportRef = attendanceReportRef.child(sessionId);

                    Date currentDate = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");  // You can change the format as needed
                    String formattedDate = dateFormat.format(currentDate);

                    Calendar calendar = Calendar.getInstance();
                    String amPm;
                    if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                        amPm = "AM";
                    } else {
                        amPm = "PM";
                    }
                    String startTime=calendar.get(Calendar.HOUR_OF_DAY)+" "+amPm;
                    int endTime=calendar.get(Calendar.HOUR_OF_DAY)+1;

                    if(endTime>12){
                        endTime=endTime-12;
                    }
                    String s_endTime=endTime+" "+amPm;
                    Log.d("report","period date : "+formattedDate);
                    Log.d("report","startTime : "+startTime);
                    Log.d("report","s_endTime : "+s_endTime);
                    Log.d("report","subject_name: "+subject);

                    newReportRef.child("period_date").setValue(formattedDate);
                    newReportRef.child("period_end_time").setValue(s_endTime);
                    newReportRef.child("period_start_time").setValue(startTime);
                    newReportRef.child("course_code").setValue(subject);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

    }
}