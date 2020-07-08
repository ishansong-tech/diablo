package com.ishansong.diablo.core.exception;

public class DiabloException extends RuntimeException {

    private static final long serialVersionUID = 8068509879445395353L;

    public DiabloException(final Throwable e) {
        super(e);
    }

    public DiabloException(final String message) {
        super(message);
    }

    public DiabloException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
