package com.shariarunix.examiner.DataModel;

import java.io.Serializable;

public class StudentDataModel implements Serializable {
    private String name, email, phone, course, password, userId;
    private int prevExamTotalMarks, prevExamResult;

    private boolean isLoggedIn;

    public StudentDataModel(){
        // Default Empty Contractor
    }

    public StudentDataModel(String name,
                            String email,
                            String phone,
                            String course,
                            String password,
                            String userId,
                            int prevExamTotalMarks,
                            int prevExamResult,
                            boolean isLoggedIn) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.course = course;
        this.password = password;
        this.userId = userId;
        this.prevExamTotalMarks = prevExamTotalMarks;
        this.prevExamResult = prevExamResult;
        this.isLoggedIn = isLoggedIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPrevExamTotalMarks() {
        return prevExamTotalMarks;
    }

    public void setPrevExamTotalMarks(int prevExamTotalMarks) {
        this.prevExamTotalMarks = prevExamTotalMarks;
    }

    public int getPrevExamResult() {
        return prevExamResult;
    }

    public void setPrevExamResult(int prevExamResult) {
        this.prevExamResult = prevExamResult;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}