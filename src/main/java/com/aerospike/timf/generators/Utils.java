package com.aerospike.timf.generators;

import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    private static String NUMBERS = "1234567890";
    
    private static String collectionOf(String alphabet, int length) {
        StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(ThreadLocalRandom.current().nextInt(alphabet.length())));
        }
        return sb.toString();
    
    }
    public static String stringOfNumbers(int length) {
        return collectionOf(NUMBERS, length);
    }
    
}
