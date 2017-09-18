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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.function.Function;

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public abstract class VertxNetHandler extends VertxHandler<NetSocketImpl> {

  private final Function<ChannelHandlerContext, NetSocketImpl> connectionFactory;

  public VertxNetHandler(Function<ChannelHandlerContext, NetSocketImpl> connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  public VertxNetHandler(NetSocketImpl conn) {
    this(ctx -> conn);
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    setConnection(connectionFactory.apply(ctx));
  }

  @Override
  protected Object decode(Object msg, ByteBufAllocator allocator) throws Exception {
    return msg;
  }
}
