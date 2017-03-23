package com.github.olloginov.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmartArray {
    public static <T> List<T> safeList(T[] array) {
        if (array != null) {
            return Arrays.asList(array);
        }
        return Collections.emptyList();
    }
}
