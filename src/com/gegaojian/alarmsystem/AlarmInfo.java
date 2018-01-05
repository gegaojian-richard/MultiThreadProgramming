package com.gegaojian.alarmsystem;

public class AlarmInfo {
    private final String id;
    private final AlarmType type;
    private final String extraInfo;

    public AlarmInfo(String id, AlarmType type, String extraInfo) {
        this.id = id;
        this.type = type;
        this.extraInfo = extraInfo;
    }

    public String getInfo() {
        return extraInfo;
    }

    public String getId() {
        return id;
    }

    public AlarmType getType() {
        return type;
    }
}
