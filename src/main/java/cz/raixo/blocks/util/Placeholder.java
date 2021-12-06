package cz.raixo.blocks.util;

import java.util.Map;

public class Placeholder {

    public static String translate(String from, Map<String, String> data) {
        String n = from;
        for (Map.Entry<String, String> e : data.entrySet()) {
            n = n.replaceAll("(?i)%" + e.getKey() + "%", e.getValue());
        }
        return n;
    }

}
