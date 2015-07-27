/*
 *  Copyright (c) 2011-2013 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.core.cli.commands;

import io.vertx.core.cli.*;

@Summary("A command saying hello.")
@Description("A simple command to wish you a good day. Pass your name with `--name`")
public class HelloCommand extends DefaultCommand {

  public static boolean called = false;

  public String name;


  @Option(longName = "name", shortName = "n", required = true, name = "name")
  @Description("your name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the command name such as 'run'.
   */
  @Override
  public String name() {
    return "hello";
  }
  /**
   * Executes the command.
   *
   * @throws CommandLineException If anything went wrong.
   */
  @Override
  public void run() throws CommandLineException {
    called = true;
    out.println("Hello " + name);
  }
}
