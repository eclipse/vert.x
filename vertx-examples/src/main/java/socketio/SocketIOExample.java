/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.socketio.impl.*;
import org.vertx.java.core.socketio.*;
import org.vertx.java.deploy.Verticle;

/**
 * @author Keesun Baik
 */
public class SocketIOExample extends Verticle {

	public void start() {
		HttpServer server = vertx.createHttpServer();

		server.requestHandler(new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				if (req.path.equals("/")) req.response.sendFile("socketio/index.html"); // Serve the html
				if (req.path.equals("/socketio.js")) req.response.sendFile("socketio/socketio.js"); // Serve the html
			}
		});

		final SocketIOServer io = new DefaultSocketIOServer(vertx, server);
//		final SocketIOServer io = vertx.createSocketIOServer(server);

		io.configure("development", new Configurer() {
			public void configure(JsonObject config) {
				config.putString("transports", "jsonp-polling"); // Now, Only this transport is implmented.
			}
		});

		io.sockets().onConnect(new Handler<SocketIOSocket>() {
			public void handle(final SocketIOSocket sock) {
				sock.on("data", new Handler<JsonObject>() {
					public void handle(JsonObject data) {
						sock.emit("data", data); // Echo it back
					}
				});
			}
		});

		server.listen(8081);
	}
}
