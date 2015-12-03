package com.github.pockethub.core;

import java.io.IOException;
import java.util.NoSuchElementException;

public class NoSuchPageException extends NoSuchElementException {

    protected final IOException cause;

    public NoSuchPageException(IOException cause) {
        this.cause = cause;
    }

    public String getMessage() {
        return this.cause != null ? this.cause.getMessage() : super.getMessage();
    }

    public IOException getCause() {
        return this.cause;
    }
}
