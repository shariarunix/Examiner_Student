package com.shariarunix.examiner.DataModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExamDataModel implements Serializable {
    private String examName, examSyllabus, examTime, examDate, totalMarks, duration, course, examId;

    private List<String> usersList = new ArrayList<>();
    private List<QuestionModel> questionModelList = new ArrayList<>();

    public ExamDataModel(){
        //Default Constructor
    }

    public ExamDataModel(String examName,
                         String examSyllabus,
                         String examTime,
                         String examDate,
                         String totalMarks,
                         String duration,
                         String course,
                         String examId,
                         List<String> usersList,
                         List<QuestionModel> questionModelList) {

        this.examName = examName;
        this.examSyllabus = examSyllabus;
        this.examTime = examTime;
        this.examDate = examDate;
        this.totalMarks = totalMarks;
        this.duration = duration;
        this.course = course;
        this.examId = examId;
        this.usersList = usersList;
        this.questionModelList = questionModelList;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamSyllabus() {
        return examSyllabus;
    }

    public void setExamSyllabus(String examSyllabus) {
        this.examSyllabus = examSyllabus;
    }

    public String getExamTime() {
        return examTime;
    }

    public void setExamTime(String examTime) {
        this.examTime = examTime;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public List<String> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<String> usersList) {
        this.usersList = usersList;
    }

    public List<QuestionModel> getQuestionModelList() {
        return questionModelList;
    }

    public void setQuestionModelList(List<QuestionModel> questionModelList) {
        this.questionModelList = questionModelList;
    }
}


