package com.example.firebasedemo.modules;

public class Person {
    public String name, phone, date;
    public Double la, lo;

    public Person() {

    }

    public Person(String name, String phone, String date, Double la, Double lo) {
        this.name = name;
        this.phone = phone;
        this.date = date;
        this.la = la;
        this.lo = lo;
    }
}
