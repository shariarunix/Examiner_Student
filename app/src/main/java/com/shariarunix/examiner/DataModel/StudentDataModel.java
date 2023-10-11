package com.shariarunix.examiner.DataModel;



public class StudentDataModel{
    private String name, email, phone, course, password, userId;

    public StudentDataModel(){
        // Default Empty Contractor
    }
    public StudentDataModel(String name, String email, String phone, String course, String password, String userId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.course = course;
        this.password = password;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}
