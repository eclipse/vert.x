package io.vertx.core.cli;

import io.vertx.core.cli.commands.RunCommand;
import io.vertx.core.spi.Command;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * The entry point of the Vert.x Command Line interface.
 */
public class VertxCommandLineInterface extends UsageMessageFormatter {

  private static List<String> PROCESS_ARGS;

  public static List<String> getProcessArguments() {
    return PROCESS_ARGS;
  }

  protected final List<CommandLookup> lookups;

  protected final Map<String, Command> commandByName;
  protected Object main;

  public VertxCommandLineInterface() {
    this(Collections.singletonList(new ServiceCommandLoader()));
  }

  public VertxCommandLineInterface(Collection<CommandLookup> lookups) {
    this.lookups = new ArrayList<>(lookups);
    this.commandByName = new TreeMap<>();
    load();
  }

  protected void load() {
    for (CommandLookup lookup : lookups) {
      final Collection<Command> commands = lookup.lookup();
      for (Command command : commands) {
        commandByName.put(command.name(), command);
      }
    }
  }

  public Collection<String> getCommandNames() {
    return commandByName.keySet();
  }

  public Collection<Command> getCommands() {
    return commandByName.values();
  }

  public Command getCommand(String name) {
    return commandByName.get(name);
  }

  public void execute(String command, String... cla) {
    if (command != null && isAskingForVersion(command)) {
      execute("version");
      return;
    }
    if (command == null || isAskingForHelp(command)) {
      printGlobalUsage();
      return;
    }

    Command cmd = commandByName.get(command);
    if (cmd == null) {
      printCommandNotFound(command);
      return;
    }

    CommandLine line = new CommandLine();
    ExecutionContext context = new ExecutionContext(line, this);

    if (main != null) {
      context.put("Main", main);
      context.put("Main-Class", main.getClass().getName());
    }

    try {
      // Step 1 - definition
      CommandManager.define(cmd, line);
      cmd.initialize(context);

      // Check for help - the command need to have been initialized ot get the complete model.
      if (cla.length >= 1 && isAskingForHelp(cla[0])) {
        printCommandUsage(cmd, line);
        return;
      }

      // Step 2 - parsing and injection
      line.parse(cla);
      CommandManager.inject(cmd, line);

      // Step 3 - validation
      cmd.setup();

      // Step 4 - execution
      cmd.run();

      // Step 5 - cleanup
      cmd.tearDown();
    } catch (MissingOptionException | MissingValueException | InvalidValueException e) {
      printSpecificException(cmd, line, e);
    } catch (CommandLineException e) {
      // Generic error
      printGenericExecutionError(cmd, line, e);
    }
  }


  private void printCommandUsage(Command command, CommandLine line) {
    StringBuilder builder = new StringBuilder();

    String header = getNewLine()
        + CommandManager.getSummary(command) + getNewLine()
        + CommandManager.getDescription(command) + getNewLine()
        + getNewLine();

    computeCommandUsage(builder, getCommandLinePrefix() + " " + command.name(), header, line, "", true);
    System.out.println(builder.toString());
  }

  protected void printGenericExecutionError(Command cmd, CommandLine line, CommandLineException e) {
    System.out.println("Error while executing command " + cmd.name() + ": " + e.getMessage() + getNewLine());
    if (e.getCause() != null) {
      e.getCause().printStackTrace(System.out);
    }
  }

  protected void printSpecificException(Command cmd, CommandLine line, Exception e) {
    System.out.println(e.getMessage() + getNewLine());
    printCommandUsage(cmd, line);
  }

  protected void printCommandNotFound(String command) {
    StringBuilder builder = new StringBuilder();
    buildWrapped(builder, 0, "The command '" + command + "' is not a valid command." + getNewLine()
        + "See '" + getCommandLinePrefix() + " --help'");
    System.out.println(builder.toString());
  }

  protected void printGlobalUsage() {
    StringBuilder builder = new StringBuilder();

    computeUsage(builder, getCommandLinePrefix() + " [COMMAND] [OPTIONS] [arg...]");

    builder.append(getNewLine());
    builder.append("Commands:").append(getNewLine());

    renderCommands(builder, commandByName.values());

    builder.append(getNewLine()).append(getNewLine());

    buildWrapped(builder, 0, "Run '" + getCommandLinePrefix() + " COMMAND --help' for more information on a command.");

    System.out.println(builder.toString());
  }

  protected String getCommandLinePrefix() {
    // Let's try to do an educated guess.

    // Check whether or not the "sun.java.command" system property is defined
    final String property = System.getProperty("sun.java.command");
    if (property != null) {
      final String[] segments = property.split(" ");
      if (segments.length >= 1) {
        // Fat Jar ?
        if (segments[0].endsWith(".jar")) {
          return "java -jar " + segments[0];
        } else {
          // Starter or another launcher passed as command line
          return "java " + segments[0];
        }
      }
    }
    return "vertx";
  }

  public static boolean isAskingForHelp(String command) {
    return command.equalsIgnoreCase("--help")
        || command.equalsIgnoreCase("-help")
        || command.equalsIgnoreCase("-h")
        || command.equalsIgnoreCase("?")
        || command.equalsIgnoreCase("/?");
  }

  private static boolean isAskingForVersion(String command) {
    return command.equalsIgnoreCase("-version") || command.equalsIgnoreCase("--version");
  }

  /**
   * Dispatches to the right command. This method is generally called from the {@code main} method.
   *
   * @param args the command line arguments.
   */
  public void dispatch(String[] args) {
    dispatch(null, args);
  }

  /**
   * Dispatches to the right command. This method is generally called from the {@code main} method.
   *
   * @param main the main instance on which hooks and callbacks are going to be called. If not set, the current
   *             object is used.
   * @param args the command line arguments.
   */
  public void dispatch(Object main, String[] args) {
    this.main = main == null ? this : main;

    PROCESS_ARGS = Collections.unmodifiableList(Arrays.asList(args));

    // We check whether or not we have a main verticle specified via the getMainVerticle method.
    // By default this method retrieve the value from the 'Main-Verticle' Manifest header. However it can be overridden.

    final String verticle = getMainVerticle();
    if (verticle != null) {
      // We have a main verticle, append it to the arg list and execute the default command (run)
      String[] newwArgs = new String[args.length + 1];
      newwArgs[0] = verticle;
      System.arraycopy(args, 0, newwArgs, 1, args.length);
      execute(getDefaultCommand(), newwArgs);
      return;
    }

    if (args.length == 0) {
      execute(null);
    } else {
      // The first argument is the command name.
      execute(args[0], Arrays.copyOfRange(args, 1, args.length));
    }
  }

  public String getDefaultCommand() {
    return "run";
  }

  public String getMainVerticle() {
    try {
      Enumeration<URL> resources = RunCommand.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
      while (resources.hasMoreElements()) {
        Manifest manifest = new Manifest(resources.nextElement().openStream());
        Attributes attributes = manifest.getMainAttributes();
        String mainClass = attributes.getValue("Main-Class");
        if (main.getClass().getName().equals(mainClass)) {
          String theMainVerticle = attributes.getValue("Main-Verticle");
          if (theMainVerticle != null) {
            return theMainVerticle;
          }
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage());
    }
    return null;
  }
}
