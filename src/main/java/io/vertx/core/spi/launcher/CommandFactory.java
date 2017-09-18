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

package io.vertx.core.spi.launcher;

import io.vertx.core.Launcher;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;

/**
 * SPI Interface to provide a new {@link Launcher} command. Implementors needs to provide two methods:
 * <ol>
 * <li>{@link #define()} - creates a {@link CLI} instance (so the model)</li>
 * <li>{@link #create(CommandLine)}} - creates a new command instance</li>
 * </ol>
 *
 * @author Clement Escoffier <clement@apache.org>
 */
public interface CommandFactory<C extends Command> {

  /**
   * @return a new instance of the command.
   */
  C create(CommandLine evaluated);

  /**
   * Creates a new {@link CLI} instance.
   *
   * @return the CLI.
   */
  CLI define();


}
