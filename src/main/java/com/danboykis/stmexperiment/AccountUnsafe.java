package com.danboykis.stmexperiment;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.math.BigInteger;

public class AccountUnsafe {
    public final BigInteger id;
    private DollarAmount dollarAmount;

    public AccountUnsafe(BigInteger id, DollarAmount amount) {
        this.id = id;
        this.dollarAmount = amount;
    }

    public BigInteger getId() {
        return id;
    }

    public DollarAmount getDollarAmount() {
        synchronized (this) {
            return dollarAmount;
        }
    }

    public void deposit(final DollarAmount amount) {
        Preconditions.checkState(amount != null, "Not a valid amount");
        synchronized (this) {
            DollarAmount newAmount =
                    new DollarAmount(getDollarAmount().getValue().add(amount.getValue()));
            this.dollarAmount = newAmount;
        }
    }

    public void withDraw(final DollarAmount amount) {
        if(getDollarAmount().compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough funds!");
        }
        synchronized (this) {
            DollarAmount newAmount =
                    new DollarAmount(getDollarAmount().getValue().subtract(amount.getValue()));
            this.dollarAmount = newAmount;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id",id)
                .add("dollarAmount",dollarAmount)
                .toString();
    }
}
