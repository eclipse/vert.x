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

package io.vertx.core.buffer.impl;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.spi.BufferFactory;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BufferFactoryImpl implements BufferFactory {

  @Override
  public Buffer buffer(int initialSizeHint) {
    return new BufferImpl(initialSizeHint);
  }

  @Override
  public Buffer buffer() {
    return new BufferImpl();
  }

  @Override
  public Buffer buffer(String str) {
    return new BufferImpl(str);
  }

  @Override
  public Buffer buffer(String str, String enc) {
    return new BufferImpl(str, enc);
  }

  @Override
  public Buffer buffer(byte[] bytes) {
    return new BufferImpl(bytes);
  }

  @Override
  public Buffer buffer(ByteBuf byteBuffer) {
    return new BufferImpl(byteBuffer);
  }
}
