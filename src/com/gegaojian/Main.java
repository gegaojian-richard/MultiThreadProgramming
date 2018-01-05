package com.gegaojian;

import com.gegaojian.alarmsystem.AlarmAgent;
import com.gegaojian.alarmsystem.AlarmInfo;
import com.gegaojian.alarmsystem.AlarmManager;
import com.gegaojian.alarmsystem.AlarmType;

public class Main {

    public static void main(String[] args) {
	// write your code here
        System.out.println("Begin:");

        for (int i = 0; i < 100; ++i){
            try {
                AlarmManager.getInstance().sendAlarm(AlarmType.FAULT, String.valueOf(i) , "fault");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        AlarmManager.getInstance().shutdown();
        System.out.println("End!");
    }
}
