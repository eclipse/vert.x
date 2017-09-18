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

package io.vertx.test.core;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Thomas Segismont
 */
public class NestedRootJarResolverTest extends FileResolverTestBase {

  private ClassLoader prevCL;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    // This folder is inside the nested-inf/classes directory, inside nestedroot.jar
    webRoot = "webroot2";

    prevCL = Thread.currentThread().getContextClassLoader();
    URL jarUrl = prevCL.getResource("nestedroot.jar");
    URL rootUrl = new URL("jar:" + jarUrl + "!/nested-inf/classes!/");
    URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{rootUrl}, prevCL);
    Thread.currentThread().setContextClassLoader(urlClassLoader);
  }

  @Override
  public void after() throws Exception {
    if (prevCL != null) {
      Thread.currentThread().setContextClassLoader(prevCL);
    }
    super.after();
  }
}
