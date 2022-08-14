package com.songoda.epiclevels.boost;

import java.util.Objects;

public class Boost {

    private int id;

    private long expiration;
    private final double multiplier;

    public Boost(long expiration, double multiplier) {
        this.expiration = expiration;
        this.multiplier = multiplier;
    }

    public Boost(int id, long expiration, double multiplier) {
        this(expiration, multiplier);
        this.id = id;
    }

    public void expire() {
        expiration = -1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getExpiration() {
        return expiration;
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Boost boost = (Boost) o;
        return id == boost.id && expiration == boost.expiration && Double.compare(boost.multiplier, multiplier) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, expiration, multiplier);
    }
}
