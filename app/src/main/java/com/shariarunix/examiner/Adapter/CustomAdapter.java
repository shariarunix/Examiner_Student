package com.shariarunix.examiner.Adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.shariarunix.examiner.DataModel.ExamDataModel;
import com.shariarunix.examiner.DataModel.ExamResultModel;
import com.shariarunix.examiner.DataModel.ResourceDataModel;
import com.shariarunix.examiner.ExamInfoActivity;
import com.shariarunix.examiner.R;

import java.util.List;

public class CustomAdapter extends BaseAdapter {

    Context context;
    int layout, length;
    List<ExamDataModel> examDataModelList;
    List<ExamResultModel> examResultModelList;
    List<ResourceDataModel> resourceDataModelList;

    public List<ResourceDataModel> getResourceDataModelList() {
        return resourceDataModelList;
    }

    public void setResourceDataModelList(List<ResourceDataModel> resourceDataModelList) {
        this.resourceDataModelList = resourceDataModelList;
    }

    public List<ExamDataModel> getExamDataModelList() {
        return examDataModelList;
    }

    public void setExamDataModelList(List<ExamDataModel> examDataModelList) {
        this.examDataModelList = examDataModelList;
    }

    public List<ExamResultModel> getExamResultModelList() {
        return examResultModelList;
    }

    public void setExamResultModelList(List<ExamResultModel> examResultModelList) {
        this.examResultModelList = examResultModelList;
    }


    public CustomAdapter(Context context, int layout, int length) {
        this.context = context;
        this.layout = layout;
        this.length = length;
    }

    @Override
    public int getCount() {
        return length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            view = LayoutInflater.from(context).inflate(layout, viewGroup, false);
        }

        if (layout == R.layout.exam_list_item){
            ExamDataModel newModel = examDataModelList.get(i);
            TextView txtListExamName = view.findViewById(R.id.txt_list_exam_name);
            TextView txtListExamDate = view.findViewById(R.id.txt_list_exam_date);
            TextView txtListExamTime = view.findViewById(R.id.txt_list_exam_time);

            AppCompatButton btnExamReadMore = view.findViewById(R.id.btn_exam_read_more);

            txtListExamName.setText(newModel.getExamName());
            txtListExamDate.setText(newModel.getExamDate());
            txtListExamTime.setText(newModel.getExamTime());

            btnExamReadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent nextActivity = new Intent(context, ExamInfoActivity.class);
                    nextActivity.putExtra("examData", newModel);
                    context.startActivity(nextActivity);
                }
            });
        }

        if (layout == R.layout.result_list_item){
            ExamResultModel newErm = examResultModelList.get(i);

            TextView txtShowExamName = view.findViewById(R.id.txt_show_exam_name);
            TextView txtShowResult = view.findViewById(R.id.txt_show_result);
            TextView txtShowDate = view.findViewById(R.id.txt_show_date);

            txtShowExamName.setText(newErm.getExamName());
            txtShowResult.setText(newErm.getExamResult() +" out of "+ newErm.getExamTotalMarks());
            txtShowDate.setText(newErm.getExamDate());
        }

        if (layout == R.layout.resource_list_item) {
            ResourceDataModel resourceDataModel = resourceDataModelList.get(i);

            TextView txtShowMsgDate = view.findViewById(R.id.txt_show_msg_date);
            TextView txtShowMsgTime = view.findViewById(R.id.txt_show_msg_time);
            TextView txtMsgBox = view.findViewById(R.id.txt_msg_box);
            TextView txtShowAdminName = view.findViewById(R.id.txt_show_admin_name);

            txtShowMsgDate.setText(resourceDataModel.getDate());
            txtShowMsgTime.setText(resourceDataModel.getTime());
            txtMsgBox.setText(resourceDataModel.getResource());
            txtShowAdminName.setText("by " + resourceDataModel.getUser());
        }

        return view;
    }

}

