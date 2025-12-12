package ru.itwizardry.algorithms.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MyHashMapTest {

    private MyMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new MyHashMap<>();
    }

    @Test
    void putAndGetShouldWork() {
        map.put("a", 1);
        Integer value = map.get("a");

        assertEquals(1, value);
        assertEquals(1, map.size());
    }

    @Test
    void putSameKeyShouldOverwriteValueAndNotIncreaseSize() {
        map.put("BMW", 1);
        map.put("BMW", 2);

        assertEquals(2, map.get("BMW"));
        assertEquals(1, map.size());
    }

    @Test
    void getShouldReturnNullIfKeyDoesNotExist() {
        map.put("Moscow", 1);

        assertEquals(1, map.size());
        assertNull(map.get("Atlanta"));
        assertEquals(1, map.size());
    }

    @Test
    void removeShouldReturnValueAndDecreaseSize() {
        map.put("Bus", 1);
        map.put("Car", 2);

        assertEquals(2, map.size());

        Integer removed = map.remove("Car");

        assertEquals(2, removed);
        assertNull(map.get("Car"));
        assertEquals(1, map.size());
    }

    @Test
    void removeShouldReturnNullIfKeyDoesNotExist() {
        map.put("Kafka", 1);

        Integer removed = map.remove("Golang");

        assertNull(removed);
        assertEquals(1, map.size());
    }

    @Test
    void nullKeyShouldBeSupported() {
        map.put(null, 50);

        assertEquals(50, map.get(null));
        assertEquals(1, map.size());

        Integer removed = map.remove(null);

        assertEquals(50, removed);
        assertNull(map.get(null));
        assertEquals(0, map.size());
    }

    @Test
    void collisionShouldBeHandledCorrectly() {
        MyMap<BadHashKey, Integer> collisionMap = new MyHashMap<>();

        BadHashKey k1 = new BadHashKey("k1");
        BadHashKey k2 = new BadHashKey("k2");

        collisionMap.put(k1, 10);
        collisionMap.put(k2, 20);

        assertEquals(2, collisionMap.size());
        assertEquals(10, collisionMap.get(k1));
        assertEquals(20, collisionMap.get(k2));

        Integer removed = collisionMap.remove(k1);

        assertEquals(10, removed);
        assertNull(collisionMap.get(k1));
        assertEquals(20, collisionMap.get(k2));
        assertEquals(1, collisionMap.size());
    }

    @Test
    void clearShouldRemoveAllEntries() {
        map.put("a", 1);
        map.put("b", 2);

        map.clear();

        assertEquals(0, map.size());
        assertNull(map.get("a"));
        assertNull(map.get("b"));
    }



    private static final class BadHashKey {
        private final String id;

        private BadHashKey(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BadHashKey other)) return false;
            return id.equals(other.id);
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }
}