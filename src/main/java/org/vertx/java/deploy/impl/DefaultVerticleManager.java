/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.java.deploy.impl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.SimpleHandler;
import org.vertx.java.core.VertxInternal;
import org.vertx.java.core.impl.Context;
import org.vertx.java.core.impl.DeploymentHandle;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.deploy.Container;
import org.vertx.java.deploy.Verticle;
import org.vertx.java.deploy.VerticleFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class DefaultVerticleManager implements VerticleManager {

  private static final Logger log = LoggerFactory.getLogger(DefaultVerticleManager.class);

  private final VertxInternal vertx;
  // deployment name --> deployment
  private final Map<String, Deployment> deployments = new HashMap<>();
  // The out of the box busmods dirs
  private final File systemModRoot;
  // The user mods dir
  private final File userModRoot;

  private CountDownLatch stopLatch = new CountDownLatch(1);
  
  private Map<String, VerticleFactory> factories;

  public DefaultVerticleManager(VertxInternal vertx) {
    this.vertx = vertx;
    VertxLocator.vertx = vertx;
    VertxLocator.container = new Container(this);
    String installDir = System.getProperty("vertx.install");
    if (installDir == null) {
      installDir = ".";
    }
    systemModRoot = new File(installDir, "mods");
    String modDir = System.getProperty("vertx.mods");
    if (modDir != null && !modDir.trim().equals("")) {
      userModRoot = new File(modDir);
      if (!userModRoot.exists()) {
        throw new IllegalStateException("Module directory " + userModRoot + " does not exist");
      }
    } else {
      userModRoot = null;
    }

    this.factories = new HashMap<String, VerticleFactory>();

    // Find and load VerticleFactories
    Iterable<VerticleFactory> services = VerticleFactory.factories;
    for (VerticleFactory vf : services) {
      factories.put(vf.getLanguage(), vf);
    }
  }

  /* (non-Javadoc)
 * @see org.vertx.java.deploy.impl.VerticleManager#block()
 */
@Override
public void block() {
    while (true) {
      try {
        stopLatch.await();
        break;
      } catch (InterruptedException e) {
        //Ignore
      }
    }
  }

  /* (non-Javadoc)
 * @see org.vertx.java.deploy.impl.VerticleManager#unblock()
 */
@Override
public void unblock() {
    stopLatch.countDown();
  }

  /* (non-Javadoc)
 * @see org.vertx.java.deploy.impl.VerticleManager#getConfig()
 */
@Override
public JsonObject getConfig() {
    VerticleHolder holder = getVerticleHolder();
    return holder == null ? null : holder.config;
  }

  /* (non-Javadoc)
 * @see org.vertx.java.deploy.impl.VerticleManager#getDeploymentName()
 */
@Override
public String getDeploymentName() {
    VerticleHolder holder = getVerticleHolder();
    return holder == null ? null : holder.deployment.name;
  }

  /* (non-Javadoc)
 * @see org.vertx.java.deploy.impl.VerticleManager#getDeploymentURLs()
 */
@Override
public URL[] getDeploymentURLs() {
    VerticleHolder holder = getVerticleHolder();
    return holder == null ? null : holder.deployment.urls;
  }

  /* (non-Javadoc)
 * @see org.vertx.java.deploy.impl.VerticleManager#getDeploymentModDir()
 */
@Override
public File getDeploymentModDir() {
    VerticleHolder holder = getVerticleHolder();
    return holder == null ? null : holder.deployment.modDir;
  }

  /* (non-Javadoc)
 * @see org.vertx.java.deploy.impl.VerticleManager#getLogger()
 */
@Override
public Logger getLogger() {
    VerticleHolder holder = getVerticleHolder();
    return holder == null ? null : holder.logger;
  }

  /* (non-Javadoc)
 * @see org.vertx.java.deploy.impl.VerticleManager#deploy(boolean, java.lang.String, java.lang.String, org.vertx.java.core.json.JsonObject, java.net.URL[], int, java.io.File, org.vertx.java.core.Handler)
 */
@Override
public synchronized String deploy(boolean worker, String name, final String main,
                                    final JsonObject config, final URL[] urls,
                                    int instances, File currentModDir,
                                    final Handler<Void> doneHandler) {
  
    if (deployments.containsKey(name)) {
      throw new IllegalStateException("There is already a deployment with name: " + name);
    }

    // We first check if there is a module with the name, if so we deploy that

    String deployID = deployMod(name, main, config, instances, currentModDir, doneHandler);
    if (deployID == null) {
      if (urls == null) {
        throw new IllegalStateException("urls cannot be null");
      }
      return doDeploy(worker, name, main, config, urls, instances, currentModDir, doneHandler);
    } else {
      return deployID;
    }
  }

  /* (non-Javadoc)
 * @see org.vertx.java.deploy.impl.VerticleManager#undeployAll(org.vertx.java.core.Handler)
 */
@Override
public synchronized void undeployAll(final Handler<Void> doneHandler) {
    final UndeployCount count = new UndeployCount();
    if (!deployments.isEmpty()) {
      // We do it this way since undeploy is itself recursive - we don't want
      // to attempt to undeploy the same verticle twice if it's a child of
      // another
      while (!deployments.isEmpty()) {
        String name = deployments.keySet().iterator().next();
        count.incRequired();
        undeploy(name, new SimpleHandler() {
          public void handle() {
            count.undeployed();
          }
        });
      }
    }
    count.setHandler(doneHandler);
  }

  /* (non-Javadoc)
 * @see org.vertx.java.deploy.impl.VerticleManager#undeploy(java.lang.String, org.vertx.java.core.Handler)
 */
@Override
public synchronized void undeploy(String name, final Handler<Void> doneHandler) {
    if (deployments.get(name) == null) {
      throw new IllegalArgumentException("There is no deployment with name " + name);
    }
    doUndeploy(name, doneHandler);
  }

  /* (non-Javadoc)
 * @see org.vertx.java.deploy.impl.VerticleManager#listInstances()
 */
@Override
public synchronized Map<String, Integer> listInstances() {
    Map<String, Integer> map = new HashMap<>();
    for (Map.Entry<String, Deployment> entry: deployments.entrySet()) {
      map.put(entry.getKey(), entry.getValue().verticles.size());
    }
    return map;
  }

  // We calculate a path adjustment that can be used by the fileSystem object
  // so that the *effective* working directory can be the module directory
  // this allows modules to read and write the file system as if they were
  // in the module dir, even though the actual working directory will be
  // wherever vertx run or vertx start was called from
  private void setPathAdjustment(File modDir) {
    Path cwd = Paths.get(".").toAbsolutePath().getParent();
    Path pmodDir = Paths.get(modDir.getAbsolutePath());
    Path relative = cwd.relativize(pmodDir);
    Context.getContext().setPathAdjustment(relative);
  }

  private String doDeploy(boolean worker, String name, final String main,
                          final JsonObject config, final URL[] urls,
                          int instances,
                          final File modDir,
                          final Handler<Void> doneHandler)
  {

    // Infer the main type
	String language = "java";
	LOOP: for (VerticleFactory vf : factories.values()) {
		if (vf.isFactoryFor(main)) {
			language = vf.getLanguage();
			break LOOP;
		}
	}

    final String deploymentName = name == null ?  "deployment-" + UUID.randomUUID().toString() : name;

    log.debug("Deploying name : " + deploymentName  + " main: " + main +
                 " instances: " + instances);

    if (!factories.containsKey(language))
    	throw new IllegalArgumentException("Unsupported language: " + language);

    final VerticleFactory verticleFactory = factories.get(language);
    verticleFactory.init(this);

    final int instCount = instances;

    class AggHandler {
      AtomicInteger count = new AtomicInteger(0);

      // We need a context on which to execute the done Handler
      // We use the current calling context (if any) or assign a new one
      Context doneContext = vertx.getOrAssignContext();

      void started() {
        if (count.incrementAndGet() == instCount) {
          if (doneHandler != null) {
            doneContext.execute(new Runnable() {
              public void run() {
                doneHandler.handle(null);
              }
            });
          }
        }
      }
    }

    final AggHandler aggHandler = new AggHandler();

    String parentDeploymentName = getDeploymentName();
    final Deployment deployment = new Deployment(deploymentName, verticleFactory,
        config == null ? new JsonObject() : config.copy(), urls, modDir, parentDeploymentName);
    deployments.put(deploymentName, deployment);
    if (parentDeploymentName != null) {
      Deployment parent = deployments.get(parentDeploymentName);
      parent.childDeployments.add(deploymentName);
    }

    for (int i = 0; i < instances; i++) {

      // Launch the verticle instance

      Runnable runner = new Runnable() {
        public void run() {

          Verticle verticle;
          try {
            verticle = verticleFactory.createVerticle(main, new ParentLastURLClassLoader(urls, getClass()
                .getClassLoader()));
          } catch (Throwable t) {
            log.error("Failed to create verticle", t);
            doUndeploy(deploymentName, doneHandler);
            return;
          }

          //Inject vertx
          verticle.setVertx(vertx);
          verticle.setContainer(new Container(DefaultVerticleManager.this));

          try {
            addVerticle(deployment, verticle);
            if (modDir != null) {
              setPathAdjustment(modDir);
            }
            verticle.start();
          } catch (Throwable t) {
            vertx.reportException(t);
            doUndeploy(deploymentName, doneHandler);
          }
          aggHandler.started();
        }
      };

      if (worker) {
        vertx.startInBackground(runner);
      } else {
        vertx.startOnEventLoop(runner);
      }

    }

    return deploymentName;
  }

  private String deployMod(String deployName, String modName, JsonObject config,
                           int instances, File currentModDir, Handler<Void> doneHandler) {
    // First we look in the system mod dir then in the user mod dir (if any)
    String res = doDeployMod(systemModRoot, deployName, modName, config, instances, currentModDir, doneHandler);
    if (res == null && userModRoot != null) {
      res = doDeployMod(userModRoot, deployName, modName, config, instances, currentModDir, doneHandler);
    }
    return res;
  }

  // TODO execute this as a blocking action so as not to block the caller
  // TODO cache mod info?
  private String doDeployMod(File dir, String deployName, String modName, JsonObject config,
                             int instances, File currentModDir, Handler<Void> doneHandler) {
    File modDir = new File(dir, modName);
    if (modDir.exists()) {
      String conf;
      try {
        conf = new Scanner(new File(modDir, "mod.json")).useDelimiter("\\A").next();
      } catch (FileNotFoundException e) {
        throw new IllegalStateException("Module " + modName + " does not contain a mod.json file");
      }
      JsonObject json;
      try {
        json = new JsonObject(conf);
      } catch (DecodeException e) {
        throw new IllegalStateException("Module " + modName + " mod.json contains invalid json");
      }

      List<URL> urls = new ArrayList<>();
      try {
        urls.add(modDir.toURI().toURL());
        File libDir = new File(modDir, "lib");
        if (libDir.exists()) {
          File[] jars = libDir.listFiles();
          for (File jar: jars) {
            urls.add(jar.toURI().toURL());
          }
        }
      } catch (MalformedURLException e) {
        //Won't happen
        log.error("malformed url", e);
      }

      String main = json.getString("main");
      if (main == null) {
        throw new IllegalStateException("Module " + modName + " mod.json must contain a \"main\" field");
      }
      Boolean worker = json.getBoolean("worker");
      if (worker == null) {
        worker = Boolean.FALSE;
      }
      Boolean preserveCwd = json.getBoolean("preserve-cwd");
      if (preserveCwd == null) {
        preserveCwd = Boolean.FALSE;
      }
      if (preserveCwd) {
        // Use the current module directory instead, or the cwd if not in a module
        modDir = currentModDir;
      }
      return doDeploy(worker, deployName, main, config,
                      urls.toArray(new URL[urls.size()]), instances, modDir, doneHandler);
    }
    else {
      return null;
    }
  }

  // Must be synchronized since called directly from different thread
  private synchronized void addVerticle(Deployment deployment, Verticle verticle) {
    String loggerName = deployment.name + "-" + deployment.verticles.size();
    Logger logger = LoggerFactory.getLogger(loggerName);
    Context context = Context.getContext();
    VerticleHolder holder = new VerticleHolder(deployment, context, verticle,
                                               loggerName, logger, deployment.config);
    deployment.verticles.add(holder);
    context.setDeploymentHandle(holder);
  }

  private VerticleHolder getVerticleHolder() {
    Context context = Context.getContext();
    if (context != null) {
      VerticleHolder holder = (VerticleHolder)context.getDeploymentHandle();
      return holder;
    } else {
      return null;
    }
  }


  private void doUndeploy(String name, final Handler<Void> doneHandler) {
    UndeployCount count = new UndeployCount();
    doUndeploy(name, count);
    if (doneHandler != null) {
      count.setHandler(doneHandler);
    }
  }

  private void doUndeploy(String name, final UndeployCount count) {

    final Deployment deployment = deployments.remove(name);

    // Depth first - undeploy children first
    for (String childDeployment: deployment.childDeployments) {
      doUndeploy(childDeployment, count);
    }

    if (!deployment.verticles.isEmpty()) {

      for (final VerticleHolder holder: deployment.verticles) {
        count.incRequired();
        holder.context.execute(new Runnable() {
          public void run() {
            try {
              holder.verticle.stop();
            } catch (Throwable t) {
              vertx.reportException(t);
            }
            count.undeployed();
            LoggerFactory.removeLogger(holder.loggerName);
            holder.context.runCloseHooks();
          }
        });
      }
    }

    if (deployment.parentDeploymentName != null) {
      Deployment parent = deployments.get(deployment.parentDeploymentName);
      if (parent != null) {
        parent.childDeployments.remove(name);
      }
    }
  }

  private static class VerticleHolder implements DeploymentHandle {
    final Deployment deployment;
    final Context context;
    final Verticle verticle;
    final String loggerName;
    final Logger logger;
    //We put the config here too so it's still accessible to the verticle after it has been deployed
    //(deploy is async)
    final JsonObject config;

    private VerticleHolder(Deployment deployment, Context context, Verticle verticle, String loggerName,
                           Logger logger, JsonObject config) {
      this.deployment = deployment;
      this.context = context;
      this.verticle = verticle;
      this.loggerName = loggerName;
      this.logger = logger;
      this.config = config;
    }

    public void reportException(Throwable t) {
      deployment.factory.reportException(t);
    }
  }

  private static class Deployment {
    final String name;
    final VerticleFactory factory;
    final JsonObject config;
    final URL[] urls;
    final File modDir;
    final List<VerticleHolder> verticles = new ArrayList<>();
    final List<String> childDeployments = new ArrayList<>();
    final String parentDeploymentName;

    private Deployment(String name, VerticleFactory factory, JsonObject config,
                       URL[] urls, File modDir, String parentDeploymentName) {
      this.name = name;
      this.factory = factory;
      this.config = config;
      this.urls = urls;
      this.modDir = modDir;
      this.parentDeploymentName = parentDeploymentName;
    }
  }

  private class UndeployCount {
    int count;
    int required;
    Handler<Void> doneHandler;
    Context context = vertx.getOrAssignContext();

    synchronized void undeployed() {
      count++;
      checkDone();
    }

    synchronized void incRequired() {
      required++;
    }

    synchronized void setHandler(Handler<Void> doneHandler) {
      this.doneHandler = doneHandler;
      checkDone();
    }

    void checkDone() {
      if (doneHandler != null && count == required) {
        context.execute(new Runnable() {
          public void run() {
            doneHandler.handle(null);
          }
        });
      }
    }
  }
}
