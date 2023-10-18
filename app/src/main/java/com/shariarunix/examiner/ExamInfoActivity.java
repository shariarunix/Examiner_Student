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
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
public class ExamInfoActivity extends AppCompatActivity {
    TextView txtExamName, txtExamDate, txtExamTime, txtExamSyllabus, txtExamTotalMarks, txtExamDuration;
    AppCompatButton btnSetAlarm, btnGiveExam, btnExamFinished;
    SharedPreferences sharedPreferences;
    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_info);

        sharedPreferences = getSharedPreferences("examinerPref", MODE_PRIVATE);
        String userID =sharedPreferences.getString("userID","");

        txtExamName = findViewById(R.id.txt_exam_name);
        txtExamDate = findViewById(R.id.txt_exam_date);
        txtExamTime = findViewById(R.id.txt_exam_time);
        txtExamSyllabus = findViewById(R.id.txt_exam_syllabus);
        txtExamTotalMarks = findViewById(R.id.txt_exam_total_marks);
        txtExamDuration = findViewById(R.id.txt_exam_duration);

        btnSetAlarm = findViewById(R.id.btn_set_alarm);
        btnGiveExam = findViewById(R.id.btn_give_exam);
        btnExamFinished = findViewById(R.id.btn_exam_finish);

        ExamDataModel newExamData = (ExamDataModel) getIntent().getSerializableExtra("examData");

        String examName, examDate, examTime, examSyllabus, examTotalMarks, examDuration;

        assert newExamData != null;
        examName = newExamData.getExamName();
        examDate = newExamData.getExamDate();
        examTime = newExamData.getExamTime();
        examSyllabus = newExamData.getExamSyllabus();
        examTotalMarks = newExamData.getTotalMarks();
        examDuration = newExamData.getDuration();

        List<String> usersList = newExamData.getUsersList();

        String[] newExamDate = examDate.split("/");

        long examDurationLong = Long.parseLong(examDuration);

        txtExamName.setText(examName);
        txtExamDate.setText(examDate);
        txtExamTime.setText(examTime);
        txtExamSyllabus.setText(examSyllabus);
        txtExamTotalMarks.setText(examTotalMarks);
        txtExamDuration.setText(examDuration + " Min");

        int[] examStartTimeArray = normalToInt(examTime);

        String examStartTimeStr = timeValidation(examStartTimeArray[0], examStartTimeArray[1]);
        LocalTime examStartTime = LocalTime.parse(examStartTimeStr);
        LocalTime examEndTime = examStartTime.plusMinutes(examDurationLong);

        int[] presentTimeInt = normalToInt(getPresentTime());
        String presentTimeStr = timeValidation(presentTimeInt[0], presentTimeInt[1]);
        LocalTime presentTime = LocalTime.parse(presentTimeStr);

        txtExamDate.setText(examDate);
        txtExamTime.setText(examTime);

        if (examDate.compareTo(getPresentDate()) == 0){
            if (presentTime.isAfter(examStartTime) && presentTime.isBefore(examEndTime)){

                btnSetAlarm.setVisibility(View.GONE);
                btnGiveExam.setVisibility(View.VISIBLE);
                btnExamFinished.setVisibility(View.GONE);
                // Check is user already perform this exam or not
                if (isUserCompletedExam(usersList, userID)) {
                    btnGiveExam.setVisibility(View.GONE);
                    btnExamFinished.setVisibility(View.VISIBLE);
                    btnExamFinished.setText("You've Completed The Test");
                }

            } else if (presentTime.isAfter(examEndTime)) {

                btnSetAlarm.setVisibility(View.GONE);
                btnGiveExam.setVisibility(View.GONE);
                btnExamFinished.setVisibility(View.VISIBLE);

            } else if (presentTime.isBefore(examStartTime)) {

                btnSetAlarm.setVisibility(View.VISIBLE);
                btnGiveExam.setVisibility(View.GONE);
                btnExamFinished.setVisibility(View.GONE);

            } else {
                Toast.makeText(ExamInfoActivity.this, "Something Wrong, Please Try Again!", Toast.LENGTH_SHORT).show();
            }
        } else if (examDate.compareTo(getPresentDate()) > 0) {

            btnSetAlarm.setVisibility(View.VISIBLE);
            btnGiveExam.setVisibility(View.GONE);
            btnExamFinished.setVisibility(View.GONE);

        } else if (examDate.compareTo(getPresentDate()) < 0) {

            btnSetAlarm.setVisibility(View.GONE);
            btnGiveExam.setVisibility(View.GONE);
            btnExamFinished.setVisibility(View.VISIBLE);

        }

        // Going to the Exam Page on clicking Give Exam Button
        btnGiveExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent examPage = new Intent(ExamInfoActivity.this, ExamActivity.class);
                examPage.putExtra("examDataModel", newExamData);

                startActivity(examPage);
            }
        });
        // Opening Calender Event on clicking Set Alarm Button and set a  alarm
        btnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openCalender = new Intent(Intent.ACTION_INSERT);
                openCalender.setType("vnd.android.cursor.item/event");

                openCalender.putExtra(CalendarContract.Events.TITLE, "" + newExamData.getExamName());

                openCalender.putExtra(CalendarContract.Events.DESCRIPTION, "Exam Syllabus : \n" + newExamData.getExamSyllabus());

                GregorianCalendar calDate = new GregorianCalendar(Integer.parseInt(newExamDate[2]),
                        Integer.parseInt(newExamDate[1])-1,
                        Integer.parseInt(newExamDate[0]),
                        examStartTimeArray[0], examStartTimeArray[1], 0);

                openCalender.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calDate.getTimeInMillis());
                openCalender.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calDate.getTimeInMillis());
                startActivity(openCalender);
            }
        });
        // Going back on clicking Exam Finished Button
        btnExamFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    // Get User's Device Date
    private String getPresentDate(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        sDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));
        return sDateFormat.format(new Date());
    }
    // Get User's Device Time
    private String getPresentTime(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sTimeFormat = new SimpleDateFormat("hh:mm a");
        sTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));
        return sTimeFormat.format(new Date());
    }
    // Converting Time to 24 hr format
    private int[] normalToInt(String time){
        String[] newTime = time.split(" ");
        String[] hrMin = newTime[0].split(":");

        int hr, min;

        if (newTime[1].equals("PM") || newTime[1].equals("Pm") || newTime[1].equals("pm")) {
            hr = Integer.parseInt(hrMin[0]);
            if (hr < 12){
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
    // Adding 0 in front of the number smaller than 10
    private String timeValidation(int hr, int min){
        String newHr = hr < 10 ? "0" + hr : String.valueOf(hr);
        String newMin = min < 10 ? "0" + min : String.valueOf(min);
        return newHr + ":" + newMin;
    }
    // Check is user completed the exam already
    private boolean isUserCompletedExam(List<String> usersList, String userID){
        return usersList.contains(userID);
    }

}