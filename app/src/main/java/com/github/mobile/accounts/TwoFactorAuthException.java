/*
 * Copyright 2013 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.accounts;

import java.io.IOException;

/**
 * Exception class to be thrown when server responds with a 401 and
 * an X-GitHub-OTP: required;:2fa-type header.
 * This exception wraps an {@link IOException} that is the actual exception
 * that occurred when the request was made.
 */
public class TwoFactorAuthException extends IOException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3889626691109709714L;

    /**
     * Cause exception
     */
    protected final IOException cause;

    /**
     * Two-factor authentication type
     */
    protected final int twoFactorAuthType;

    /**
     * Create two-factor authentification exception
     *
     * @param cause
     * @param twoFactorAuthType
     */
    public TwoFactorAuthException(IOException cause, int twoFactorAuthType) {
        this.cause = cause;
        this.twoFactorAuthType = twoFactorAuthType;
    }

    @Override
    public String getMessage() {
        return cause != null ? cause.getMessage() : super.getMessage();
    }

    @Override
    public IOException getCause() {
        return cause;
    }
}