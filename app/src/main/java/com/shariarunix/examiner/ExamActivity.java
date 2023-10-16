package com.shariarunix.examiner;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shariarunix.examiner.DataModel.ExamResultModel;
import com.shariarunix.examiner.DataModel.QuestionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
public class ExamActivity extends AppCompatActivity {
    List<QuestionModel> questionModelList = new ArrayList<>();
    String examName, examDate, course, userEmail, userID, examTotalMarksString, examDurationString, correctOption;

    int result = 0, questionIndex = 1, examTotalMarks, examDuration;
    long totalTimeMillis, tempCountDownTime;
    TextView txtExamPageName, txtQuestion, txtShowTimer;
    RadioGroup questionOptionGroup;
    RadioButton optionOne, optionTwo, optionThree, optionFour;

    CountDownTimer countDownTimer, tempCountDownTimer;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private final DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        sharedPreferences = getSharedPreferences("examinerPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Getting Data From Shared Preference
        userID =sharedPreferences.getString("userID","");
        userEmail =sharedPreferences.getString("userEmail","");

        // Getting Data from previous activity
        questionModelList = (List<QuestionModel>) getIntent().getSerializableExtra("questionList");

        examName = getIntent().getStringExtra("examName");
        examDate = getIntent().getStringExtra("examDate");
        course = getIntent().getStringExtra("course");
        examTotalMarksString = getIntent().getStringExtra("examTotalMarks");
        examDurationString = getIntent().getStringExtra("examDuration");

        // Convert exam total marks string to int
        assert examTotalMarksString != null;
        examTotalMarks = Integer.parseInt(examTotalMarksString);

        //Convert exam duration String to integer
        examDuration = Integer.parseInt(examDurationString);

        // Shuffle the list of question
        assert questionModelList != null;
        Collections.shuffle(questionModelList);

        // Finding the id of XML View
        txtExamPageName = findViewById(R.id.txt_exam_page_name);
        txtQuestion = findViewById(R.id.txt_question);
        txtShowTimer = findViewById(R.id.txt_show_timer);

        AppCompatButton btnSubmit = findViewById(R.id.btn_question_submit);
        AppCompatButton btnNext = findViewById(R.id.btn_question_next);

        questionOptionGroup = findViewById(R.id.question_option_group);

        optionOne = findViewById(R.id.option_one);
        optionTwo = findViewById(R.id.option_two);
        optionThree = findViewById(R.id.option_three);
        optionFour = findViewById(R.id.option_four);

        // Exam Complete Dialog
        BottomSheetDialog examCompleteDialog = new BottomSheetDialog(ExamActivity.this, R.style.bottom_sheet_dialog);
        Objects.requireNonNull(examCompleteDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        examCompleteDialog.getBehavior().setSkipCollapsed(true);
        examCompleteDialog.getBehavior().setState(STATE_EXPANDED);
        examCompleteDialog.setCancelable(false);
        examCompleteDialog.setContentView(R.layout.exam_complete_dialog);

        TextView txtExamResultShow = examCompleteDialog.findViewById(R.id.txt_show_exam_result);
        AppCompatButton btnExamCompleteDialogContinue = examCompleteDialog.findViewById(R.id.btn_exam_complete_continue);

        assert btnExamCompleteDialogContinue != null;
        btnExamCompleteDialogContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                examCompleteDialog.dismiss();
                onBackPressed();
            }
        });

        // Starting CountDown For Exam Length
        totalTimeMillis = (long) examDuration * 60 * 1000;

        countDownTimer = new CountDownTimer(totalTimeMillis, 1000) {
            @Override
            public void onTick(long l) {

                totalTimeMillis = l;

                long minutes = totalTimeMillis / 60000;
                long seconds = (totalTimeMillis % 60000) / 1000;

                @SuppressLint("DefaultLocale") String minutesStr = String.format("%02d", minutes);
                @SuppressLint("DefaultLocale") String secondsStr = String.format("%02d", seconds);

                String newTimeStr;

                newTimeStr = minutesStr;
                newTimeStr += ":";
                newTimeStr += secondsStr;

                txtShowTimer.setText(newTimeStr);
            }
            @Override
            public void onFinish() {
                onBackPressed();
            }
        }.start();

        // Checking User is Active or Not by this Countdown
        tempCountDownTime = 2 * 30 * 1000;

        tempCountDownTimer = new CountDownTimer(tempCountDownTime, 1000) {
            @Override
            public void onTick(long l) {
                tempCountDownTime = l;
            }

            @Override
            public void onFinish() {
                onBackPressed();
            }
        }.start();

        // Setting the question on the template
        setQuestion(0);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (examTotalMarks > questionIndex){

                    int checkedID = questionOptionGroup.getCheckedRadioButtonId();

                    RadioButton checkedBtn = findViewById(checkedID);

                    correctOption = questionModelList.get(questionIndex-1).getCorrectOption();

                    boolean optionOneIsChecked = optionOne.isChecked();
                    boolean optionTwoIsChecked = optionTwo.isChecked();
                    boolean optionThreeIsChecked = optionThree.isChecked();
                    boolean optionFourIsChecked = optionFour.isChecked();

                    if (optionOneIsChecked || optionTwoIsChecked || optionThreeIsChecked || optionFourIsChecked) {
                        if (checkedBtn.getText().toString().equals(correctOption)){
                            result +=1;
//                            Toast.makeText(ExamActivity.this, ""+result,Toast.LENGTH_SHORT).show();
                        }
                    }

                    setQuestion(questionIndex);

                    questionOptionGroup.clearCheck();

                    questionIndex++;

                    tempCountDownTimer.cancel();
                    tempCountDownTimer.start();

                } else if (examTotalMarks == questionIndex) {
                    int checkedID = questionOptionGroup.getCheckedRadioButtonId();
                    RadioButton checkedBtn = findViewById(checkedID);

                    String newCorrectOption = questionModelList.get(examTotalMarks-1).getCorrectOption();

                    boolean optionOneIsChecked = optionOne.isChecked();
                    boolean optionTwoIsChecked = optionTwo.isChecked();
                    boolean optionThreeIsChecked = optionThree.isChecked();
                    boolean optionFourIsChecked = optionFour.isChecked();

                    if (optionOneIsChecked || optionTwoIsChecked || optionThreeIsChecked || optionFourIsChecked) {
                        if (checkedBtn.getText().toString().equals(newCorrectOption)){
                            result +=1;
//                            Toast.makeText(ExamActivity.this, ""+result,Toast.LENGTH_SHORT).show();
                        }
                    }
                    Toast.makeText(ExamActivity.this, ""+result,Toast.LENGTH_SHORT).show();
                    assert txtExamResultShow != null;
                    txtExamResultShow.setText(result + " / " + examTotalMarks);
                    examCompleteDialog.show();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Setting Exam Name
        txtExamPageName.setText(examName);
    }

    public void setQuestion(int index){
        String tempQuestion = index+1+". "+questionModelList.get(index).getQuestion();
        txtQuestion.setText(tempQuestion);
        optionOne.setText(questionModelList.get(index).getOptionOne());
        optionTwo.setText(questionModelList.get(index).getOptionTwo());
        optionThree.setText(questionModelList.get(index).getOptionThree());
        optionFour.setText(questionModelList.get(index).getOptionFour());
    }
    public void setDataToDatabase(){
        ExamResultModel examResultModel = new ExamResultModel(examName,
                examDate,
                examTotalMarksString,
                ""+result,
                course,
                userEmail,
                userID);

        String resultKey = mReference.push().getKey();
        assert resultKey != null;
        mReference.child("result").child(resultKey).setValue(examResultModel);

        mReference.child("student").child(userID).child("prevExamResult").setValue(result);
        mReference.child("student").child(userID).child("prevExamTotalMarks").setValue(examTotalMarks);
    }
    public void submitResult(){
        boolean optionOneIsChecked = optionOne.isChecked();
        boolean optionTwoIsChecked = optionTwo.isChecked();
        boolean optionThreeIsChecked = optionThree.isChecked();
        boolean optionFourIsChecked = optionFour.isChecked();

        if (optionOneIsChecked || optionTwoIsChecked || optionThreeIsChecked || optionFourIsChecked){
            int checkedID = questionOptionGroup.getCheckedRadioButtonId();

            RadioButton checkedBtn = findViewById(checkedID);

            if (checkedBtn.getText().toString().equals(correctOption)){
                result +=1;
            }
        }

        countDownTimer.cancel();
        tempCountDownTimer.cancel();
    }
    @Override
    public void onBackPressed() {
        submitResult();
        startActivity(new Intent(ExamActivity.this, MainActivity.class));
        finish();
        super.onBackPressed();
    }
    @Override
    protected void onPause() {
        Toast.makeText(ExamActivity.this, "Your Result : "+result, Toast.LENGTH_SHORT).show();

        // Setting result data to firebase database
        setDataToDatabase();
        onBackPressed();
        finish();
        super.onPause();
    }
}

