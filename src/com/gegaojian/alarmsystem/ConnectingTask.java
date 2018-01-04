package com.gegaojian.alarmsystem;

public class ConnectingTask implements Runnable {
    @Override
    public void run() {
        // 模拟连接
        try{
            Thread.sleep(100);
        }catch (InterruptedException e){;}

        AlarmAgent.getInstance().onConnected();
    }
}
