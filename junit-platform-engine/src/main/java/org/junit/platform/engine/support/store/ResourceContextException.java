/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.store;

public class ResourceContextException extends RuntimeException {
	public ResourceContextException(String message) {
		super(message);
	}

	public ResourceContextException(String message, Throwable cause) {
		super(message, cause);
	}

}
