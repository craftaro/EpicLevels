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
        this.expiration = -1;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getExpiration() {
        return this.expiration;
    }

    public double getMultiplier() {
        return this.multiplier;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Boost boost = (Boost) obj;
        return this.id == boost.id &&
                this.expiration == boost.expiration &&
                Double.compare(boost.multiplier, this.multiplier) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.expiration, this.multiplier);
    }
}
