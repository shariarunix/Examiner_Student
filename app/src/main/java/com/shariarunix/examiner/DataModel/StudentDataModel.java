package com.shariarunix.examiner.DataModel;

import java.io.Serializable;

public class StudentDataModel implements Serializable {
    private String userName, name, email, phone, guardianPhone, course, password, userId, profileUri;
    private int prevExamTotalMarks, prevExamResult;
    private boolean isLoggedIn;

    public StudentDataModel(){
        // Default Empty Contractor
    }

    public StudentDataModel(String userId,
                            String userName,
                            String name,
                            String email,
                            String phone,
                            String guardianPhone,
                            String course,
                            String password,
                            String profileUri,
                            int prevExamTotalMarks,
                            int prevExamResult,
                            boolean isLoggedIn) {

        this.userName = userName;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.guardianPhone = guardianPhone;
        this.course = course;
        this.password = password;
        this.userId = userId;
        this.profileUri = profileUri;
        this.prevExamTotalMarks = prevExamTotalMarks;
        this.prevExamResult = prevExamResult;
        this.isLoggedIn = isLoggedIn;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
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

    public String getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(String profileUri) {
        this.profileUri = profileUri;
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