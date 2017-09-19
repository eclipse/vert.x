/*
 * Copyright (c) 2011-2017 The original author or authors
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 and the Apache License, Version 2.0
 * which accompanies this distribution. The Eclipse Public License 2.0 is
 * available at http://www.eclipse.org/legal/epl-2.0.html, and the Apache
 * License, Version 2.0 is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.core.cli.impl;

import io.vertx.core.cli.*;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Option;
import io.vertx.core.cli.annotations.Summary;

@Summary("A command saying hello.")
@Description("A simple cli to wish you a good day. Pass your name with `--name`")
@Name("hello")
public class HelloClI {

  public static boolean called = false;
  public String name;

  @Option(longName = "name", shortName = "n", required = true, argName = "name")
  @Description("your name")
  public void setTheName(String name) {
    this.name = name;
  }

  public String run() throws CLIException {
    called = true;
    return "Hello " + name;
  }
}
