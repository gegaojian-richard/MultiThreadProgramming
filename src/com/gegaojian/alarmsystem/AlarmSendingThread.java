package com.gegaojian.alarmsystem;


import com.gegaojian.twophasetermination.AbstractTerminatableThread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负责调用AlarmAgent的相关方法发送告警信息
 */
// 模式角色：Two-phaseTermination.ConcreteTerminatableThread
public class AlarmSendingThread extends AbstractTerminatableThread{
    private final AlarmAgent alarmAgent = AlarmAgent.getInstance();

    // 告警信息队列
    private final BlockingQueue<AlarmInfo> alarmQueue;
    // 告警信息注册表
    private final ConcurrentMap<String, AtomicInteger> submittedAlarmRegistry; // key:告警信息唯一标识 value:重复数，不重复为0

    public AlarmSendingThread(){
        alarmQueue = new ArrayBlockingQueue<AlarmInfo>(100);
        submittedAlarmRegistry = new ConcurrentHashMap<>();
    }

    @Override
    protected void doRun() throws Exception {
        AlarmInfo alarmInfo;
        alarmInfo = alarmQueue.take(); // 有则取，无则等待
        terminationToken.reservations.decrementAndGet(); // 任务计数-1

        try {
            // 发送
            alarmAgent.sendAlarm(alarmInfo);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 处理恢复告警:将对应的故障告警从注册表中删除
        // Todo: 注意告警系统只负责发送告警信息，不负责处理故障，故障恢复后会有相应的恢复告警信息提交
        if (AlarmType.RESUME == alarmInfo.getType()){
            String key = AlarmType.FAULT.toString() + ":" + alarmInfo.getId() + "@" + alarmInfo.getInfo();
            submittedAlarmRegistry.remove(key);
            key = AlarmType.RESUME.toString() + ":" + alarmInfo.getId() + "@" + alarmInfo.getInfo();
            submittedAlarmRegistry.remove(key);
        }
    }

    public int sendAlarm(final AlarmInfo alarmInfo){
        AlarmType type = alarmInfo.getType();
        String id = alarmInfo.getId();
        String extraInfo = alarmInfo.getInfo();

        // 若已经准备关闭则不再接受新的告警信息
        if(terminationToken.isToShutdown()){
            //记录被拒绝的告警
            System.err.println("rejected alarm: " + id + "," + extraInfo);

            return -1;
        }

        int duplicateSubmissionCount = 0;
        try {
            AtomicInteger prevSubmittedCounter;

            // 查看告警信息注册表是否为重复告警信息，如果不是则注册告警信息并返回null
            prevSubmittedCounter = submittedAlarmRegistry.putIfAbsent(type.toString() + ":" + id + "@" + extraInfo,
                    new AtomicInteger(0));

            if (null == prevSubmittedCounter){ // 新的告警信息
                terminationToken.reservations.incrementAndGet();// 任务计数+1
                alarmQueue.put(alarmInfo); // 添加进告警信息队列
            }else { // 重复的未恢复故障
                // 仅在注册表中增加计数
                duplicateSubmissionCount = prevSubmittedCounter.incrementAndGet();
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

        return duplicateSubmissionCount;
    }

    @Override
    protected void doCleanup(Exception cause) {
        if (null != cause && !(cause instanceof InterruptedException)){
            cause.printStackTrace();
        }
        alarmAgent.disConnected();
    }
}
