package com.github.mobile;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic weak store of ids to items
 */
public abstract class ItemStore {

    private static class ItemReference<V> extends WeakReference<V> {

        private Object id;

        /**
         * Create item reference
         *
         * @param item
         * @param id
         * @param queue
         */
        public ItemReference(V item, Object id, ReferenceQueue<? super V> queue) {
            super(item, queue);
            this.id = id;
        }
    }

    /**
     * Generic reference store
     *
     * @param <V>
     */
    protected static class ItemReferences<V> {

        private final ReferenceQueue<V> queue;

        private final Map<Object, ItemReference<V>> items;

        /**
         * Create reference store
         */
        public ItemReferences() {
            queue = new ReferenceQueue<V>();
            items = new HashMap<Object, ItemReference<V>>();
        }

        @SuppressWarnings("rawtypes")
        private void expungeEntries() {
            ItemReference ref;
            while ((ref = (ItemReference) queue.poll()) != null)
                items.remove(ref.id);
        }

        /**
         * Get item with id from store
         *
         * @param id
         * @return item
         */
        public V get(final Object id) {
            expungeEntries();
            WeakReference<V> ref = items.get(id);
            return ref != null ? ref.get() : null;
        }

        /**
         * Insert item with id into store
         *
         * @param id
         * @param item
         */
        public void put(Object id, V item) {
            expungeEntries();
            items.put(id, new ItemReference<V>(item, id, queue));
        }
    }
}
