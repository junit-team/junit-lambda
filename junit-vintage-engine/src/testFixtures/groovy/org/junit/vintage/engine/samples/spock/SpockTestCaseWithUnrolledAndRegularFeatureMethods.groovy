/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.vintage.engine.samples.spock

import spock.lang.Specification
import spock.lang.Unroll

class SpockTestCaseWithUnrolledAndRegularFeatureMethods extends Specification {

    @Unroll
    def "unrolled feature for #input"() {
        expect:
        input == 42
        where:
        input << [23, 42]
    }

    def "regular"() {
        expect:
        true
    }
}
