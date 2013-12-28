/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package org.vertx.java.core.file.impl;

import org.vertx.java.core.file.FileProps;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;


public class DefaultFileProps implements FileProps {

  private final long creationTime;
  private final long lastAccessTime;
  private final long lastModifiedTime;
  private final boolean isDirectory;
  private final boolean isOther;
  private final boolean isRegularFile;
  private final boolean isSymbolicLink;
  private final long size;

  public DefaultFileProps(BasicFileAttributes attrs) {
    creationTime = attrs.creationTime().toMillis();
    lastModifiedTime = attrs.lastModifiedTime().toMillis();
    lastAccessTime = attrs.lastAccessTime().toMillis();
    isDirectory = attrs.isDirectory();
    isOther = attrs.isOther();
    isRegularFile = attrs.isRegularFile();
    isSymbolicLink = attrs.isSymbolicLink();
    size = attrs.size();
  }

  @Override
  public Date creationTime() {
    return new Date(creationTime);
  }

  @Override
  public Date lastAccessTime() {
    return new Date(lastAccessTime);
  }

  @Override
  public Date lastModifiedTime() {
    return new Date(lastModifiedTime);
  }

  @Override
  public boolean isDirectory() {
    return isDirectory;
  }

  @Override
  public boolean isOther() {
    return isOther;
  }

  @Override
  public boolean isRegularFile() {
    return isRegularFile;
  }

  @Override
  public boolean isSymbolicLink() {
    return isSymbolicLink;
  }

  @Override
  public long size() {
    return size;
  }
}
