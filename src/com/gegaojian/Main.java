package com.gegaojian;

import com.gegaojian.alarmsystem.AlarmAgent;
import com.gegaojian.alarmsystem.AlarmInfo;

public class Main {

    public static void main(String[] args) {
	// write your code here
        System.out.println("Begin:");

        for (int i = 0; i < 10000; ++i){
            try {
                AlarmAgent.getInstance().sendAlarm(new AlarmInfo("AlarmINFO : " + i));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        AlarmAgent.getInstance().disConnected();
        System.out.println("End!");
    }
}
