package com.danboykis.stmexperiment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountServiceTest {

    static final int INNER_LOOP = 1000;
    static final int THREADS = 100;

    ExecutorService executorService;
    final CountDownLatch stopBarrier = new CountDownLatch(THREADS);
    final CountDownLatch startBarrier = new CountDownLatch(1);

    @Before
    public void setUp() {
        AccountService.transferCnt.set(0);
        executorService = Executors.newFixedThreadPool(THREADS);
    }

    @Test
    public void testSTM() throws InterruptedException {
        final Account account1 = new Account(BigInteger.ZERO,DollarAmount.of(100.0));
        final Account account2 = new Account(BigInteger.ONE,DollarAmount.of(100.0));

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    startBarrier.await();
                    for(int i=0; i<INNER_LOOP; i++) {
                        AccountService.transfer(account1, account2, DollarAmount.of(1.0));
                        AccountService.transfer(account2, account1, DollarAmount.of(1.0));
                    }
                    stopBarrier.countDown();
                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(1);
                }
            }
        };
        long startTime = System.currentTimeMillis();
        for(int i=0; i<THREADS; i++) {
            executorService.submit(r);
        }
        startBarrier.countDown();
        try {
            for(int i=0; i<100; i++) account1.deposit(DollarAmount.of(BigDecimal.ONE));
        } catch (Exception e) { e.printStackTrace(); }
        stopBarrier.await();
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time: "+endTime/1000.0);
        Assert.assertTrue(AccountService.transferCnt.get() == THREADS * 2 * INNER_LOOP);
        System.out.println("-----");
        System.out.println(account1);
        System.out.println(account2);
        //The only example where it's possible to assert on the values
        Assert.assertTrue(account1.getDollarAmount().plus(100).equals(account2.getDollarAmount()));
    }

    @Test
    public void testUnsafeLock() throws InterruptedException {
        final AccountUnsafe account1 = new AccountUnsafe(BigInteger.ZERO,DollarAmount.of(100.0));
        final AccountUnsafe account2 = new AccountUnsafe(BigInteger.ONE,DollarAmount.of(100.0));
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    startBarrier.await();
                    for(int i=0; i<INNER_LOOP; i++) {
                        AccountService.transferUnsafe(account1, account2, DollarAmount.of(1.0));
                        AccountService.transferUnsafe(account2, account1, DollarAmount.of(1.0));
                    }
                    stopBarrier.countDown();
                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(1);
                }
            }
        };
        long startTime = System.currentTimeMillis();
        for(int i=0; i<THREADS; i++) {
            executorService.submit(r);
        }
        startBarrier.countDown();
        //Underlines the problem with lock not composing
        for(int i=0; i<100; i++) account1.deposit(DollarAmount.of(BigDecimal.ONE));
        stopBarrier.await();
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time: "+endTime/1000.0);
        Assert.assertTrue(AccountService.transferCnt.get() == THREADS * 2 * INNER_LOOP);
        System.out.println("-----");
        System.out.println(account1);
        System.out.println(account2);
    }

    @Test
    public void testSafeLock() throws InterruptedException {
        final AccountUnsafe account1 = new AccountUnsafe(BigInteger.ZERO,DollarAmount.of(100.0));
        final AccountUnsafe account2 = new AccountUnsafe(BigInteger.ONE,DollarAmount.of(100.0));
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    startBarrier.await();
                    for(int i=0; i<INNER_LOOP; i++) {
                        AccountService.transferLockCorrect(account1, account2, DollarAmount.of(1.0));
                        AccountService.transferLockCorrect(account2, account1, DollarAmount.of(1.0));
                    }
                    stopBarrier.countDown();
                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(1);
                }
            }
        };
        long startTime = System.currentTimeMillis();
        for(int i=0; i<THREADS; i++) {
            executorService.submit(r);
        }
        startBarrier.countDown();
        for(int i=0; i<100; i++) account1.deposit(DollarAmount.of(BigDecimal.ONE));
        stopBarrier.await();
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time: "+endTime/1000.0);
        Assert.assertTrue(AccountService.transferCnt.get() == THREADS * 2 * INNER_LOOP);
        System.out.println("-----");
        System.out.println(account1);
        System.out.println(account2);
    }
}
