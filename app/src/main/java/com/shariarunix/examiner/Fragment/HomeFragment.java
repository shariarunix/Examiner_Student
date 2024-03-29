package com.shariarunix.examiner.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shariarunix.examiner.Adapter.CustomAdapter;
import com.shariarunix.examiner.DataModel.ExamDataModel;
import com.shariarunix.examiner.DataModel.StudentDataModel;
import com.shariarunix.examiner.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private DatabaseReference mReference;
    private static final String U_DATA = "arg1";
    String userName, userEmail, userPhone, userGrdPhone, userCourse;
    int prevExamResult, prevExamTotalMarks;
    boolean isDialogShown;
    List<ExamDataModel> examDataList = new ArrayList<>();
    ProgressBar homeProgressBar;
    TextView txtEmptyExam;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ListView examList;

    public HomeFragment() {
        //Default Empty Constructor
    }

    public static HomeFragment getInstance(StudentDataModel studentDataModel) {
        HomeFragment homeFragment = new HomeFragment();
        Bundle bundle = new Bundle();

        bundle.putSerializable(U_DATA, studentDataModel);

        homeFragment.setArguments(bundle);
        return homeFragment;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = requireActivity().getSharedPreferences("examinerPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isDialogShown = sharedPreferences.getBoolean("introDialog", false);

        txtEmptyExam = view.findViewById(R.id.txt_empty_exam);
        examList = view.findViewById(R.id.exam_list);
        homeProgressBar = view.findViewById(R.id.home_progress_bar);

        TextView txtShowName = view.findViewById(R.id.txt_show_name_home);
        TextView txtShowPrevResult = view.findViewById(R.id.txt_show_prev_result);
        TextView txtShowPrevResultComment = view.findViewById(R.id.txt_show_prev_result_comment);

        LinearLayout layoutShowPrevResult = view.findViewById(R.id.layout_show_prev_result);

        if (getArguments() != null) {
            StudentDataModel studentDataModel = (StudentDataModel) getArguments().getSerializable(U_DATA);

            assert studentDataModel != null;
            userName = studentDataModel.getName();
            userEmail = studentDataModel.getEmail();
            userPhone = studentDataModel.getPhone();
            userGrdPhone = studentDataModel.getGuardianPhone();
            userCourse = studentDataModel.getCourse();
            prevExamResult = studentDataModel.getPrevExamResult();
            prevExamTotalMarks = studentDataModel.getPrevExamTotalMarks();

            txtShowName.setText(userName);
        }


        Dialog introDialog = new Dialog(requireActivity());
        showIntroDialog(introDialog);

        // Showing user previous exam result
        if (prevExamResult == 0 && prevExamTotalMarks == 0) {
            layoutShowPrevResult.setVisibility(View.GONE);
        } else if (prevExamResult >= prevExamTotalMarks * 0.9) {
            txtShowPrevResultComment.setText("Very Good");
        } else if (prevExamResult < prevExamTotalMarks * 0.9 && prevExamResult > prevExamTotalMarks * 0.5) {
            txtShowPrevResultComment.setText("Good");
        } else if (prevExamResult <= prevExamTotalMarks * 0.5) {
            txtShowPrevResultComment.setText("Very Bad");
            layoutShowPrevResult.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.red_pr_bg));
        }

        // Setting previous exam data to view
        txtShowPrevResult.setText(prevExamResult + "/" + prevExamTotalMarks);

        // Load Exam data in fragment
        loadExamData(examList, userCourse);

    }

    // Load exam data
    public void loadExamData(ListView listView, String course) {
        mReference
                .child("examSet")
                .orderByChild("course")
                .equalTo(course)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (isAdded()) {
                            examDataList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ExamDataModel examDataModel = dataSnapshot.getValue(ExamDataModel.class);

                                examDataList.add(examDataModel);
                            }

                            homeProgressBar.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);

                            if (examDataList.size() < 1) {
                                txtEmptyExam.setVisibility(View.VISIBLE);
                            }

                            CustomAdapter examListAdapter = new CustomAdapter(requireActivity(), R.layout.list_item_exam, examDataList.size());
                            examListAdapter.setExamDataModelList(examDataList);

                            listView.setAdapter(examListAdapter);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireActivity(), "Please check your internet", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void showIntroDialog(Dialog dialog) {
        dialog.setContentView(R.layout.dialog_user_info);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCancelable(false);

        TextView txtName = dialog.findViewById(R.id.txt_stNameID);
        TextView txtEmail = dialog.findViewById(R.id.txt_stEmailID);
        TextView txtPhone = dialog.findViewById(R.id.txt_stPhoneID);
        TextView txtGrdPhone = dialog.findViewById(R.id.txt_stGrdPhoneID);
        TextView txtCourse = dialog.findViewById(R.id.txt_stCourseID);

        AppCompatButton btnDialogHide = dialog.findViewById(R.id.btn_dialog_ok);

        txtName.setText(userName);
        txtEmail.setText(userEmail);
        txtPhone.setText(userPhone);
        txtGrdPhone.setText(userGrdPhone);
        txtCourse.setText(userCourse);

        btnDialogHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("introDialog", false);
                editor.apply();
                dialog.hide();
            }
        });

        if (isDialogShown) {
            dialog.show();
        }
    }
}