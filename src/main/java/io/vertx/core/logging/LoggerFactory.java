/*
 * Copyright (c) 2009 Red Hat, Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.core.logging;

import static java.util.Objects.requireNonNull;

import io.vertx.core.spi.logging.LogDelegate;
import io.vertx.core.spi.logging.LogDelegateFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @deprecated see https://github.com/eclipse-vertx/vert.x/issues/2774
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class LoggerFactory {

  public static final String LOGGER_DELEGATE_FACTORY_CLASS_NAME = "vertx.logger-delegate-factory-class-name";

  private static volatile LogDelegateFactory delegateFactory;

  private static final ConcurrentMap<String, Logger> loggers = new ConcurrentHashMap<>();

  static {
    initialise();
  }

  public static synchronized void initialise() {
    LogDelegateFactory newDelegateFactory;

    // If a system property is specified then this overrides any delegate factory which is set
    // programmatically - this is primarily of use so we can configure the logger delegate on the client side.
    // call to System.getProperty is wrapped in a try block as it will fail if the client runs in a secured
    // environment
    String className = JULLogDelegateFactory.class.getName();
    try {
      className = System.getProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME);
    } catch (Exception e) {
    }

    if (className != null) {
      ClassLoader tcccl = Thread.currentThread().getContextClassLoader();
      try {
        newDelegateFactory = loadClass(className, tcccl);
      } catch (IllegalArgumentException e) {
        ClassLoader thisClasssLoader = LoggerFactory.class.getClassLoader();
        newDelegateFactory = loadClass(className, thisClasssLoader);
      }
    } else {
      newDelegateFactory = new JULLogDelegateFactory();
    }

    LoggerFactory.delegateFactory = newDelegateFactory;
  }

  private static LogDelegateFactory loadClass(String className, ClassLoader classLoader) {
    try {
      Class<?> clz = classLoader.loadClass(className);
      return (LogDelegateFactory) clz.newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("Error instantiating LogDelegateFactory implemention: \"" + className + "\"", e);
    }
  }

  /**
   * Set new custom LoggerFactory.
   * @param factory the new LogDelegateFactory to use
   */
  public static synchronized void setLogDelegateFactory(LogDelegateFactory factory) {
      requireNonNull(factory, "factory == null");
      LogDelegateFactory current = delegateFactory;
      if (current != null) {
          try {
            getLogger(LoggerFactory.class).debug("Replacing LogDelegateFactory " + current + " by " + factory);
          } catch (Throwable t) {
            factory.createDelegate(LoggerFactory.class.getCanonicalName())
                .error("Failed to log LogDelegateFactory replacement on previous implementation", t);
          }
          loggers.clear();
      }
      delegateFactory = factory;
  }

  /**
   * Get current LoggerFactory.
   * @return the currently used LogDelegateFactory
   */
  public static LogDelegateFactory getLogDelegateFactory() {
      return delegateFactory;
  }

  /**
   * @deprecated see https://github.com/eclipse-vertx/vert.x/issues/2774
   */
  @Deprecated
  public static Logger getLogger(final Class<?> clazz) {
    String name = clazz.isAnonymousClass() ?
      clazz.getEnclosingClass().getCanonicalName() :
      clazz.getCanonicalName();
    return getLogger(name);
  }

  /**
   * @deprecated see https://github.com/eclipse-vertx/vert.x/issues/2774
   */
  @Deprecated
  public static Logger getLogger(final String name) {
    Logger logger = loggers.get(name);

    if (logger == null) {
      LogDelegate delegate = delegateFactory.createDelegate(name);

      logger = new Logger(delegate);

      Logger oldLogger = loggers.putIfAbsent(name, logger);

      if (oldLogger != null) {
        logger = oldLogger;
      }
    }

    return logger;
  }

  /**
   * @deprecated see https://github.com/eclipse-vertx/vert.x/issues/2774
   */
  @Deprecated
  public static void removeLogger(String name) {
    loggers.remove(name);
  }
}
