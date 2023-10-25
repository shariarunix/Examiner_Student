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
import com.shariarunix.examiner.DataModel.ExamDataModel;
import com.shariarunix.examiner.DataModel.ExamResultModel;
import com.shariarunix.examiner.DataModel.QuestionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ExamActivity extends AppCompatActivity {

    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
    SharedPreferences sharedPreferences;
    ExamDataModel examDataModel;
    CountDownTimer countDownTimer, tempCountDownTimer;
    long totalTimeMillis, tempCountDownTime;
    List<String> usersList = new ArrayList<>();
    List<QuestionModel> tempQuestionList = new ArrayList<>();
    List<QuestionModel> questionListOne = new ArrayList<>();
    List<QuestionModel> questionListTwo = new ArrayList<>();
    List<QuestionModel> questionListThree = new ArrayList<>();
    List<QuestionModel> questionModelList = new ArrayList<>();

    String userID, userName, userEmail, userCourse;
    String examID, examName, examDate, examTotalMarksString, examDurationString, correctOption;
    int examTotalMarks, examDuration, questionIndex = 1, result = 0;

    TextView txtExamPageName, txtQuestion, txtShowTimer, txtShowMark, txtExamResultShow;
    AppCompatButton btnSubmit, btnNext;
    RadioGroup questionOptionGroup;
    RadioButton optionOne, optionTwo, optionThree, optionFour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_exam);

        // Initializing Shared Preferences
        sharedPreferences = getSharedPreferences("examinerPref", MODE_PRIVATE);

        //Getting User Data From Shared Preferences
        userID = sharedPreferences.getString("userID", "");
        userEmail = sharedPreferences.getString("userEmail", "");
        userName = sharedPreferences.getString("userName", "");

        // Getting Data from previous activity
        examDataModel = (ExamDataModel) getIntent().getSerializableExtra("examDataModel");

        assert examDataModel != null;
        examID = examDataModel.getExamId();
        examName = examDataModel.getExamName();
        examDate = examDataModel.getExamDate();
        userCourse = examDataModel.getCourse();
        examTotalMarksString = examDataModel.getTotalMarks();
        examDurationString = examDataModel.getDuration();

        tempQuestionList = examDataModel.getQuestionModelList();
        usersList = examDataModel.getUsersList();

        examTotalMarks = Integer.parseInt(examTotalMarksString);
        examDuration = Integer.parseInt(examDurationString);

        // Finding the id of XML View
        txtExamPageName = findViewById(R.id.txt_exam_page_name);
        txtShowTimer = findViewById(R.id.txt_show_timer);

        txtQuestion = findViewById(R.id.txt_question);
        txtShowMark = findViewById(R.id.txt_show_mark);

        btnSubmit = findViewById(R.id.btn_question_submit);
        btnNext = findViewById(R.id.btn_question_next);

        questionOptionGroup = findViewById(R.id.question_option_group);

        optionOne = findViewById(R.id.option_one);
        optionTwo = findViewById(R.id.option_two);
        optionThree = findViewById(R.id.option_three);
        optionFour = findViewById(R.id.option_four);

        // Setting Exam Title
        txtExamPageName.setText(examName);

        // Starting CountDown According To Exam Duration
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
                checkingAns(questionModelList, questionIndex - 1);
                methodForBackPressed();
            }
        }.start();

        // Checking User is Active or Not by this Countdown
        tempCountDownTime = 2 * 60 * 1000;
        tempCountDownTimer = new CountDownTimer(tempCountDownTime, 1000) {
            @Override
            public void onTick(long l) {
                tempCountDownTime = l;
            }

            @Override
            public void onFinish() {
                checkingAns(questionModelList, questionIndex - 1);
                methodForBackPressed();
            }
        }.start();

        // Exam Complete Dialog
        BottomSheetDialog examCompleteDialog = new BottomSheetDialog(ExamActivity.this, R.style.bottom_sheet_dialog);
        Objects.requireNonNull(examCompleteDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        examCompleteDialog.getBehavior().setSkipCollapsed(true);
        examCompleteDialog.getBehavior().setState(STATE_EXPANDED);
        examCompleteDialog.setCancelable(false);

        examCompleteDialog.setContentView(R.layout.bottom_dialog_exam_complete);

        txtExamResultShow = examCompleteDialog.findViewById(R.id.txt_show_exam_result);
        AppCompatButton btnExamCompleteDialogContinue = examCompleteDialog.findViewById(R.id.btn_exam_complete_continue);

        assert btnExamCompleteDialogContinue != null;
        btnExamCompleteDialogContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                examCompleteDialog.dismiss();
                methodForBackPressed();
            }
        });

        questionModelList = questionPattern();

        setQuestion(questionModelList, 0);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (questionModelList.size() > questionIndex) {
                    // Checking Correct Answer and Adding Marks
                    checkingAns(questionModelList, questionIndex - 1);

                    setQuestion(questionModelList, questionIndex);
                    questionOptionGroup.clearCheck();

                    questionIndex++;

                    tempCountDownTimer.cancel();
                    tempCountDownTimer.start();

                } else if (questionModelList.size() == questionIndex) {
                    // Checking Correct Answer and Adding Marks
                    checkingAns(questionModelList, questionIndex - 1);

                    assert txtExamResultShow != null;
                    txtExamResultShow.setText(result + " / " + examTotalMarks);
                    examCompleteDialog.show();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkingAns(questionModelList, questionIndex - 1);
                questionOptionGroup.clearCheck();
                methodForBackPressed();
            }
        });
    }

    private void checkingAns(List<QuestionModel> qmList, int ansIndex) {

        int checkedID = questionOptionGroup.getCheckedRadioButtonId();
        RadioButton checkedBtn = findViewById(checkedID);

        correctOption = qmList.get(ansIndex).getCorrectOption();

        if (optionOne.isChecked() || optionTwo.isChecked() || optionThree.isChecked() || optionFour.isChecked()) {
            // Is ans correct or not, If correct adding marks to result variable
            if (checkedBtn.getText().toString().equals(correctOption)) {
                result += qmList.get(ansIndex).getQuestionMark();
            }
        }
    }

    private List<QuestionModel> questionPattern() {

        List<QuestionModel> qmList = new ArrayList<>();

        for (QuestionModel qm : tempQuestionList) {
            if (qm.getQuestionMark() == 1) {
                questionListOne.add(qm);
            } else if (qm.getQuestionMark() == 2) {
                questionListTwo.add(qm);
            } else if (qm.getQuestionMark() == 3) {
                questionListThree.add(qm);
            }
        }

        // Adding Question from questionListThree to questionModelList
        if (questionListThree.size() > 3) {
            Collections.shuffle(questionListThree);

            for (int i = 1; i <= 3; i++) {
                qmList.add(questionListThree.get(i));
            }
        } else {
            qmList.addAll(questionListThree);
        }

        int listSizeThree = qmList.size();
        int totalMarkThree = listSizeThree * 3;

        // Adding Question from questionListTwo to questionModelList
        if (questionListTwo.size() > 5) {
            Collections.shuffle(questionListTwo);

            for (int i = 1; i <= 5; i++) {
                qmList.add(questionListTwo.get(i));
            }
        } else {
            qmList.addAll(questionListTwo);
        }

        int listSizeTwo = qmList.size() - listSizeThree;
        int totalMarkTwo = listSizeTwo * 2;

        int itemForMarkOne = examTotalMarks - (totalMarkThree + totalMarkTwo);
        for (int i = 1; i <= itemForMarkOne; i++) {
            Collections.shuffle(questionListOne);
            qmList.add(questionListOne.get(i));
        }

        Collections.shuffle(qmList);

        return qmList;
    }

    private void setQuestion(List<QuestionModel> questionModelList, int index) {

        String tempQuestion = index + 1 + ". " + questionModelList.get(index).getQuestion();
        txtQuestion.setText(tempQuestion);

        txtShowMark.setText(String.valueOf(questionModelList.get(index).getQuestionMark()));

        optionOne.setText(questionModelList.get(index).getOptionOne());
        optionTwo.setText(questionModelList.get(index).getOptionTwo());
        optionThree.setText(questionModelList.get(index).getOptionThree());
        optionFour.setText(questionModelList.get(index).getOptionFour());
    }

    public void setDataToDatabase() {
        ExamResultModel examResultModel = new ExamResultModel(examName, examDate, examTotalMarksString,
                "" + result, userCourse, userEmail, userName, userID);

        String resultKey = mReference.push().getKey();

        assert resultKey != null;
        mReference.child("result").child(resultKey).setValue(examResultModel);

        mReference.child("student").child(userID).child("prevExamResult").setValue(result);
        mReference.child("student").child(userID).child("prevExamTotalMarks").setValue(examTotalMarks);

        usersList.add(userID);
        mReference.child("examSet").child(examID).child("usersList").setValue(usersList);
    }

    private void methodForBackPressed() {

        countDownTimer.cancel();
        tempCountDownTimer.cancel();

        startActivity(new Intent(ExamActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        checkingAns(questionModelList, questionIndex - 1);
        questionOptionGroup.clearCheck();
        methodForBackPressed();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        Toast.makeText(ExamActivity.this, "Your Result : " + result, Toast.LENGTH_SHORT).show();
        questionOptionGroup.clearCheck();
        // Setting result data to firebase database
        setDataToDatabase();
        methodForBackPressed();

        finish();
        super.onPause();
    }
}

