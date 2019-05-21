package com.songoda.epiclevels.boost;

public class Boost {

    private long expiration;
    private final double multiplier;

    public Boost(long expiration, double multiplier) {
        this.expiration = expiration;
        this.multiplier = multiplier;
    }

    public void expire() {
        expiration = -1;
    }

    public long getExpiration() {
        return expiration;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
