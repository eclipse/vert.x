/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.core;

/**
 * This class can be used for simple handlers which don't receive any value.
 */
public abstract class VoidHandler implements Handler<Void> {

  public final void handle(Void event) {
    handle();
  }

  /**
   * Handle the event. It should be overridden by the user.
   */
  protected abstract void handle();
}
