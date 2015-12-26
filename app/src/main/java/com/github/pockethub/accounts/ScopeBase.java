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
package com.github.pockethub.accounts;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

import java.util.Map;

/**
 * Base scope class
 */
public abstract class ScopeBase implements Scope {

    private static final Provider<Object> SEEDED_KEY_PROVIDER = new Provider<Object>() {
        public Object get() {
            throw new IllegalStateException("Object not seeded in this scope");
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

    /**
     * Get scoped object map
     *
     * @param key
     * @return map
     */
    protected abstract <T> Map<Key<?>, Object> getScopedObjectMap(Key<T> key);
}
