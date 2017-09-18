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
package io.vertx.core.net;

import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.KeyStoreHelper;

import javax.net.ssl.TrustManagerFactory;

/**
 * Certification authority configuration options.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface TrustOptions {

  /**
   * @return a copy of these options
   */
  TrustOptions clone();

  /**
   * Create and return the trust manager factory for these options.
   * <p>
   * The returned trust manager factory should be already initialized and ready to use.
   *
   * @param vertx the vertx instance
   * @return the trust manager factory
   */
  default TrustManagerFactory getTrustManagerFactory(Vertx vertx) throws Exception {
    return KeyStoreHelper.create((VertxInternal) vertx, this).getTrustMgrFactory((VertxInternal) vertx);
  }
}
