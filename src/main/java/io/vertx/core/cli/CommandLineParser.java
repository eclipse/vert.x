package io.vertx.core.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parse the command line and set the value to the options.
 * Unrecognized options are also stored.
 */
public class CommandLineParser {

  //TODO first / last unamed parameters
  // TODO Option help line

  protected String token;
  protected OptionModel current;
  protected List<OptionModel> expectedOpts;
  private CommandLine commandLine;

  /**
   * Remove the hyphens from the beginning of <code>str</code> and
   * return the new String.
   *
   * @param str The string from which the hyphens should be removed.
   * @return the new String.
   */
  static String stripLeadingHyphens(String str) {
    if (str == null) {
      return null;
    }
    if (str.startsWith("--")) {
      return str.substring(2, str.length());
    } else if (str.startsWith("-")) {
      return str.substring(1, str.length());
    }

    return str;
  }

  /**
   * Remove the leading and trailing quotes from <code>str</code>.
   * E.g. if str is '"one two"', then 'one two' is returned.
   *
   * @param str The string from which the leading and trailing quotes
   *            should be removed.
   * @return The string without the leading and trailing quotes.
   */
  static String stripLeadingAndTrailingQuotes(String str) {
    int length = str.length();
    if (length > 1 && str.startsWith("\"") && str.endsWith("\"") && str.substring(1, length - 1).indexOf('"') == -1) {
      str = str.substring(1, length - 1);
    }

    return str;
  }

  public CommandLine parse(CommandLine commandLine, String... cla)
      throws CommandLineException {

    this.commandLine = commandLine;

    current = null;

    // Reset the option state.
    commandLine.clear();


    // Extract the list of required options.
    // Every time an option get a value, it is removed from the list.
    expectedOpts = getRequiredOptions();

    if (cla != null) {
      for (String arg : cla) {
        visit(arg);
      }
    }

    // check the values of the last option
    checkRequiredValues();

    // check that all required options has a value
    checkRequiredOptions();

    // Call global validation.
    commandLine.validate();

    return commandLine;
  }

  private List<OptionModel> getRequiredOptions() {
    return commandLine.getOptions().stream().filter(OptionModel::isRequired).collect(Collectors.toList());
  }

  private void checkRequiredOptions() throws MissingOptionException {
    // if there are required options that have not been processed
    if (!expectedOpts.isEmpty()) {
      throw new MissingOptionException(expectedOpts);
    }
  }

  private void checkRequiredValues() throws MissingValueException {
    if (current != null) {
      if (current.acceptValue() && !current.hasValue() && !current.maybeFlag()) {
        throw new MissingValueException(current);
      }
    }
  }

  private void visit(String token) throws CommandLineException {
    this.token = token;

    if (current != null && current.acceptValue() && isArgument(token)) {
      current.process(stripLeadingAndTrailingQuotes(token));
    } else if (token.startsWith("--")) {
      handleLongOption(token);
    } else if (token.startsWith("-") && !"-".equals(token)) {
      handleShortAndLongOption(token);
    } else {
      handleArgument(token);
    }

    if (current != null && !current.acceptMoreValues()) {
      current = null;
    }
  }

  /**
   * Returns true is the token is a valid argument.
   *
   * @param token
   */
  private boolean isArgument(String token) {
    return !isOption(token) || isNegativeNumber(token);
  }

