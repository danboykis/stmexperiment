package com.danboykis.stmexperiment;

import clojure.lang.LockingTransaction;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AccountService {
    public static final AtomicInteger transferCnt = new AtomicInteger(0);
    final static Lock tieBreaker = new ReentrantLock();

    public static void transfer(final Account fromAcct, final Account toAcct, final DollarAmount amount) throws Exception {
        transferCnt.getAndIncrement();
        LockingTransaction.runInTransaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                toAcct.deposit(amount);
                fromAcct.withDraw(amount);
                return null;
            }
        });
    }


    public static void transferUnsafe(final AccountUnsafe fromAcct, final AccountUnsafe toAcct, final DollarAmount amount) throws Exception {
        transferCnt.getAndIncrement();
        toAcct.deposit(amount);
        fromAcct.withDraw(amount);
    }

    //Lock ordering implemented
    public static void transferLockCorrect(final AccountUnsafe fromAcct, final AccountUnsafe toAcct, final DollarAmount amount) {
        transferCnt.getAndIncrement();
        int fromHc = System.identityHashCode(fromAcct);
        int toHc = System.identityHashCode(toAcct);
        if(fromHc < toHc) {
            synchronized (fromAcct) {
                synchronized (toAcct) {
                    toAcct.deposit(amount);
                    fromAcct.withDraw(amount);
                }
            }
        } else if(toHc < fromHc) {
            synchronized (toAcct) {
                synchronized (fromAcct) {
                    toAcct.deposit(amount);
                    fromAcct.withDraw(amount);
                }
            }
        } else {
            tieBreaker.lock();
            try {
                synchronized (toAcct) {
                    synchronized (fromAcct) {
                        toAcct.deposit(amount);
                        fromAcct.withDraw(amount);
                    }
                }
            } finally {
                tieBreaker.unlock();
            }
        }
    }
}
