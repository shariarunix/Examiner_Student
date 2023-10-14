package com.shariarunix.examiner.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shariarunix.examiner.Adapter.CustomAdapter;
import com.shariarunix.examiner.DataModel.ExamDataModel;
import com.shariarunix.examiner.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private DatabaseReference mReference;
    private static final String U_NAME = "arg1";
    private static final String U_PREV_EXAM = "arg2";
    private static final String U_PREV_EXAM_MARKS = "arg3";
    private static final String U_COURSE = "arg4";
    String userName, userCourse, prevExamResultStr, prevExamTotalMarksStr;
    int prevExamResult, prevExamTotalMarks;
    List<ExamDataModel> examDataList = new ArrayList<>();

    public HomeFragment(){
        //Default Empty Constructor
    }

    public static HomeFragment getInstance(String uName, String uCourse, String prevExamResult, String prevExamTotalMarks){
        HomeFragment homeFragment = new HomeFragment();
        Bundle bundle = new Bundle();

        bundle.putString(U_NAME, uName);
        bundle.putString(U_COURSE, uCourse);
        bundle.putString(U_PREV_EXAM, prevExamResult);
        bundle.putString(U_PREV_EXAM_MARKS, prevExamTotalMarks);

        homeFragment.setArguments(bundle);
        return homeFragment;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mReference = FirebaseDatabase.getInstance().getReference();

        ListView examList = view.findViewById(R.id.exam_list);

        TextView txtShowName = view.findViewById(R.id.txt_show_name_home);
        TextView txtShowPrevResult = view.findViewById(R.id.txt_show_prev_result);
        TextView txtShowPrevResultComment = view.findViewById(R.id.txt_show_prev_result_comment);

        LinearLayout layoutShowPrevResult = view.findViewById(R.id.layout_show_prev_result);

        if (getArguments() != null){
            userName = getArguments().getString(U_NAME);
            userCourse = getArguments().getString(U_COURSE);
            prevExamResultStr = getArguments().getString(U_PREV_EXAM);
            prevExamTotalMarksStr = getArguments().getString(U_PREV_EXAM_MARKS);

            prevExamResult = Integer.parseInt(prevExamResultStr);
            prevExamTotalMarks = Integer.parseInt(prevExamTotalMarksStr);

            txtShowName.setText(userName);
        }

        // Showing user previous exam result
        if (prevExamResult == 0 && prevExamTotalMarks == 0) {
            layoutShowPrevResult.setVisibility(View.GONE);
        } else if (prevExamResult >= prevExamTotalMarks * 0.9) {
            txtShowPrevResultComment.setText("Very Good");
        } else if (prevExamResult < prevExamTotalMarks * 0.9 && prevExamResult > prevExamTotalMarks * 0.5) {
            txtShowPrevResultComment.setText("Good");
        } else if (prevExamResult <= prevExamTotalMarks * 0.5) {
            txtShowPrevResultComment.setText("Very Bad");
            layoutShowPrevResult.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.red_pr_bg));
        }

        // Setting previous exam data to view
        txtShowPrevResult.setText(prevExamResult+"/"+prevExamTotalMarks);

        // Load Exam data in fragment
        loadExamData(examList, userCourse);

        return view;
    }

    // Load exam data
    public void loadExamData(ListView listView, String course){
        mReference
            .child("examSet")
                .orderByChild("course")
                .equalTo(course)
                .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    examDataList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        ExamDataModel examDataModel = dataSnapshot.getValue(ExamDataModel.class);

                        examDataList.add(examDataModel);
                    }

//                    Collections.reverse(examDataList);

                    listView.setAdapter(new CustomAdapter(getActivity(), R.layout.exam_list_item, examDataList, examDataList.size()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Please check your internet", Toast.LENGTH_SHORT).show();
                }
            });
    }
}