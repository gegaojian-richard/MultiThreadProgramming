package com.gegaojian.guardedsuspension;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionVarBlocker implements Blocker{
    private final Lock lock;

    private final Condition condition;

    public ConditionVarBlocker(Lock lock) {
        this.lock = lock;
        this.condition = lock.newCondition();
    }

    public ConditionVarBlocker() {
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    /**
     *
     * @param guardedAction 受保护的目标动作
     * @param <V> 第一个<V> 是表示callWithGuard方法返回值的模板参数，第二个<V>是GuardedAction的模板参数
     * @return
     * @throws Exception
     */
    @Override
    public <V> V callWithGuard(GuardedAction<V> guardedAction) throws Exception {
        lock.lockInterruptibly();
        V result;
        try {
            final Predicate guard = guardedAction.guard;
            while (!guard.evaluate()) { // 使用while循环避免提前唤醒线程
                condition.await(); // 使当前线程在接到信号或被中断之前一直处于等待状态。
            }
            result = guardedAction.call();
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     *
     * @param stateOperation Callable对象，更改状态的操作，其call方法的返回值为true时，该方法才会唤醒被暂挂的线程
     * @throws Exception
     */
    @Override
    public void signalAfter(Callable<Boolean> stateOperation) throws Exception {
        lock.lockInterruptibly();
        try {
            if (stateOperation.call()) {
                condition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void signal() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void broadcastAfter(Callable<Boolean> stateOperation) throws Exception {
        lock.lockInterruptibly();
        try{
            if(stateOperation.call()){
                condition.signalAll();
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void broadcast() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
