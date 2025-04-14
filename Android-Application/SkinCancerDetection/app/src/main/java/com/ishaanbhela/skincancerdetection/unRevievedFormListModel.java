package com.ishaanbhela.skincancerdetection;

public class unRevievedFormListModel {

    String UID;
    String Name;
    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public unRevievedFormListModel(String name, String UID) {
        this.UID = UID;
        Name = name;
    }

}
