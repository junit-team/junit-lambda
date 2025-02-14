/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.params.aggregator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Month

/**
 * Tests for ParameterizedTest kotlin compatibility
 */
object KotlinParameterizedTests {
    @ParameterizedTest
    @MethodSource("dataProvidedByKotlinSequence")
    fun `a method source can be supplied by a Sequence returning method`(
        value: Int,
        month: Month
    ) {
        assertEquals(value, month.value)
    }

    @JvmStatic
    private fun dataProvidedByKotlinSequence() =
        sequenceOf(
            arguments(1, Month.JANUARY),
            arguments(3, Month.MARCH),
            arguments(8, Month.AUGUST),
            arguments(5, Month.MAY),
            arguments(12, Month.DECEMBER)
        )
}
