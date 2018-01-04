package com.gegaojian.alarmsystem;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import com.gegaojian.guardedsuspension.*;


public class AlarmAgent {
    private volatile boolean connectedToServer = false;
    private static AlarmAgent alarmAgent = new AlarmAgent();

    private AlarmAgent(){
        init();
    }

    public static AlarmAgent getInstance(){
        return alarmAgent;
    }

    // 模式角色：Predicate
    private final Predicate agentConnected = new Predicate() {
        @Override
        public boolean evaluate() {
            return connectedToServer;
        }
    };

    // 模式角色：Blocker,由具体执行目标动作
    private final Blocker blocker = new ConditionVarBlocker();

    // 计时器，用来支持心跳守护进程
    private final Timer heartbeatTimer = new Timer(true);

    public void sendAlarm(final AlarmInfo alarmInfo) throws Exception{
        // 创建一个受保护的目标动作,保护条件为Predicate：agentConnected
        GuardedAction<Void> guardedAction = new GuardedAction<Void>(agentConnected) {
            @Override
            public Void call() throws Exception {
                doSendAlarm(alarmInfo);
                return null;
            }
        };

        // 将任务交给blocker执行
        blocker.callWithGuard(guardedAction);
    }

    // 目标动作
    private void doSendAlarm(AlarmInfo alarmInfo){
        System.out.println("sending alarm" + alarmInfo.getInfo());

        // 模拟向报警服务器发送报警
        try{
            Thread.sleep(50);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // stateChanged方法
    protected void onConnected() {
        try {
            blocker.signalAfter(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    connectedToServer = true;
                    System.out.println("connected to server");
                    return Boolean.TRUE;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDisconnected() {
        connectedToServer = false;
    }

    public void disConnected() {
        System.out.println("disconnected from alarm server.");
        connectedToServer = false;
    }

    private void init() {
        Thread connectingThread = new Thread(new ConnectingTask());

        connectingThread.start();

        heartbeatTimer.schedule(new HeartbeatTask(), 600, 1000);
    }
}
