/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.tagexpression;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.errorprone.annotations.Var;

/**
 * @since 1.1
 */
class Tokenizer {

	List<Token> tokenize(String infixTagExpression) {
		if (infixTagExpression == null) {
			return emptyList();
		}

		return deriveTokensFrom(infixTagExpression, trimmedTokenStringsFrom(infixTagExpression));
	}

	private List<String> trimmedTokenStringsFrom(String infixTagExpression) {
		return Pattern.compile("\\s").splitAsStream(infixTagExpression.replaceAll("([()!|&])", " $1 ")).filter(
			part -> !part.isEmpty()).collect(toList());
	}

	private List<Token> deriveTokensFrom(String infixTagExpression, List<String> trimmedTokens) {
		@Var
		int startIndex = 0;
		List<Token> tokens = new ArrayList<>(trimmedTokens.size());
		for (String trimmedToken : trimmedTokens) {
			Token token = extractTokenStartingAt(infixTagExpression, startIndex, trimmedToken);
			startIndex = token.endIndexExclusive();
			tokens.add(token);
		}
		return tokens;
	}

	private Token extractTokenStartingAt(String infixTagExpression, int startIndex, String trimmedToken) {
		int foundAt = infixTagExpression.indexOf(trimmedToken, startIndex);
		int endIndex = foundAt + trimmedToken.length();
		String rawToken = infixTagExpression.substring(startIndex, endIndex);
		return new Token(startIndex, rawToken);
	}

}
