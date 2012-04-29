/*
 * Copyright 2012 GitHub Inc.
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
package com.github.mobile.guice;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

import java.util.Map;

public abstract class ScopeBase implements Scope {

    private static final Provider<Object> SEEDED_KEY_PROVIDER = new Provider<Object>() {
        public Object get() {
            throw new IllegalStateException(
                    "If you got here then it means that"
                            + " your code asked for scoped object which should have been"
                            + " explicitly seeded in this scope, but was not.");
        }
    };

    /**
     * Returns a provider that always throws an exception complaining that the
     * object in question must be seeded before it can be injected.
     *
     * @return typed provider
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> Provider<T> seededKeyProvider() {
        return (Provider<T>) SEEDED_KEY_PROVIDER;
    }

    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return new Provider<T>() {
            public T get() {
                Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);

                @SuppressWarnings("unchecked")
                T current = (T) scopedObjects.get(key);
                if (current == null && !scopedObjects.containsKey(key)) {
                    current = unscoped.get();
                    scopedObjects.put(key, current);
                }
                return current;
            }
        };
    }

    protected abstract <T> Map<Key<?>, Object> getScopedObjectMap(Key<T> key);
}
