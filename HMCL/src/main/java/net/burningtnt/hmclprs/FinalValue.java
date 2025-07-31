package net.burningtnt.hmclprs;

public final class FinalValue<T> {
    private volatile T value;

    public FinalValue() {
    }

    public T getValue() {
        T v = value;
        if (v == null) {
            throw new IllegalStateException("Final Value hasn't been initialized.");
        }
        return v;
    }

    public void setValue(T v) {
        synchronized (this) {
            if (value != null) {
                throw new IllegalStateException("Final Value has been initialized.");
            }
            this.value = v;
        }
    }
}
