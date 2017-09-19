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
package io.vertx.core.cli.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a setter to be called with the value of a command line argument.
 *
 * @author Clement Escoffier <clement@apache.org>
 * @see Option
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {

  /**
   * The name of this argument (used in doc)
   */
  String argName() default "value";

  /**
   * The (0-based) position of this argument relative to the argument list. The first parameter has the index 0,
   * the second 1...
   * <p/>
   * Index is mandatory to force you to think to the order.
   */
  int index();

  /**
   * Whether or not the argument is required. An argument is required by default.
   */
  boolean required() default true;
}
