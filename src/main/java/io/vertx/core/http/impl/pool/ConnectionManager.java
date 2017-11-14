/*
 * Copyright (c) 2011-2014 The original author or authors
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

package io.vertx.core.http.impl.pool;

import io.netty.channel.Channel;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpClientMetrics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ConnectionManager<C> {

  public static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

  private final int maxWaitQueueSize;
  private final HttpClientMetrics metrics; // Shall be removed later combining the PoolMetrics with HttpClientMetrics
  private final ConnectionProvider<C> connector;
  private final Function<SocketAddress, PoolOptions> optionsProvider;
  private final Map<Channel, C> connectionMap = new ConcurrentHashMap<>();
  private final Map<EndpointKey, Pool<C>> endpointMap = new ConcurrentHashMap<>();

  public ConnectionManager(HttpClientMetrics metrics,
                           ConnectionProvider<C> connector,
                           Function<SocketAddress, PoolOptions> optionsProvider,
                           int maxWaitQueueSize) {
    this.maxWaitQueueSize = maxWaitQueueSize;
    this.metrics = metrics;
    this.connector = connector;
    this.optionsProvider = optionsProvider;
  }

  private static final class EndpointKey {

    private final boolean ssl;
    private final int port;
    private final String host;

    EndpointKey(boolean ssl, int port, String host) {
      this.ssl = ssl;
      this.host = host;
      this.port = port;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      EndpointKey that = (EndpointKey) o;

      if (ssl != that.ssl) return false;
      if (port != that.port) return false;
      if (!Objects.equals(host, that.host)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = ssl ? 1 : 0;
      result = 31 * result + (host != null ? host.hashCode() : 0);
      result = 31 * result + port;
      return result;
    }
  }

  private Pool<C> getConnQueue(String peerHost, boolean ssl, int port, String host) {
    EndpointKey key = new EndpointKey(ssl, port, peerHost);
    PoolOptions options = optionsProvider.apply(SocketAddress.inetSocketAddress(port, host));
    return endpointMap.computeIfAbsent(key, targetAddress -> new Pool<>(
      connector,
      metrics,
      maxWaitQueueSize,
      peerHost,
      host,
      port,
      ssl,
      options.getMaxSize(), v -> endpointMap.remove(key),
      holder -> connectionMap.put(holder.channel, holder.connection),
      holder -> connectionMap.remove(holder.channel)));
  }

  public void getConnection(String peerHost, boolean ssl, int port, String host, Waiter<C> waiter) {
    while (true) {
      Pool<C> connQueue = getConnQueue(peerHost, ssl, port, host);
      if (connQueue.getConnection(waiter)) {
        break;
      }
    }
  }

  public void close() {
    endpointMap.clear();
    for (C conn : connectionMap.values()) {
      connector.close(conn);
    }
  }
}