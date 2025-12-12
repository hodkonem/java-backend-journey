package ru.itwizardry.algorithms.map;

public interface MyMap<K, V> {
    V put(K key, V value);

    V get(K key);

    V remove(K key);

    int size();

    void clear();
}
