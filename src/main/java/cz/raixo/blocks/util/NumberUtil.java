package cz.raixo.blocks.util;

import java.util.Optional;

public class NumberUtil {

    public static Optional<Integer> parseInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public static Optional<Float> parseFloat(String s) {
        try {
            return Optional.of(Float.parseFloat(s));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

}
