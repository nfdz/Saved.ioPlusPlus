/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.sync;

/**
 * Custom exception that contains any error that could happens while synchronization.
 */
public class SyncException extends Exception {
    static final long serialVersionUID = 1L;

    public SyncException(String msg) {
        super(msg);
    }

    public SyncException(String msg, Throwable th) {
        super(msg, th);
    }
}
