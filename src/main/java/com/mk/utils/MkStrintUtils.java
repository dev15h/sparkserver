package com.mk.utils;

/**
 * Created by margish on 7/4/15.
 */
public class MkStrintUtils {
    public static String removeSpecialChars(String input) {
        return input.replaceAll("[^A-Za-z0-9]", "");
    }

    public static String removeSpecialChars(String input, String except) {
        input = input.replace(except, "xxxxxxxxyyyyyyzzzzz");
        input = input.replaceAll("[^A-Za-z0-9]", "");
        input = input.replace("xxxxxxxxyyyyyyzzzzz", except);
        return  input;
    }
}
