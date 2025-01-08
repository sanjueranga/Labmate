package com.example.labmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceReportActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 1;
    private ProgressDialog progressDialog;
    private FirebaseDatabase firebaseDatabase;
    private Button downloadButton;
    private SimpleDateFormat sdf;
    private int rowNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

        downloadButton = findViewById(R.id.download);
        progressDialog = new ProgressDialog(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.US);

        // Find TextView elements
        TextView studentsNumberText = findViewById(R.id.text_students_number);
        TextView presentNumberText = findViewById(R.id.text_present_number);
        TextView absentNumberText = findViewById(R.id.text_absent_number);

        // Populate attendance data on activity render
        populateAttendanceCounts(studentsNumberText, presentNumberText, absentNumberText);

        downloadButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST);
            } else {
                exportExcelFile();
            }
        });
    }

    private void exportExcelFile() {
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        Intent intent = getIntent();
        String sessionId = intent.getStringExtra("sessionId");

        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(this, "Invalid session ID", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Generating report...");
        progressDialog.show();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    DataSnapshot sessionData = dataSnapshot.child("AttendanceReport").child(sessionId);
                    DataSnapshot studentsData = dataSnapshot.child("Students");
                    DataSnapshot attendanceData = dataSnapshot.child("attendance");

                    File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    String fileName = "AttendanceReport_" + sdf.format(new Date()) + ".xlsx";
                    File file = new File(downloadDir, fileName);

                    Workbook workbook = new XSSFWorkbook();
                    Sheet sheet = workbook.createSheet("Attendance Report");

                    // Create header row
                    Row headerRow = sheet.createRow(rowNum++);
                    headerRow.createCell(0).setCellValue("Enrollment No");
                    headerRow.createCell(1).setCellValue("Student Name");
                    headerRow.createCell(2).setCellValue("Email");
                    headerRow.createCell(3).setCellValue("Attendance Status");

                    // Fetch session course code
                    String courseCode = sessionData.child("course_code").getValue(String.class);

                    // Populate student data
                    for (DataSnapshot student : studentsData.getChildren()) {
                        String studentCourseCode = student.child("course_code").getValue(String.class);

                        // Only include students for the session's course
                        if (courseCode.equals(studentCourseCode)) {
                            String enrollmentNo = student.getKey();
                            String name = student.child("student_name").getValue(String.class);
                            String email = student.child("student_email").getValue(String.class);
                            String attendanceStatus = "Absent"; // Default to absent

                            for (DataSnapshot attendance : attendanceData.getChildren()) {
                                if (email.equals(attendance.child("userEmail").getValue(String.class))
                                        && sessionData.child("period_date").getValue(String.class)
                                        .equals(attendance.child("date").getValue(String.class))
                                        && sessionData.child("period_start_time").getValue(String.class)
                                        .equals(attendance.child("time").getValue(String.class)))
                                {
                                    attendanceStatus = "Present";
                                    break;
                                }
                            }

                            Row row = sheet.createRow(rowNum++);
                            row.createCell(0).setCellValue(enrollmentNo);
                            row.createCell(1).setCellValue(name);
                            row.createCell(2).setCellValue(email);
                            row.createCell(3).setCellValue(attendanceStatus);
                        }
                    }

                    FileOutputStream fileOut = new FileOutputStream(file);
                    workbook.write(fileOut);
                    fileOut.close();
                    workbook.close();

                    Toast.makeText(getApplicationContext(), "Report saved to Downloads", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("Export Error", e.getMessage(), e);
                    Toast.makeText(getApplicationContext(), "Failed to create Excel file", Toast.LENGTH_SHORT).show();
                } finally {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateAttendanceCounts(TextView studentsNumberText, TextView presentNumberText, TextView absentNumberText) {
        Intent intent = getIntent();
        String sessionId = intent.getStringExtra("sessionId");

        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(this, "Invalid session ID", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Loading data...");
        progressDialog.show();

        DatabaseReference databaseReference = firebaseDatabase.getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    DataSnapshot sessionData = dataSnapshot.child("AttendanceReport").child(sessionId);
                    DataSnapshot studentsData = dataSnapshot.child("Students");
                    DataSnapshot attendanceData = dataSnapshot.child("attendance");

                    String courseCode = sessionData.child("course_code").getValue(String.class);

                    // Initialize counters
                    int totalStudents = 0;
                    int presentCount = 0;
                    int absentCount = 0;

                    for (DataSnapshot student : studentsData.getChildren()) {
                        String studentCourseCode = student.child("course_code").getValue(String.class);

                        if (courseCode.equals(studentCourseCode)) {
                            totalStudents++;
                            String email = student.child("student_email").getValue(String.class);
                            boolean isPresent = false;

                            for (DataSnapshot attendance : attendanceData.getChildren()) {
                                if (email.equals(attendance.child("userEmail").getValue(String.class))
                                        && sessionData.child("period_date").getValue(String.class)
                                        .equals(attendance.child("date").getValue(String.class))
                                        && sessionData.child("period_start_time").getValue(String.class)
                                        .equals(attendance.child("time").getValue(String.class))) {
                                    isPresent = true;
                                    break;
                                }
                            }

                            if (isPresent) {
                                presentCount++;
                            } else {
                                absentCount++;
                            }
                        }
                    }

                    // Update the UI with the counts
                    studentsNumberText.setText(String.valueOf(totalStudents));
                    presentNumberText.setText(String.valueOf(presentCount));
                    absentNumberText.setText(String.valueOf(absentCount));

                } catch (Exception e) {
                    Log.e("Data Error", e.getMessage(), e);
                    Toast.makeText(getApplicationContext(), "Failed to load attendance data", Toast.LENGTH_SHORT).show();
                } finally {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
