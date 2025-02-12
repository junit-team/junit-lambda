/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.api

class PrimitiveAndWrapperTypeHelpers {

    static char c(int number) {
        return (char) number
    }

    static Character C(int number) {
        return Character.valueOf((char) number)
    }

    static byte b(int number) {
        return (byte) number
    }

    static Byte B(int number) {
        return Byte.valueOf((byte) number)
    }

    static double d(int number) {
        return (double) number
    }

    static Double D(int number) {
        return Double.valueOf((double) number)
    }

    static float f(int number) {
        return (float) number
    }

    static Float F(int number) {
        return Float.valueOf((float) number)
    }

    static long l(int number) {
        return (long) number
    }

    static Long L(int number) {
        return Long.valueOf( (long) number)
    }

    static short s(int number) {
        return (short) number
    }

    static Short S(int number) {
        return Short.valueOf( (short) number)
    }

    static int i(int number) {
        return  number
    }

    static Integer I(int number) {
        return Integer.valueOf( number)
    }

}
