/*
 * Copyright (c) 2011-2017 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     https://www.eclipse.org/legal/epl-2.0/
 *
 *     The Apache License v2.0 is available at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.test.core;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.test.core.tls.Cert;
import io.vertx.test.core.tls.Trust;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Http2TLSTest extends HttpTLSTest {

  @Override
  HttpServer createHttpServer(HttpServerOptions options) {
    return vertx.createHttpServer(options.setUseAlpn(true));
  }

  @Override
  HttpClient createHttpClient(HttpClientOptions options) {
    return vertx.createHttpClient(options.setUseAlpn(true));
  }

  @Override
  protected TLSTest testTLS(Cert<?> clientCert, Trust<?> clientTrust, Cert<?> serverCert, Trust<?> serverTrust) throws Exception {
    return super.testTLS(clientCert, clientTrust, serverCert, serverTrust).version(HttpVersion.HTTP_2);
  }
}
