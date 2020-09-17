package com.example.firebasedemo.modules;

public class Notification {
    public String message;
    public Boolean isRead;
    public Long date;

    public Notification() { // for firebase

    }

    public Notification(String message, Boolean isRead, Long date) {
        this.message = message;
        this.isRead = isRead;
        this.date = date;
    }
}
