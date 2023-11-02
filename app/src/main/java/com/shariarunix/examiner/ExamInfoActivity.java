package com.shariarunix.examiner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.shariarunix.examiner.DataModel.ExamDataModel;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ExamInfoActivity extends AppCompatActivity {
    TextView txtExamName, txtExamDate, txtExamTime, txtExamSyllabus, txtExamTotalMarks, txtExamDuration;
    AppCompatButton btnAction;
    SharedPreferences sharedPreferences;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_info);

        sharedPreferences = getSharedPreferences("examinerPref", MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "");

        txtExamName = findViewById(R.id.txt_exam_name);
        txtExamDate = findViewById(R.id.txt_exam_date);
        txtExamTime = findViewById(R.id.txt_exam_time);
        txtExamSyllabus = findViewById(R.id.txt_exam_syllabus);
        txtExamTotalMarks = findViewById(R.id.txt_exam_total_marks);
        txtExamDuration = findViewById(R.id.txt_exam_duration);

        btnAction = findViewById(R.id.btn_action);

        ExamDataModel newExamData = (ExamDataModel) getIntent().getSerializableExtra("examData");

        assert newExamData != null;
        txtExamName.setText(newExamData.getExamName());
        txtExamDate.setText(newExamData.getExamDate());
        txtExamTime.setText(newExamData.getExamTime());
        txtExamSyllabus.setText(newExamData.getExamSyllabus());
        txtExamTotalMarks.setText(newExamData.getTotalMarks());
        txtExamDuration.setText(newExamData.getDuration());

        // Splitting the Date and Time for setting alarm on calender app
        int[] examTimeArr = normalToInt(newExamData.getExamTime());
        String[] examDateArr = newExamData.getExamDate().split("/");

        // Exam Start time with second
        String examStartTimeWithSecond = addSecondToTime(newExamData.getExamTime());

        // Declaring Date and Time format
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            // Parsing all time to SimpleTimeFormat
            Date examStartTime = simpleTimeFormat.parse(examStartTimeWithSecond);
            Date examEndTime = addExamDuration(examStartTime, newExamData.getDuration());
            Date presentTime = simpleTimeFormat.parse(getPresentTime());
            // Parsing all date to SimpleDateFormat
            Date examDate = simpleDateFormat.parse(newExamData.getExamDate());
            Date presentDate = simpleDateFormat.parse(getPresentDate());

            assert presentDate != null;
            if (presentDate.before(examDate)) {
                // If Exam date is before than Present date then this condition will be applied.
                // Opening calender app for setting alarm.

                goToCalender(examTimeArr, examDateArr, newExamData);
            } else if (presentDate.after(examDate)) {
                // If Exam date is after than Present date then this condition will be applied.
                // Changing the action button text.
                // Performing back pressed method on clicking action button.

                btnAction.setText("Exam Finished");
                btnAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            } else {
                // If Exam date and Present date match with each other then this condition will be applied.

                assert presentTime != null;
                if (presentTime.after(examStartTime) && presentTime.before(examEndTime)) {
                    // If Exam Start time is after Present time and Exam End time is before Present time then this condition will be applied.

                    if (newExamData.getUsersList().contains(userID)) {
                        // If user already completed the test then this condition will be applied.
                        // Changing the action button text.
                        // Performing back pressed method on clicking action button.

                        btnAction.setText("You've Completed The Test");
                        btnAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onBackPressed();
                            }
                        });
                    } else {
                        // If user have not completed the test then this condition will be applied.
                        // Changing the action button text.
                        // Go to Exam activity for performing the exam.

                        btnAction.setText("Give Exam");
                        btnAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent examPage = new Intent(ExamInfoActivity.this, ExamActivity.class);
                                examPage.putExtra("examDataModel", newExamData);

                                startActivity(examPage);
                            }
                        });
                    }
                } else if (presentTime.before(examStartTime)) {
                    // If Exam Start time is before then Present time then this condition will be applied.
                    // Opening the calender app for setting alarm.

                    goToCalender(examTimeArr, examDateArr, newExamData);
                } else if (presentTime.after(examEndTime)) {
                    // If Exam End time is after then Present time then this condition will be applied.

                    if (newExamData.getUsersList().contains(userID)){
                        // If user already completed the test then this condition will be applied.
                        // Changing the action button text.
                        // Performing back pressed method on clicking action button.

                        btnAction.setText("You've Completed The Test");
                        btnAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onBackPressed();
                            }
                        });
                    } else {
                        // If user have not completed the test then this condition will be applied.
                        // Changing the action button text.
                        // Performing back pressed method on clicking action button.

                        btnAction.setText("Exam Finished");
                        btnAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onBackPressed();
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(ExamInfoActivity.this, "Something went wrong, Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    // Going to calender app and setting an alarm there.
    private void goToCalender(int[] examTimeArr, String[] examDateArr, ExamDataModel newExamData) {
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openCalender = new Intent(Intent.ACTION_INSERT);
                openCalender.setType("vnd.android.cursor.item/event");

                openCalender.putExtra(CalendarContract.Events.TITLE, "" + newExamData.getExamName());

                openCalender.putExtra(CalendarContract.Events.DESCRIPTION, "Exam Syllabus : \n" + newExamData.getExamSyllabus());

                GregorianCalendar calDate = new GregorianCalendar(Integer.parseInt(examDateArr[2]), Integer.parseInt(examDateArr[1]) - 1, Integer.parseInt(examDateArr[0]), examTimeArr[0], examTimeArr[1], 0);

                openCalender.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calDate.getTimeInMillis());
                openCalender.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calDate.getTimeInMillis());
                startActivity(openCalender);
            }
        });
    }
    // Adding exam duration to exam start time and getting the exam end time
    private Date addExamDuration(Date examStartTime, String duration) {
        Calendar calendar = Calendar.getInstance();
        assert examStartTime != null;
        calendar.setTime(examStartTime);

        calendar.add(Calendar.MINUTE, Integer.parseInt(duration));

        return calendar.getTime();
    }
    // Adding second to time
    private String addSecondToTime(String examTime) {
        String[] examTimeArr = examTime.split(" ");

        return examTimeArr[0] + ":00 " + examTimeArr[1];
    }
    // Get User's Device Time
    private String getPresentTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sTimeFormat = new SimpleDateFormat("hh:mm:ss a");
        sTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));
        return sTimeFormat.format(new Date());
    }
    // Get User's Device Date
    private String getPresentDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        sDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));
        return sDateFormat.format(new Date());
    }
    // Converting Time to 24 hr format
    private int[] normalToInt(String time) {
        String[] newTime = time.split(" ");
        String[] hrMin = newTime[0].split(":");

        int hr, min;

        if (newTime[1].equals("PM") || newTime[1].equals("Pm") || newTime[1].equals("pm")) {
            hr = Integer.parseInt(hrMin[0]);
            if (hr < 12) {
                hr += 12;
            }
            min = Integer.parseInt(hrMin[1]);

        } else {
            hr = Integer.parseInt(hrMin[0]);
            if (hr == 12) {
                hr = 0;
            }
            min = Integer.parseInt(hrMin[1]);
        }

        return new int[]{hr, min};
    }
}