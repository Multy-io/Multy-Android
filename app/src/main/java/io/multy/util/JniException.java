package io.multy.util;

/**
 * Singletone for throwing exception from jni layer
 */
public class JniException extends Exception {

    public static void thhrowJniException(String message) {
//        throw new Exception(message);
    }

}
