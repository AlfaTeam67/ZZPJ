package com.fininsight.marketdata.exception;

import java.util.UUID;

public class PriceSnapshotNotFoundException extends RuntimeException {
    public PriceSnapshotNotFoundException(UUID id) {
        super("Price snapshot not found: " + id);
    }
}
