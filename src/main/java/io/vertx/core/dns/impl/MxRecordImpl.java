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
package io.vertx.core.dns.impl;

import io.vertx.core.dns.MxRecord;

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public final class MxRecordImpl implements MxRecord, Comparable<MxRecord> {

  private final int priority;
  private final String name;

  public MxRecordImpl(int priority, String name) {
    this.priority = priority;
    this.name = name;
  }

  @Override
  public int priority() {
    return priority;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return priority() + " " + name();
  }

  @Override
  public int compareTo(MxRecord o) {
    return Integer.valueOf(priority()).compareTo(o.priority());
  }
}
