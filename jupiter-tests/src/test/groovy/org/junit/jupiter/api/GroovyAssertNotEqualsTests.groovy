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

import org.opentest4j.AssertionFailedError

import java.util.function.Supplier

import static org.junit.jupiter.api.Assertions.assertNotEquals
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.PrimitiveAndWrapperTypeHelpers.*

class GroovyAssertNotEqualsTests {

    Supplier<String> supplier = { '' }

    @Test
    void "null references can be passed to assertNotEquals"() {
        Object null1 = null
        Object null2 = null

        assertThrows(AssertionFailedError, { assertNotEquals(null1, null) } )
        assertThrows(AssertionFailedError, { assertNotEquals(null, null2) } )
        assertThrows(AssertionFailedError, { assertNotEquals(null1, null2) } )
    }

    @Test
    void "integers can be passed to assertNotEquals"() {
        assertNotEquals(i(42), i(2))
        assertNotEquals(i(42), I(2))
        assertNotEquals(I(42), i(2))
        assertNotEquals(I(42), I(2))

        assertNotEquals(i(42), i(2), '')
        assertNotEquals(i(42), I(2), '')
        assertNotEquals(I(42), i(2), '')
        assertNotEquals(I(42), I(2), '')

        assertNotEquals(i(42), i(2), supplier)
        assertNotEquals(i(42), I(2), supplier)
        assertNotEquals(I(42), i(2), supplier)
        assertNotEquals(I(42), I(2), supplier)
    }

    @Test
    void "floats can be passed to assertNotEquals"() {
        assertNotEquals(f(42), f(2))
        assertNotEquals(f(42), F(2))
        assertNotEquals(F(42), f(2))
        assertNotEquals(F(42), F(2))

        assertNotEquals(f(42), f(2), '')
        assertNotEquals(f(42), F(2), '')
        assertNotEquals(F(42), f(2), '')
        assertNotEquals(F(42), F(2), '')

        assertNotEquals(f(42), f(2), supplier)
        assertNotEquals(f(42), F(2), supplier)
        assertNotEquals(F(42), f(2), supplier)
        assertNotEquals(F(42), F(2), supplier)
    }

    @Test
    void "floats can be passed to assertNotEquals with delta"() {
        assertNotEquals(f(42), f(2), 0.01f)
        assertNotEquals(f(42), F(2), 0.01f)
        assertNotEquals(F(42), f(2), 0.01f)
        assertNotEquals(F(42), F(2), 0.01f)

        assertNotEquals(f(42), f(2), 0.01f, '')
        assertNotEquals(f(42), F(2), 0.01f, '')
        assertNotEquals(F(42), f(2), 0.01f, '')
        assertNotEquals(F(42), F(2), 0.01f, '')

        assertNotEquals(f(42), f(2), 0.01f, supplier)
        assertNotEquals(f(42), F(2), 0.01f, supplier)
        assertNotEquals(F(42), f(2), 0.01f, supplier)
        assertNotEquals(F(42), F(2), 0.01f, supplier)
    }

    @Test
    void "bytes can be passed to assertNotEquals"() {
        assertNotEquals(b(42), b(2))
        assertNotEquals(b(42), B(2))
        assertNotEquals(B(42), b(2))
        assertNotEquals(B(42), B(2))

        assertNotEquals(b(42), b(2), '')
        assertNotEquals(b(42), B(2), '')
        assertNotEquals(B(42), b(2), '')
        assertNotEquals(B(42), B(2), '')

        assertNotEquals(b(42), b(2), supplier)
        assertNotEquals(b(42), B(2), supplier)
        assertNotEquals(B(42), b(2), supplier)
        assertNotEquals(B(42), B(2), supplier)
    }

    @Test
    void "doubles can be passed to assertNotEquals"() {
        assertNotEquals(d(42), d(2))
        assertNotEquals(d(42), D(2))
        assertNotEquals(D(42), d(2))
        assertNotEquals(D(42), D(2))

        assertNotEquals(d(42), d(2), '')
        assertNotEquals(d(42), D(2), '')
        assertNotEquals(D(42), d(2), '')
        assertNotEquals(D(42), D(2), '')

        assertNotEquals(d(42), d(2), supplier)
        assertNotEquals(d(42), D(2), supplier)
        assertNotEquals(D(42), d(2), supplier)
        assertNotEquals(D(42), D(2), supplier)
    }

    @Test
    void "doubles can be passed to assertNotEquals with delta"() {
        assertNotEquals(d(42), d(2), 0.01d)
        assertNotEquals(d(42), D(2), 0.01d)
        assertNotEquals(D(42), d(2), 0.01d)
        assertNotEquals(D(42), D(2), 0.01d)

        assertNotEquals(d(42), d(2), 0.01d, '')
        assertNotEquals(d(42), D(2), 0.01d, '')
        assertNotEquals(D(42), d(2), 0.01d, '')
        assertNotEquals(D(42), D(2), 0.01d, '')

        assertNotEquals(d(42), d(2), 0.01d, supplier)
        assertNotEquals(d(42), D(2), 0.01d, supplier)
        assertNotEquals(D(42), d(2), 0.01d, supplier)
        assertNotEquals(D(42), D(2), 0.01d, supplier)
    }

    @Test
    void "chars can be passed to assertNotEquals"() {
        assertNotEquals(c(42), c(2))
        assertNotEquals(c(42), C(2))
        assertNotEquals(C(42), c(2))
        assertNotEquals(C(42), C(2))

        assertNotEquals(c(42), c(2), '')
        assertNotEquals(c(42), C(2), '')
        assertNotEquals(C(42), c(2), '')
        assertNotEquals(C(42), C(2), '')

        assertNotEquals(c(42), c(2), supplier)
        assertNotEquals(c(42), C(2), supplier)
        assertNotEquals(C(42), c(2), supplier)
        assertNotEquals(C(42), C(2), supplier)
    }

    @Test
    void "longs can be passed to assertNotEquals"() {
        assertNotEquals(l(42), l(2))
        assertNotEquals(l(42), L(2))
        assertNotEquals(L(42), l(2))
        assertNotEquals(L(42), L(2))

        assertNotEquals(l(42), l(2), '')
        assertNotEquals(l(42), L(2), '')
        assertNotEquals(L(42), l(2), '')
        assertNotEquals(L(42), L(2), '')

        assertNotEquals(l(42), l(2), supplier)
        assertNotEquals(l(42), L(2), supplier)
        assertNotEquals(L(42), l(2), supplier)
        assertNotEquals(L(42), L(2), supplier)
    }

    @Test
    void "shorts can be passed to assertNotEquals"() {
        assertNotEquals(s(42), s(2))
        assertNotEquals(s(42), S(2))
        assertNotEquals(S(42), s(2))
        assertNotEquals(S(42), S(2))

        assertNotEquals(s(42), s(2), '')
        assertNotEquals(s(42), S(2), '')
        assertNotEquals(S(42), s(2), '')
        assertNotEquals(S(42), S(2), '')

        assertNotEquals(s(42), s(2), supplier)
        assertNotEquals(s(42), S(2), supplier)
        assertNotEquals(S(42), s(2), supplier)
        assertNotEquals(S(42), S(2), supplier)
    }

}
