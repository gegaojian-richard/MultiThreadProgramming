package com.gegaojian.alarmsystem;

/**
 * AlarmManager是告警系统唯一入口(单例)，负责管理一个告警信息发送线程AlarmSendingThread，告警信息发送线程通过AlarmAgent来发送信息
 * 由告警信息发送线程来管理告警信息队列
 * 告警信息发送线程是一个可终止线程，线程终止前会将告警信息队列中的任务处理完后才终止
 */
public class AlarmManager {
    private static final AlarmManager INSTANCE = new AlarmManager();

    // 关闭标识
    private volatile boolean shutdownRequested = false;

    private final AlarmSendingThread alarmSendingThread;

    private AlarmManager(){
        // todo:创建告警信息发送线程
        alarmSendingThread = new AlarmSendingThread();
    }

    public static AlarmManager getInstance() {
        return INSTANCE;
    }

    /**
     * 发送告警
     * @param type
     * @param id
     * @param extraInfo
     * @return ID + extraInfo告警信息被提交的次数，-1表示告警系统已关闭，拒绝接收新的告警信息
     */
    public int sendAlarm(AlarmType type, String id, String extraInfo){
        System.out.println("Trigger alarm " + type + "," + id + "," + extraInfo);
        int duplicateSubmissionCount = 0;
        try{
            AlarmInfo alarmInfo = new AlarmInfo(id, type, extraInfo);
            // 向告警发送线程提交告警信息
            duplicateSubmissionCount = alarmSendingThread.sendAlarm(alarmInfo);
        }catch (Throwable t){
            t.printStackTrace();
        }
        return duplicateSubmissionCount;
    }

    public void init() {
        // 启动告警信息发送线程
        alarmSendingThread.start();
    }

    public synchronized void shutdown(){
        if (shutdownRequested) {
            throw new IllegalStateException("shutdown already requested!");
        }

        // 调用告警信息发送线程的终止方法
        alarmSendingThread.terminate();

        shutdownRequested = true;
    }
}
