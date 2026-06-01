package retrieval;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom hash table with separate chaining (linked-list buckets).
 * Put / Get / Remove: O(1) average  |  O(n) worst-case (all in one bucket).
 * Automatically resizes at 75 % load factor to maintain O(1) amortised.
 */
public class HashMap<K, V> {

    // Start small and grow on demand when the table becomes crowded.
    private static final int DEFAULT_CAP  = 16;
    private static final double LOAD_FACTOR  = 0.75;

    // Each bucket stores a linked list of entries that share the same index.
    private static class Entry<K, V> {
        K key; 
        V value; 
        Entry<K, V> next;
        Entry(K k, V v) { 
            key = k; 
            value = v; 
        }
    }

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] buckets = new Entry[DEFAULT_CAP];
    private int size = 0;
    private int capacity = DEFAULT_CAP;

    // Core ops

    /**
     * Store or update a key-value pair.
     * If the key already exists in the bucket chain, only the value changes.
     */
    public void put(K key, V value) {
        if ((double) size / capacity >= LOAD_FACTOR) 
            resize();

        int idx = index(key);

        for (Entry<K, V> e = buckets[idx]; e != null; e = e.next) {
            if (e.key.equals(key)) { 
                e.value = value; return; 
            }
        }

        // New key: insert at the head of the bucket chain.
        Entry<K, V> n = new Entry<>(key, value);
        n.next = buckets[idx]; 
        buckets[idx] = n; 
        size++;
    }

    /**
     * Retrieve a value by key by walking only one bucket chain.
     * Returns null when the key is not present.
     */
    public V get(K key) {
        for (Entry<K, V> e = buckets[index(key)]; e != null; e = e.next)
            if (e.key.equals(key)) 
                return e.value;
        return null;
    }

    /**
     * Remove a key from its bucket chain.
     * The previous node is tracked so the chain can be relinked correctly.
     */
    public boolean remove(K key) {
        int idx = index(key);
        Entry<K, V> cur = buckets[idx], 
        prev = null;

        while (cur != null) {
            if (cur.key.equals(key)) {
                if (prev == null) 
                    buckets[idx] = cur.next; 
                else 
                    prev.next = cur.next;

                size--; 
                return true;
            }
            prev = cur; 
            cur = cur.next;
        }
        return false;
    }

    public boolean containsKey(K key) { 
        return get(key) != null; 
    }

    public int getSize() { 
        return size; 
    }

    public int getCapacity() { 
        return capacity; 
    }

    /**
     * Return all stored entries as [bucketIndex, key, value] string triples.
     * This is used for UI display, so the order follows bucket traversal.
     */
    public List<String[]> getEntries() {
        List<String[]> list = new ArrayList<>();
        for (int i = 0; i < capacity; i++)
            for (Entry<K, V> e = buckets[i]; e != null; e = e.next)
                list.add(new String[]{ String.valueOf(i), e.key.toString(), e.value.toString() });
        return list;
    }

    // Internal helpers

    // Map a key to a bucket index using its hash code.
    private int index(K key) { 
        return Math.abs(key.hashCode()) % capacity; 
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        // Rebuild the table with twice the capacity so collisions stay lower.
        capacity *= 2;
        Entry<K, V>[] nb = new Entry[capacity];
        for (int i = 0; i < buckets.length; i++) {
            Entry<K, V> e = buckets[i];
            while (e != null) {
                Entry<K, V> next = e.next;
                // Recompute the bucket because the capacity has changed.
                int ni = Math.abs(e.key.hashCode()) % capacity;
                e.next = nb[ni]; nb[ni] = e;
                e = next;
            }
        }
        buckets = nb;
    }
}
