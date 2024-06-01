/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.extensions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

/**
 * Demonstrates an implementation of {@link CloseableResource} using an {@link HttpServer}.
 */
// tag::user_guide[]
public class HttpServerResource implements CloseableResource {
	private final HttpServer httpServer;

	// end::user_guide[]
	/**
	 * Initializes the Http server resource, using the given port.
	 *
	 * @param port (int) The port number for the server, must be in the range 0-65535.
	 * @throws IOException if an IOException occurs during initialization.
	 */
	// tag::user_guide[]
	public HttpServerResource(int port) throws IOException {
		this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
	}

	// end::user_guide[]

	/**
	 * Starts the Http server with an example handler.
	 */
	// tag::user_guide[]
	public void start() {
		//Example handler
		httpServer.createContext("/example", exchange -> {
			String test = "This is a test.";
			exchange.sendResponseHeaders(200, test.length());
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(test.getBytes());
			}
		});
		httpServer.setExecutor(null);
		httpServer.start();
	}

	@Override
	public void close() throws Throwable {
		httpServer.stop(0);
	}
}
// end::user_guide[]
