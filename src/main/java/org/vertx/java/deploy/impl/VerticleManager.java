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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.SimpleHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.impl.BlockingAction;
import org.vertx.java.core.impl.Context;
import org.vertx.java.core.impl.DeploymentHandle;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.deploy.Container;
import org.vertx.java.deploy.Verticle;
import org.vertx.java.deploy.VerticleFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class VerticleManager {

  private static final Logger log = LoggerFactory.getLogger(VerticleManager.class);
  private static final String REPO_URI_ROOT = "/vertx-mods/mods/";
  private static final String DEFAULT_REPO_HOST = "vert-x.github.com";
  private static final int BUFFER_SIZE = 4096;

  private final VertxInternal vertx;
  // deployment name --> deployment
  private final Map<String, Deployment> deployments = new HashMap<>();
  // The user mods dir
  private final File modRoot;

  private CountDownLatch stopLatch = new CountDownLatch(1);
  
  private Map<String, VerticleFactory> factories;

  public VerticleManager(VertxInternal vertx) {
    this.vertx = vertx;
    VertxLocator.vertx = vertx;
    VertxLocator.container = new Container(this);
    String installDir = System.getProperty("vertx.install");
    if (installDir == null) {
      installDir = ".";
    }
    String modDir = System.getProperty("vertx.mods");
    if (modDir != null && !modDir.trim().equals("")) {
      modRoot = new File(modDir);
    } else {
      // Default to local module directory called 'mods'
      modRoot = new File("./mods");
    }

    this.factories = new HashMap<String, VerticleFactory>();

    // Find and load VerticleFactories
    Iterable<VerticleFactory> services = VerticleFactory.factories;
    for (VerticleFactory vf : services) {
      factories.put(vf.getLanguage(), vf);
    }
  }

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

  public void unblock() {
    stopLatch.countDown();
  }

  public JsonObject getConfig() {
    VerticleHolder holder = getVerticleHolder();
    return holder == null ? null : holder.config;
  }

  public String getDeploymentName() {
    VerticleHolder holder = getVerticleHolder();
    return holder == null ? null : holder.deployment.name;
  }

  public URL[] getDeploymentURLs() {
    VerticleHolder holder = getVerticleHolder();
    return holder == null ? null : holder.deployment.urls;
  }

  public File getDeploymentModDir() {
    VerticleHolder holder = getVerticleHolder();
    return holder == null ? null : holder.deployment.modDir;
  }

  public Logger getLogger() {
    VerticleHolder holder = getVerticleHolder();
    return holder == null ? null : holder.logger;
  }

  public void deploy(boolean worker, final String main,
                     final JsonObject config, final URL[] urls,
                     int instances, File currentModDir,
                     final Handler<String> doneHandler) {
    Context ctx = vertx.getOrAssignContext();
    doDeploy(worker, main, config, urls, instances, currentModDir, ctx, doneHandler);
  }

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

  public synchronized void undeploy(String name, final Handler<Void> doneHandler) {
    if (deployments.get(name) == null) {
      throw new IllegalArgumentException("There is no deployment with name " + name);
    }
    doUndeploy(name, doneHandler);
  }

  public synchronized Map<String, Integer> listInstances() {
    Map<String, Integer> map = new HashMap<>();
    for (Map.Entry<String, Deployment> entry: deployments.entrySet()) {
      map.put(entry.getKey(), entry.getValue().verticles.size());
    }
    return map;
  }

  public void deployMod(final String modName, final JsonObject config,
                        final int instances, final File currentModDir, final Handler<String> doneHandler) {

    final Context ctx = vertx.getOrAssignContext();

    AsyncResultHandler<Boolean> handler = new AsyncResultHandler<Boolean>() {
      public void handle(AsyncResult<Boolean> res) {
        if (res.succeeded()) {
          if (!res.result) {
            System.out.println("Module is not installed");
            // Try and install it
            installMod(null, modName, new Handler<Boolean>() {
              public void handle(Boolean res) {
                if (res) {
                  // Now deploy it
                  deployMod(modName, config, instances, currentModDir, doneHandler);
                } else {
                  if (doneHandler != null) {
                    doneHandler.handle(null);
                  }
                }
              }
            });
          }
        } else {
          res.exception.printStackTrace();
        }
      }
    };

    // Need to run this on the background pool since it does potentially long running stuff
    BlockingAction<Boolean> deployModuleAction = new BlockingAction<Boolean>(vertx, handler) {

      @Override
      public Boolean action() throws Exception {
        System.out.println("Attempting to deploy module " + modName);
        File modDir = new File(modRoot, modName);
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
          doDeploy(worker, main, config,
                   urls.toArray(new URL[urls.size()]), instances, modDir, ctx, doneHandler);
          return true;
        } else {
          return false;
        }
      }
    };

    deployModuleAction.run();
  }

  public void installMod(String repoHost, final String moduleName, final Handler<Boolean> doneHandler) {
    if (repoHost == null) {
      repoHost = DEFAULT_REPO_HOST;
    }
    HttpClient client = vertx.createHttpClient();
    client.setHost(repoHost);
    client.exceptionHandler(new Handler<Exception>() {
      public void handle(Exception e) {
        e.printStackTrace();
        doneHandler.handle(false);
      }
    });
    String uri = REPO_URI_ROOT + moduleName + "/mod.zip";
    System.out.println("Attempting to install module " + moduleName + " from http://" + repoHost + uri);
    HttpClientRequest req = client.get(uri, new Handler<HttpClientResponse>() {
      public void handle(HttpClientResponse resp) {
        if (resp.statusCode == 200) {
          System.out.print("Downloading module...");
          resp.bodyHandler(new Handler<Buffer>() {
            public void handle(Buffer buffer) {
              System.out.println("Done");
              unzipModule(moduleName, buffer);
              doneHandler.handle(true);
            }
          });
        } else if (resp.statusCode == 404) {
          System.out.println("Can't find module " + moduleName);
          doneHandler.handle(false);
        } else {
          System.out.println("Error from server: " + resp.statusCode);
          doneHandler.handle(false);
        }
      }
    });
    req.putHeader("host", "vert-x.github.com");
    req.putHeader("user-agent", "Vert.x Module Installer");
    req.end();
  }

  public void uninstallMod(String moduleName) {
    System.out.println("Removing module " + moduleName + " from directory " + modRoot);
    File modDir = new File(modRoot, moduleName);
    if (!modDir.exists()) {
      System.err.println("Module does not exist");
    } else {
      try {
        vertx.fileSystem().deleteSync(modDir.getAbsolutePath(), true);
      } catch (Exception e) {
        System.err.println("Failed to delete directory: " + e.getMessage());
      }
    }
  }

  private void unzipModule(String modName, Buffer data) {
    if (!modRoot.exists()) {
      modRoot.mkdir();
    }
    System.out.println("Installing module into directory '" + modRoot + "'");
    File fdest = new File(modRoot, modName);
    if (fdest.exists()) {
      System.err.println("Module is already installed");
      return;
    }
    try {
      InputStream is = new ByteArrayInputStream(data.getBytes());
      ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (!entry.getName().startsWith(modName)) {
          System.err.println("Module must contain zipped directory with same name as module");
          fdest.delete();
          return;
        }
        if (entry.isDirectory()) {
          new File(modRoot, entry.getName()).mkdir();
        } else {
          int count;
          byte[] buff = new byte[BUFFER_SIZE];
          BufferedOutputStream dest = null;
          try {
            OutputStream fos = new FileOutputStream(new File(modRoot, entry.getName()));
            dest = new BufferedOutputStream(fos, BUFFER_SIZE);
            while ((count = zis.read(buff, 0, BUFFER_SIZE)) != -1) {
               dest.write(buff, 0, count);
            }
            dest.flush();
          } finally {
            if (dest != null) {
              dest.close();
            }
          }
        }
      }
      zis.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Module successfully installed");
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

  private synchronized void doDeploy(boolean worker, final String main,
                                     final JsonObject config, final URL[] urls,
                                     int instances,
                                     final File modDir,
                                     final Context context,
                                     final Handler<String> doneHandler)
  {

    // Infer the main type
    String language = "java";
    for (VerticleFactory vf : factories.values()) {
      if (vf.isFactoryFor(main)) {
        language = vf.getLanguage();
        break;
      }
    }

    final String deploymentName = "deployment-" + UUID.randomUUID().toString();

    log.debug("Deploying name : " + deploymentName  + " main: " + main +
              " instances: " + instances);

    if (!factories.containsKey(language))
    	throw new IllegalArgumentException("Unsupported language: " + language);

    final VerticleFactory verticleFactory = factories.get(language);
    verticleFactory.init(this);

    final int instCount = instances;

    class AggHandler {
      AtomicInteger count = new AtomicInteger(0);

      void started() {
        if (count.incrementAndGet() == instCount) {
          if (doneHandler != null) {
            context.execute(new Runnable() {
              public void run() {
                doneHandler.handle(deploymentName);
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
            doUndeploy(deploymentName, new SimpleHandler() {
              public void handle() {
                doneHandler.handle(null);
              }
            });
            return;
          }

          //Inject vertx
          verticle.setVertx(vertx);
          verticle.setContainer(new Container(VerticleManager.this));

          try {
            addVerticle(deployment, verticle);
            if (modDir != null) {
              setPathAdjustment(modDir);
            }
            verticle.start();
          } catch (Throwable t) {
            vertx.reportException(t);
            doUndeploy(deploymentName, new SimpleHandler() {
              public void handle() {
                doneHandler.handle(null);
              }
            });
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
