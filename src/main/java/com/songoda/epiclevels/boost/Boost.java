package com.songoda.epiclevels.boost;

public class Boost {

    private final long expiration;
    private final double multiplier;

    public Boost(long expiration, double multiplier) {
        this.expiration = expiration;
        this.multiplier = multiplier;
    }

    public long getExpiration() {
        return expiration;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
