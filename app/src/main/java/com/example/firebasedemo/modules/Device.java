package com.example.firebasedemo.modules;

public class Device {
    public String name, mac;
    public double la, lo;

    public Device() {

    }

    public Device(String name, String mac, double la, double lo) {
        this.name = name;
        this.mac = mac;
        this.la = la;
        this.lo = lo;
    }
}
