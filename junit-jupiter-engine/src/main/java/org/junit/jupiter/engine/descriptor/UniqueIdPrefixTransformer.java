/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.UniqueId;

/**
 * @since 5.13
 */
class UniqueIdPrefixTransformer implements UnaryOperator<UniqueId> {

	private final UniqueId oldPrefix;
	private final UniqueId newPrefix;
	private final int oldPrefixLength;

	UniqueIdPrefixTransformer(UniqueId oldPrefix, UniqueId newPrefix) {
		this.oldPrefix = oldPrefix;
		this.newPrefix = newPrefix;
		this.oldPrefixLength = oldPrefix.getSegments().size();
	}

	@Override
	public UniqueId apply(UniqueId uniqueId) {
		Preconditions.condition(uniqueId.hasPrefix(oldPrefix),
			() -> String.format("Unique ID %s does not have the expected prefix %s", uniqueId, oldPrefix));
		List<UniqueId.Segment> oldSegments = uniqueId.getSegments();
		List<UniqueId.Segment> suffix = oldSegments.subList(oldPrefixLength, oldSegments.size());
		UniqueId newValue = newPrefix;
		for (UniqueId.Segment segment : suffix) {
			newValue = newValue.append(segment);
		}
		return newValue;
	}
}
