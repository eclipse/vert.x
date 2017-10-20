/*
 * Copyright (c) 2011-2017 The original author or authors
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 and the Apache License, Version 2.0
 * which accompanies this distribution. The Eclipse Public License 2.0 is
 * available at http://www.eclipse.org/legal/epl-2.0.html, and the Apache
 * License, Version 2.0 is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.core.http;

import io.vertx.core.VertxException;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebsocketRejectedException extends VertxException {

  private final int status;

  public WebsocketRejectedException(int status) {
    super("Websocket connection attempt returned HTTP status code " + status);
    this.status = status;
  }

  /**
   * @return the status code of the response that rejected the upgrade
   */
  public int getStatus() {
    return status;
  }
}
