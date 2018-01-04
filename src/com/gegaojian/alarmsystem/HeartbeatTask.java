package com.gegaojian.alarmsystem;

import java.util.TimerTask;

public class HeartbeatTask extends TimerTask {
    @Override
    public void run() {
        if (!testConnection()){
            AlarmAgent.getInstance().onDisconnected();
            reconnect();
        }
    }

    private boolean testConnection() {
        // 想服务器发送请求，看是否成功
        return true;
    }

    private void reconnect(){
        ConnectingTask connectingThread = new ConnectingTask();

        // 直接在心跳定时器线程中执行
        connectingThread.run();
    }
}
