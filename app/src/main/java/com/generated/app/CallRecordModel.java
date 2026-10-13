package com.generated.app;

public class CallRecordModel {
    private final String nameOrNumber;
    private final String date;
    private final String type; // נכנסת, יוצאת, לא נענתה

    public CallRecordModel(String nameOrNumber, String date, String type) {
        this.nameOrNumber = nameOrNumber;
        this.date = date;
        this.type = type;
    }

    public String getNameOrNumber() {
        return nameOrNumber;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }
}