  /**
   * Check if the token is a negative number.
   *
   * @param token
   */
  private boolean isNegativeNumber(String token) {
    try {
      Double.parseDouble(token);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Tells if the token looks like an option.
   *
   * @param token
   */
  private boolean isOption(String token) {
    return isLongOption(token) || isShortOption(token);
  }

  /**
   * Tells if the token looks like a short option.
   *
   * @param token
   */
  private boolean isShortOption(String token) {
    // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
    return token.startsWith("-") && token.length() >= 2 && hasOptionWithShortName(token.substring(1, 2));
  }

  /**
   * Tells if the token looks like a long option.
   *
   * @param token
   */
  private boolean isLongOption(String token) {
    if (!token.startsWith("-") || token.length() == 1) {
      return false;
    }

    int pos = token.indexOf("=");
    String t = pos == -1 ? token : token.substring(0, pos);

    if (!getMatchingOptions(t).isEmpty()) {
      // long or partial long options (--L, -L, --L=V, -L=V, --l, --l=V)
      return true;
    } else if (getLongPrefix(token) != null && !token.startsWith("--")) {
      // -LV
      return true;
    }

    return false;
  }

  private void handleArgument(String token) throws CommandLineException {
    commandLine.argumentFound(token);
  }

  /**
   * Handles the following tokens:
   * <p/>
   * --L
   * --L=V
   * --L V
   * --l
   *
   * @param token the command line token to handle
   */
  private void handleLongOption(String token) throws CommandLineException {
    if (token.indexOf('=') == -1) {
      handleLongOptionWithoutEqual(token);
    } else {
      handleLongOptionWithEqual(token);
    }
  }

  /**
   * Handles the following tokens:
   * <p/>
   * --L
   * -L
   * --l
   * -l
   *
   * @param token the command line token to handle
   */
  private void handleLongOptionWithoutEqual(String token) throws CommandLineException {
    List<OptionModel> matchingOpts = getMatchingOptions(token);
    if (matchingOpts.isEmpty()) {
      handleArgument(token);
    } else if (matchingOpts.size() > 1) {
      throw new AmbiguousOptionException(token, matchingOpts);
    } else {
      final OptionModel option = matchingOpts.get(0);
      handleOption(option);
    }
  }

  /**
   * Handles the following tokens:
   * <p/>
   * --L=V
   * -L=V
   * --l=V
   * -l=V
   *
   * @param token the command line token to handle
   */
  private void handleLongOptionWithEqual(String token) throws CommandLineException {
    int pos = token.indexOf('=');

    String value = token.substring(pos + 1);

    String opt = token.substring(0, pos);

    List<OptionModel> matchingOpts = getMatchingOptions(opt);
    if (matchingOpts.isEmpty()) {
      handleArgument(token);
    } else if (matchingOpts.size() > 1) {
      throw new AmbiguousOptionException(opt, matchingOpts);
    } else {
      OptionModel option = matchingOpts.get(0);
      if (option.acceptMoreValues()) {
        handleOption(option);
        current.process(value);
        current = null;
      } else {
        handleArgument(token);
      }
    }
  }

  /**
   * Handles the following tokens:
   * <p/>
   * -S
   * -SV
   * -S V
   * -S=V
   * <p/>
   * -L
   * -LV
   * -L V
   * -L=V
   * -l
   *
   * @param token the command line token to handle
   */
  private void handleShortAndLongOption(String token) throws CommandLineException {
    String t = stripLeadingHyphens(token);

    int pos = t.indexOf('=');

    if (t.length() == 1) {
      // -S
      if (hasOptionWithShortName(t)) {
        handleOption(getOption(t));
      } else {
        handleArgument(token);
      }
    } else if (pos == -1) {
      // no equal sign found (-xxx)
      if (hasOptionWithShortName(t)) {
        handleOption(getOption(t));
      } else if (!getMatchingOptions(t).isEmpty()) {
        // -L or -l
        handleLongOptionWithoutEqual(token);
      } else {
        // look for a long prefix (-Xmx512m)
        String opt = getLongPrefix(t);

        if (opt != null && getOption(opt).acceptMoreValues()) {
          handleOption(getOption(opt));
          current.process(t.substring(opt.length()));
          current = null;
        } else if (isAValidShortOption(t)) {
          // -SV1 (-Dflag)
          String strip = t.substring(0, 1);
          OptionModel option = getOption(strip);
          handleOption(option);
          current.process(t.substring(1));
          current = null;
        }
      }
    } else {
      // equal sign found (-xxx=yyy)
      String opt = t.substring(0, pos);
      String value = t.substring(pos + 1);

      if (opt.length() == 1) {
        // -S=V
        OptionModel option = getOption(opt);
        if (option != null && option.acceptMoreValues()) {
          handleOption(option);
          current.process(value);
          current = null;
        } else {
          handleArgument(token);
        }
      } else if (isAValidShortOption(opt)) {
        // -SV1=V2 (-Dkey=value)
        handleOption(getOption(opt.substring(0, 1)));
        current.process(opt.substring(1) + "=" + value);
        current = null;
      } else {
        // -L=V or -l=V
        handleLongOptionWithEqual(token);
      }
    }
  }

  /**
   * Search for a prefix that is the long name of an option (-Xmx512m)
   *
   * @param token
   */
  private String getLongPrefix(String token) {
    String t = stripLeadingHyphens(token);

    int i;
    String opt = null;
    for (i = t.length() - 2; i > 1; i--) {
      String prefix = t.substring(0, i);
      if (hasOptionWithLongName(prefix)) {
        opt = prefix;
        break;
      }
    }

    return opt;
  }

  private boolean hasOptionWithLongName(String name) {
    for (OptionModel option : commandLine.getOptions()) {
      if (name.equalsIgnoreCase(option.getLongName())) {
        return true;
      }
    }
    return false;
  }

  private boolean hasOptionWithShortName(String name) {
    for (OptionModel option : commandLine.getOptions()) {
      if (name.equalsIgnoreCase(option.getShortName())) {
        return true;
      }
    }
    return false;
  }

  private void handleOption(OptionModel option) throws CommandLineException {
    // check the previous option before handling the next one
    checkRequiredValues();
    updateRequiredOptions(option);
    option.setInCommandLine();
    if (option.acceptMoreValues()) {
      current = option;
    } else {
      current = null;
    }
  }

  /**
   * Removes the option from the list of expected elements.
   *
   * @param option
   */
  private void updateRequiredOptions(OptionModel option) {
    if (option.isRequired()) {
      expectedOpts.remove(option);
    }
  }

  /**
   * Retrieve the {@link OptionModel} matching the long or short name specified.
   * The leading hyphens in the name are ignored (up to 2).
   *
   * @param opt short or long name of the {@link OptionModel}
   * @return the option represented by opt
   */
  public OptionModel getOption(String opt) {
    opt = stripLeadingHyphens(opt);
    for (OptionModel option : commandLine.getOptions()) {
      if (opt.equalsIgnoreCase(option.getShortName()) || opt.equalsIgnoreCase(option.getLongName())) {
        return option;
      }
    }
    return null;
  }

  private boolean isAValidShortOption(String token) {
    String opt = token.substring(0, 1);
    OptionModel option = getOption(opt);
    return option != null;
  }

  /**
   * Returns the options with a long name starting with the name specified.
   *
   * @param opt the partial name of the option
   * @return the options matching the partial name specified, or an empty list if none matches
   */
  public List<OptionModel> getMatchingOptions(String opt) {
    opt = stripLeadingHyphens(opt);

    List<OptionModel> matching = new ArrayList<>();


    final List<OptionModel> options = commandLine.getOptions();
    for (OptionModel option : options) {
      if (opt.equalsIgnoreCase(option.getLongName())) {
        return Collections.singletonList(option);
      }
    }

    for (OptionModel option : options) {
      if (option.getLongName() != null && option.getLongName().startsWith(opt)) {
        matching.add(option);
      }
    }

    return matching;
  }

}
