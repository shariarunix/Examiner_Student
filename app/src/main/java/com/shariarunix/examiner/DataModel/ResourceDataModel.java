package com.shariarunix.examiner.DataModel;

public class ResourceDataModel {

    String resource, date, time, admin;

    public ResourceDataModel() {
        // Default Empty Constructor
    }

    public ResourceDataModel(String resource, String date, String time, String admin) {
        this.resource = resource;
        this.date = date;
        this.time = time;
        this.admin = admin;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}
