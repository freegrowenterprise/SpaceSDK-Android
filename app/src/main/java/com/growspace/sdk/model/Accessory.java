package com.growspace.sdk.model;


public class Accessory {
    private String alias;
    private String mac;
    private String name;

    public Accessory() {
    }

    public Accessory(String str, String str2, String str3) {
        this.name = str;
        this.mac = str2;
        this.alias = str3;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String str) {
        this.mac = str;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String str) {
        this.alias = str;
    }
}
