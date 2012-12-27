package com.danboykis.stmexperiment;

import clojure.lang.Ref;
import clojure.lang.LockingTransaction;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.util.concurrent.Callable;

public class Account {
    public final BigInteger id;
    private final Ref dollarAmountRef;

    public Account(BigInteger id, DollarAmount amount) {
        this.id = id;
        this.dollarAmountRef = new Ref(amount);
    }

    public BigInteger getId() {
        return id;
    }

    public DollarAmount getDollarAmount() {
        return (DollarAmount)dollarAmountRef.deref();
    }

    public void deposit(final DollarAmount amount) throws Exception {
        LockingTransaction.runInTransaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Preconditions.checkState(amount!=null,"Not a valid amount");
                DollarAmount newAmount =
                        new DollarAmount(getDollarAmount().getValue().add(amount.getValue()));
                dollarAmountRef.set(newAmount);
                return null;
            }
        });
    }

    public void withDraw(final DollarAmount amount) throws Exception {
        LockingTransaction.runInTransaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Preconditions.checkState(amount!=null,"Not a valid amount");
                if(getDollarAmount().compareTo(amount) < 0) {
                    throw new IllegalStateException("Not enough funds!");
                }
                DollarAmount newAmount =
                        new DollarAmount(getDollarAmount().getValue().subtract(amount.getValue()));
                dollarAmountRef.set(newAmount);
                return null;
            }
        });
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id",id)
                .add("dollarAmount",dollarAmountRef.deref())
                .toString();
    }
}
