/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
