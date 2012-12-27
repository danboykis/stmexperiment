package com.danboykis.stmexperiment;

import com.google.common.base.Objects;

import java.math.BigDecimal;

public class DollarAmount implements Comparable<DollarAmount> {
    private final BigDecimal value;

    public static DollarAmount of(double value) { return new DollarAmount(value); }
    public static DollarAmount of(BigDecimal value) { return new DollarAmount(value); }

    public DollarAmount(double value) {
        this.value = BigDecimal.valueOf(value);
    }

    public DollarAmount(BigDecimal value) {
        this.value = value;
    }

    public DollarAmount(DollarAmount dollarAmount) {
        this(dollarAmount.value);
    }

    public DollarAmount plus(int n) { return DollarAmount.of(value.subtract(BigDecimal.valueOf(n))); }
    public DollarAmount minus(int n) { return DollarAmount.of(value.subtract(BigDecimal.valueOf(n))); }

    public BigDecimal getValue() { return value; }

    @Override
    public int compareTo(DollarAmount dollarAmount) {
        return value.compareTo(dollarAmount.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DollarAmount that = (DollarAmount) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    public String toString() {
        return Objects.toStringHelper(this)
                .add("value",value)
                .toString();
    }
}
