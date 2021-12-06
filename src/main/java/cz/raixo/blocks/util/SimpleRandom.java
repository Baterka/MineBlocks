package cz.raixo.blocks.util;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class SimpleRandom<E> {

    private final NavigableMap<Double, E> map = new TreeMap<>();
    private final Random random;
    private double total = 0;

    public SimpleRandom() {
        this(new Random());
    }

    public SimpleRandom(Random random) {
        this.random = random;
    }

    public SimpleRandom<E> add(double weight, E result) {
        if (weight <= 0) return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }

}
