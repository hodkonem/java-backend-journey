package ru.itwizardry.algorithms.map;

import java.util.Objects;

public class MyHashMap<K, V> implements MyMap<K, V> {
    private static final int INITIAL_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    private Entry<K, V>[] table;
    private int size;
    private int threshold;

    @SuppressWarnings("unchecked")
    public MyHashMap() {

        table = (Entry<K, V>[]) new Entry[INITIAL_CAPACITY];
        threshold = (int) (INITIAL_CAPACITY * LOAD_FACTOR);
    }

    @Override
    public V put(K key, V value) {
        int position = getElementPosition(key, table.length);
        Entry<K, V> head = table[position];

        if (head == null) {
            table[position] = new Entry<>(key, value, null);
            size++;
            if (size > threshold) {
                resize();
            }
            return null;
        }

        Entry<K, V> current = head;
        while (current != null) {
            if (Objects.equals(current.key, key)) {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            current = current.next;
        }

        table[position] = new Entry<>(key, value, head);
        size++;
        if (size > threshold) {
            resize();
        }
        return null;
    }

    @Override
    public V get(K key) {
        int position = getElementPosition(key, table.length);
        Entry<K, V> existedElement = table[position];
        while (existedElement != null) {
            if (Objects.equals(existedElement.key, key)) {
                return existedElement.value;
            }
            existedElement = existedElement.next;
        }
        return null;
    }

    @Override
    public V remove(K key) {
        int position = getElementPosition(key, table.length);
        Entry<K, V> current = table[position];
        Entry<K, V> prev = null;
        while (current != null) {
            if (Objects.equals(current.key, key)) {
                V oldValue = current.value;
                if (prev == null) {
                    table[position] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return oldValue;
            } else {
                prev = current;
                current = current.next;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
        size = 0;
    }

    private int getElementPosition(K key, int arrayLength) {
        if (key == null) {
            return 0;
        }
        int hash = key.hashCode();
        return (hash & 0x7fffffff) % arrayLength;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] oldTable = table;
        int newCapacity = oldTable.length * 2;

        Entry<K, V>[] newTable = (Entry<K, V>[]) new Entry[newCapacity];

        for (int i = 0; i < oldTable.length; i++) {
            Entry<K, V> current = oldTable[i];

            while (current != null) {
                Entry<K, V> next = current.next;

                int newIndex = getElementPosition(current.key, newCapacity);

                current.next = newTable[newIndex];
                newTable[newIndex] = current;

                current = next;
            }
        }

        table = newTable;
        threshold = (int) (newCapacity * LOAD_FACTOR);
    }


    private static class Entry<K, V> {
        private final K key;
        private V value;
        private Entry<K, V> next;

        public Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
}
