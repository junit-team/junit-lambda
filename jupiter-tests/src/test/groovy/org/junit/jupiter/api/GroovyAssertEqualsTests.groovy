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

import java.util.function.Supplier
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.PrimitiveAndWrapperTypeHelpers.*

class GroovyAssertEqualsTests {

    Supplier<String> supplier = { '' }

    @Test
    void "null references can be passed to assertEquals"() {
        Object null1 = null
        Object null2 = null

        assertEquals(null1, null)
        assertEquals(null, null2)
        assertEquals(null1, null2)
    }

    @Test
    void "integers can be passed to assertEquals"() {
        assertEquals(i(42), i(42))
        assertEquals(i(42), I(42))
        assertEquals(I(42), i(42))
        assertEquals(I(42), I(42))

        assertEquals(i(42), i(42), '')
        assertEquals(i(42), I(42), '')
        assertEquals(I(42), i(42), '')
        assertEquals(I(42), I(42), '')

        assertEquals(i(42), i(42), supplier)
        assertEquals(i(42), I(42), supplier)
        assertEquals(I(42), i(42), supplier)
        assertEquals(I(42), I(42), supplier)
    }

    @Test
    void "floats can be passed to assertEquals"() {
        assertEquals(f(42), f(42))
        assertEquals(f(42), F(42))
        assertEquals(F(42), f(42))
        assertEquals(F(42), F(42))

        assertEquals(f(42), f(42), '')
        assertEquals(f(42), F(42), '')
        assertEquals(F(42), f(42), '')
        assertEquals(F(42), F(42), '')

        assertEquals(f(42), f(42), supplier)
        assertEquals(f(42), F(42), supplier)
        assertEquals(F(42), f(42), supplier)
        assertEquals(F(42), F(42), supplier)
    }

    @Test
    void "floats can be passed to assertEquals with delta"() {
        assertEquals(f(42), f(42), 0.01f)
        assertEquals(f(42), F(42), 0.01f)
        assertEquals(F(42), f(42), 0.01f)
        assertEquals(F(42), F(42), 0.01f)

        assertEquals(f(42), f(42), 0.01f, '')
        assertEquals(f(42), F(42), 0.01f, '')
        assertEquals(F(42), f(42), 0.01f, '')
        assertEquals(F(42), F(42), 0.01f, '')

        assertEquals(f(42), f(42), 0.01f, supplier)
        assertEquals(f(42), F(42), 0.01f, supplier)
        assertEquals(F(42), f(42), 0.01f, supplier)
        assertEquals(F(42), F(42), 0.01f, supplier)
    }

    @Test
    void "bytes can be passed to assertEquals"() {
        assertEquals(b(42), b(42))
        assertEquals(b(42), B(42))
        assertEquals(B(42), b(42))
        assertEquals(B(42), B(42))

        assertEquals(b(42), b(42), '')
        assertEquals(b(42), B(42), '')
        assertEquals(B(42), b(42), '')
        assertEquals(B(42), B(42), '')

        assertEquals(b(42), b(42), supplier)
        assertEquals(b(42), B(42), supplier)
        assertEquals(B(42), b(42), supplier)
        assertEquals(B(42), B(42), supplier)
    }

    @Test
    void "doubles can be passed to assertEquals"() {
        assertEquals(d(42), d(42))
        assertEquals(d(42), D(42))
        assertEquals(D(42), d(42))
        assertEquals(D(42), D(42))

        assertEquals(d(42), d(42), '')
        assertEquals(d(42), D(42), '')
        assertEquals(D(42), d(42), '')
        assertEquals(D(42), D(42), '')

        assertEquals(d(42), d(42), supplier)
        assertEquals(d(42), D(42), supplier)
        assertEquals(D(42), d(42), supplier)
        assertEquals(D(42), D(42), supplier)
    }

    @Test
    void "doubles can be passed to assertEquals with delta"() {
        assertEquals(d(42), d(42), 0.01d)
        assertEquals(d(42), D(42), 0.01d)
        assertEquals(D(42), d(42), 0.01d)
        assertEquals(D(42), D(42), 0.01d)

        assertEquals(d(42), d(42), 0.01d, '')
        assertEquals(d(42), D(42), 0.01d, '')
        assertEquals(D(42), d(42), 0.01d, '')
        assertEquals(D(42), D(42), 0.01d, '')

        assertEquals(d(42), d(42), 0.01d, supplier)
        assertEquals(d(42), D(42), 0.01d, supplier)
        assertEquals(D(42), d(42), 0.01d, supplier)
        assertEquals(D(42), D(42), 0.01d, supplier)
    }

    @Test
    void "chars can be passed to assertEquals"() {
        assertEquals(c(42), c(42))
        assertEquals(c(42), C(42))
        assertEquals(C(42), c(42))
        assertEquals(C(42), C(42))

        assertEquals(c(42), c(42), '')
        assertEquals(c(42), C(42), '')
        assertEquals(C(42), c(42), '')
        assertEquals(C(42), C(42), '')

        assertEquals(c(42), c(42), supplier)
        assertEquals(c(42), C(42), supplier)
        assertEquals(C(42), c(42), supplier)
        assertEquals(C(42), C(42), supplier)
    }

    @Test
    void "longs can be passed to assertEquals"() {
        assertEquals(l(42), l(42))
        assertEquals(l(42), L(42))
        assertEquals(L(42), l(42))
        assertEquals(L(42), L(42))

        assertEquals(l(42), l(42), '')
        assertEquals(l(42), L(42), '')
        assertEquals(L(42), l(42), '')
        assertEquals(L(42), L(42), '')

        assertEquals(l(42), l(42), supplier)
        assertEquals(l(42), L(42), supplier)
        assertEquals(L(42), l(42), supplier)
        assertEquals(L(42), L(42), supplier)
    }

    @Test
    void "shorts can be passed to assertEquals"() {
        assertEquals(s(42), s(42))
        assertEquals(s(42), S(42))
        assertEquals(S(42), s(42))
        assertEquals(S(42), S(42))

        assertEquals(s(42), s(42), '')
        assertEquals(s(42), S(42), '')
        assertEquals(S(42), s(42), '')
        assertEquals(S(42), S(42), '')

        assertEquals(s(42), s(42), supplier)
        assertEquals(s(42), S(42), supplier)
        assertEquals(S(42), s(42), supplier)
        assertEquals(S(42), S(42), supplier)
    }

}
