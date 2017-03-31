/*
 * Copyright (C) 2017 Noe Fernandez
 */

package io.github.nfdz.savedio;

/**
 * This is a useful callback mechanism so we can abstract our async tasks.
 *
 * @param <T>
 */
public interface Callback<T> {
    /**
     * Invoked when the async task has completed its execution.
     * @param result The resulting object from the async task.
     */
    void onSuccess(T result);

    void onError(String msg, Throwable th);
}