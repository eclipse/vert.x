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

package io.vertx.test.codegen;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class ParentDataObject {

  private String parentProperty;

  public ParentDataObject() {
  }

  public ParentDataObject(ParentDataObject copy) {
  }

  public ParentDataObject(JsonObject json) {
  }

  public String getParentProperty() {
    return parentProperty;
  }

  public ParentDataObject setParentProperty(String parentProperty) {
    this.parentProperty = parentProperty;
    return this;
  }
}
