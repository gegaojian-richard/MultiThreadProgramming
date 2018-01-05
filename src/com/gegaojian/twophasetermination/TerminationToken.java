package com.gegaojian.twophasetermination;

import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class TerminationToken {

    // todo: protected 使得同包类可以直接访问 安全问题
    protected volatile boolean toShutdown = false;

    /**
     * reservations是public的
     */
    public final AtomicInteger reservations = new AtomicInteger(0);

    /**
     * 多个可停止线程实例可以共享同一个TerminationToken实例，使用coordinatedThreads队列来记录关联的线程
     * 这里使用WeakReference弱引用，避免阻止GC回收无用的线程
     */
    private final Queue<WeakReference<Terminatable>> coordinatedThreads;

    public TerminationToken(){
        coordinatedThreads = new ConcurrentLinkedDeque<>();
    }

    public boolean isToShutdown(){
        return toShutdown;
    }

    // todo: 疑惑
    protected void setToShutdown(boolean toShutdown){
        this.toShutdown = true;
    }

    protected void register(Terminatable thread){
        coordinatedThreads.add(new WeakReference<Terminatable>(thread));
    }

    /**
     * 通知TerminationToken实例停止其他线程，当TerminationToken实例关联的一个可被终止线程终止了，则该TerminationToken实例关联的其他线程都会被终止
     * @param thread 已被停止的线程
     */
    protected void notifyThreadTermination(Terminatable thread){
        WeakReference<Terminatable> wrThread;
        Terminatable otherThread;
        while (null != (wrThread = coordinatedThreads.poll())){ // 返回queue的头部并从queue中将其删除
            otherThread = wrThread.get(); // 返回弱引用所引用的对象的引用，若对象已不存在则返回null
            if (null != otherThread && otherThread != thread){
                otherThread.terminate();
            }
        }
    }
}
