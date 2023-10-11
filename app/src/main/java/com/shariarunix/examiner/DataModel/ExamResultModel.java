package com.shariarunix.examiner.DataModel;

public class ExamResultModel {

    private String examName, examDate, examTotalMarks, examResult, userCourse, userEmail, userId;

    public ExamResultModel() {
        // Default Empty Constructor
    }

    public ExamResultModel(String examName, String examDate, String examTotalMarks, String examResult, String userCourse, String userEmail, String userId) {
        this.examName = examName;
        this.examDate = examDate;
        this.examTotalMarks = examTotalMarks;
        this.examResult = examResult;
        this.userCourse = userCourse;
        this.userEmail = userEmail;
        this.userId = userId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getExamTotalMarks() {
        return examTotalMarks;
    }

    public void setExamTotalMarks(String examTotalMarks) {
        this.examTotalMarks = examTotalMarks;
    }

    public String getExamResult() {
        return examResult;
    }

    public void setExamResult(String examResult) {
        this.examResult = examResult;
    }

    public String getUserCourse() {
        return userCourse;
    }

    public void setUserCourse(String userCourse) {
        this.userCourse = userCourse;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
