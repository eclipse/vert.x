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
package io.vertx.core.cli.converters;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class CustomConverterTest {

  @Test
  public void testCreation() {
    assertThat(new Person4Converter().fromString("bob, morane").first).isEqualToIgnoringCase("bob");
    assertThat(new Person4Converter().fromString("bob, morane").last).isEqualToIgnoringCase("morane");
  }

  @Test
  public void testConvertion() {
    Person4 p4 = Converters.create("bob, morane", new Person4Converter());
    assertThat(p4.first).isEqualTo("bob");
    assertThat(p4.last).isEqualTo("morane");
  }

}
