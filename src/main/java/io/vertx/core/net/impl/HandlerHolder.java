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

package io.vertx.core.net.impl;

import io.vertx.core.impl.ContextImpl;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HandlerHolder<T> {
  public final ContextImpl context;
  public final T handler;

  public HandlerHolder(ContextImpl context, T handler) {
    this.context = context;
    this.handler = handler;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HandlerHolder that = (HandlerHolder) o;

    if (context != that.context) return false;
    if (handler != null ? !handler.equals(that.handler) : that.handler != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = context.hashCode();
    result = 31 * result + handler.hashCode();
    return result;
  }
}
