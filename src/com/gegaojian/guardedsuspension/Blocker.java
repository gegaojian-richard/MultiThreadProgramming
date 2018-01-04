package com.gegaojian.guardedsuspension;

import java.util.concurrent.Callable;

/**
 * 阻塞唤醒控制器
 */
public interface Blocker {
    /**
     * 执行受保护的目标动作，当且仅当受保护的目标动作中的保护条件成立时，否则阻塞该线程，直到保护条件成立
     * @param guardedAction 受保护的目标动作
     * @param <V> 第一个<V> 是表示callWithGuard方法返回值的模板参数，第二个<V>是GuardedAction的模板参数
     * @return
     * @throws Exception
     */
    <V> V callWithGuard(GuardedAction<V> guardedAction) throws Exception;

    /**
     * 执行stateOperation所指定的操作后，决定是否要唤醒本Blocker所暂挂的所有线程中的一个线程
     * @param stateOperation Callable对象，更改状态的操作，其call方法的返回值为true时，该方法才会唤醒被暂挂的线程
     * @throws Exception
     */
    void signalAfter(Callable<Boolean> stateOperation) throws Exception;

    /**
     * 无序其他操作，直接唤醒本Block所暂挂的所有线程中的一个线程
     * @throws InterruptedException
     */
    void signal() throws InterruptedException;

    /**
     * 执行stateOperation所指定的操作后，决定是否要唤醒本Blocker所暂挂的所有线程
     * @param stateOperation Callable对象，更改状态的操作，其call方法的返回值为true时，该方法才会唤醒被暂挂的线程
     * @throws Exception
     */
    void broadcastAfter(Callable<Boolean> stateOperation) throws Exception;

    /**
     * 无序其他操作，直接唤醒本Blocker所暂挂的所有线程
     * @throws InterruptedException
     */
    void broadcast() throws InterruptedException;
}
