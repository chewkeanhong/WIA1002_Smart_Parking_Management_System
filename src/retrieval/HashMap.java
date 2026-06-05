package retrieval;

import java.util.ArrayList;
import java.util.List;

// Our own hash table. Collisions are handled with separate chaining -
// each bucket is a little linked list of entries. Once it gets too full
// (past 75%) we grow the table and rehash everything.
public class HashMap<K, V> {

    private static final int DEFAULT_CAP  = 16;
    private static final double LOAD_FACTOR  = 0.75;

    // one node in a bucket chain
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
    private Entry<K, V>[] buckets;
    private int size = 0;
    private int capacity;

    public HashMap() {
        this.capacity = DEFAULT_CAP;
        this.buckets  = new Entry[DEFAULT_CAP];
    }

    @SuppressWarnings("unchecked")
    public HashMap(int initialCapacity) {
        this.capacity = initialCapacity;
        this.buckets  = new Entry[initialCapacity];
    }

    // add a key/value, or just update the value if the key is already there
    public void put(K key, V value) {
        if ((double) size / capacity >= LOAD_FACTOR)
            resize();

        int idx = index(key);

        for (Entry<K, V> e = buckets[idx]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                e.value = value; return;
            }
        }

        // brand new key - stick it at the front of the chain
        Entry<K, V> n = new Entry<>(key, value);
        n.next = buckets[idx];
        buckets[idx] = n;
        size++;
    }

    // look up a value - only has to scan one bucket's chain. null if missing.
    public V get(K key) {
        for (Entry<K, V> e = buckets[index(key)]; e != null; e = e.next)
            if (e.key.equals(key))
                return e.value;
        return null;
    }

    // remove a key. we track the previous node so we can relink around it.
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

    @SuppressWarnings("unchecked")
    public void clear() {
        buckets = new Entry[DEFAULT_CAP];
        size = 0;
        capacity = DEFAULT_CAP;
    }

    // flatten everything into [bucket, key, value] rows for the UI table
    public List<String[]> getEntries() {
        List<String[]> list = new ArrayList<>();
        for (int i = 0; i < capacity; i++)
            for (Entry<K, V> e = buckets[i]; e != null; e = e.next)
                list.add(new String[]{ String.valueOf(i), e.key.toString(), e.value.toString() });
        return list;
    }

    // which bucket a key lands in
    private int index(K key) {
        return Math.abs(key.hashCode()) % capacity;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        // double the capacity and re-hash every entry into the new table
        capacity *= 2;
        Entry<K, V>[] nb = new Entry[capacity];
        for (int i = 0; i < buckets.length; i++) {
            Entry<K, V> e = buckets[i];
            while (e != null) {
                Entry<K, V> next = e.next;
                int ni = Math.abs(e.key.hashCode()) % capacity;
                e.next = nb[ni]; nb[ni] = e;
                e = next;
            }
        }
        buckets = nb;
    }
}
