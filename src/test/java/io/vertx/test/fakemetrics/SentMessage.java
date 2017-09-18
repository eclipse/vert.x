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

package io.vertx.test.fakemetrics;

/**
* @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
*/
public class SentMessage {

  public final String address;
  public final boolean publish;
  public final boolean local;
  public final boolean remote;

  public SentMessage(String address, boolean publish, boolean local, boolean remote) {
    this.address = address;
    this.publish = publish;
    this.local = local;
    this.remote = remote;
  }

  @Override
  public boolean equals(Object obj) {
    SentMessage that = (SentMessage) obj;
    return address.equals(that.address) && publish == that.publish && local == that.local && remote == that.remote;
  }

  @Override
  public String toString() {
    return "Message[address=" + address + ",publish=" + publish + ",local=" + local + ",remote=" + remote + "]";
  }
}
