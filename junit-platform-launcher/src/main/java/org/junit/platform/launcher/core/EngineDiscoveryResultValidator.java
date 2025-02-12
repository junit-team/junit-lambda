/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static java.util.stream.Collectors.joining;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;

/**
 * Perform common validation checks on the result from the `discover()` method.
 *
 * @since 1.3
 */
class EngineDiscoveryResultValidator {

	/**
	 * Perform common validation checks.
	 *
	 * @throws org.junit.platform.commons.PreconditionViolationException if any check fails
	 */
	void validate(TestEngine testEngine, TestDescriptor root) {
		Preconditions.notNull(root,
			() -> String.format(
				"The discover() method for TestEngine with ID '%s' must return a non-null root TestDescriptor.",
				testEngine.getId()));
		Optional<String> cyclicGraphInfo = getCyclicGraphInfo(root);
		Preconditions.condition(!cyclicGraphInfo.isPresent(),
			() -> String.format("The discover() method for TestEngine with ID '%s' returned a cyclic graph; %s",
				testEngine.getId(), cyclicGraphInfo.get()));
	}

	/**
	 * @return non-empty {@link Optional} if the tree contains a cycle
	 */
	private Optional<String> getCyclicGraphInfo(TestDescriptor root) {

		Map<UniqueId, Optional<UniqueId>> visited = new HashMap<>();
		visited.put(root.getUniqueId(), Optional.empty());

		Queue<TestDescriptor> queue = new ArrayDeque<>();
		queue.add(root);

		while (!queue.isEmpty()) {
			TestDescriptor parent = queue.remove();
			for (TestDescriptor child : parent.getChildren()) {
				UniqueId uid = child.getUniqueId();
				if (visited.containsKey(uid)) { // id already known: cycle detected!

					List<UniqueId> path1 = findPath(visited, uid);
					List<UniqueId> path2 = findPath(visited, parent.getUniqueId());
					path2.add(uid);

					return Optional.of(String.format("%s exists in at least two paths:\n(1) %s\n(2) %s", uid,
						formatted(path1), formatted(path2)));
				}
				else {
					visited.put(uid, Optional.of(parent.getUniqueId()));
				}
				if (child.isContainer()) {
					queue.add(child);
				}
			}
		}
		return Optional.empty();
	}

	private String formatted(List<UniqueId> path) {
		return path.stream().map(UniqueId::toString).collect(joining(" -> "));
	}

	private static List<UniqueId> findPath(Map<UniqueId, Optional<UniqueId>> visited, UniqueId target) {
		List<UniqueId> path = new ArrayList<>();
		path.add(target);
		UniqueId current = target;

		while (visited.containsKey(current)) {
			Optional<UniqueId> backTraced = visited.get(current);
			if (backTraced.isPresent()) {
				path.add(0, backTraced.get());
				current = backTraced.get();
			}
			else {
				break;
			}
		}
		return path;
	}

}
