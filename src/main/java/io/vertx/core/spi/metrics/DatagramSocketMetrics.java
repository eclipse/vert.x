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

package io.vertx.core.spi.metrics;

import io.vertx.core.net.SocketAddress;

/**
 * The datagram/udp metrics SPI which Vert.x will use to call when each event occurs pertaining to datagram sockets.<p/>
 *
 * The thread model for the datagram socket depends on the actual context thats started the server.<p/>
 *
 * <h3>Event loop context</h3>
 *
 * Unless specified otherwise, all the methods on this object including the methods inherited from the super interfaces are invoked
 * with the thread of the http server and therefore are the same than the
 * {@link io.vertx.core.spi.metrics.VertxMetrics} {@code createMetrics} method that created and returned
 * this metrics object.
 *
 * <h3>Worker context</h3>
 *
 * Unless specified otherwise, all the methods on this object including the methods inherited from the super interfaces are invoked
 * with a worker thread.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface DatagramSocketMetrics extends NetworkMetrics<Void> {

  /**
   * Called when a socket is listening. For example, this is called when an http or net server
   * has been created and is listening on a specific host/port.
   *
   * @param localName
   * @param localAddress the local address the net socket is listening on.
   */
  void listening(String localName, SocketAddress localAddress);

}
