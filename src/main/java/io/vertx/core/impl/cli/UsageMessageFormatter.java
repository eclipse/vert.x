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
package io.vertx.core.impl.cli;

import io.vertx.core.spi.cli.ArgumentModel;
import io.vertx.core.spi.cli.Command;
import io.vertx.core.spi.cli.CommandLine;
import io.vertx.core.spi.cli.OptionModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;


public class UsageMessageFormatter {

  /**
   * default number of characters per line
   */
  public static final int DEFAULT_WIDTH = 80;

  /**
   * default padding to the left of each line
   */
  public static final int DEFAULT_LEFT_PAD = 1;

  /**
   * number of space characters to be prefixed to each description line
   */
  public static final int DEFAULT_DESC_PAD = 3;

  /**
   * the string to display at the beginning of the usage statement
   */
  public static final String DEFAULT_USAGE_PREFIX = "Usage: ";

  /**
   * default prefix for shortOpts
   */
  public static final String DEFAULT_OPT_PREFIX = "-";

  /**
   * default prefix for long Option
   */
  public static final String DEFAULT_LONG_OPT_PREFIX = "--";

  /**
   * default separator displayed between a long Option and its value
   */
  public static final String DEFAULT_LONG_OPT_SEPARATOR = " ";

  /**
   * default name for an argument
   */
  public static final String DEFAULT_ARG_NAME = "arg";

  private int width = DEFAULT_WIDTH;
  private int leftPad = DEFAULT_LEFT_PAD;
  private int descPad = DEFAULT_DESC_PAD;
  private String usagePrefix = DEFAULT_USAGE_PREFIX;
  private String newLine = System.lineSeparator();
  private String defaultOptionPrefix = DEFAULT_OPT_PREFIX;
  private String defaultLongOptPrefix = DEFAULT_LONG_OPT_PREFIX;
  private String defaultArgName = DEFAULT_ARG_NAME;
  private String longOptSeparator = DEFAULT_LONG_OPT_SEPARATOR;

  /**
   * Comparator used to sort the options when they output in help text
   * <p/>
   * Defaults to case-insensitive alphabetical sorting by option key
   */
  private Comparator<OptionModel> optionComparator = new OptionComparator();

  public void setWidth(int width) {
    this.width = width;
  }

  public int getWidth() {
    return width;
  }

  public void setLeftPadding(int padding) {
    this.leftPad = padding;
  }

  public int getLeftPadding() {
    return leftPad;
  }

  public void setDescPadding(int padding) {
    this.descPad = padding;
  }

  public int getDescPadding() {
    return descPad;
  }

  public void setUsagePrefix(String prefix) {
    this.usagePrefix = prefix;
  }

  public String getUsagePrefix() {
    return usagePrefix;
  }

  public void setNewLine(String newline) {
    this.newLine = newline;
  }

  public String getNewLine() {
    return newLine;
  }

  public void setOptionPrefix(String prefix) {
    this.defaultOptionPrefix = prefix;
  }

  public String getOptionPrefix() {
    return defaultOptionPrefix;
  }

  public void setLongOptionPrefix(String prefix) {
    this.defaultLongOptPrefix = prefix;
  }

  public String getLongOptionPrefix() {
    return defaultLongOptPrefix;
  }

  /**
   * Set the separator displayed between a long option and its value.
   * Ensure that the separator specified is supported by the parser used,
   * typically ' ' or '='.
   *
   * @param longOptSeparator the separator, typically ' ' or '='.
   */
  public void setLongOptionSeparator(String longOptSeparator) {
    this.longOptSeparator = longOptSeparator;
  }

  /**
   * Returns the separator displayed between a long option and its value.
   *
   * @return the separator
   */
  public String getLongOptionSeparator() {
    return longOptSeparator;
  }

  public void setArgName(String name) {
    this.defaultArgName = name;
  }

  public String getArgName() {
    return defaultArgName;
  }

  /**
   * Comparator used to sort the options when they output in help text.
   * Defaults to case-insensitive alphabetical sorting by option key.
   *
   * @return the {@link Comparator} currently in use to sort the options
   */
  public Comparator<OptionModel> getOptionComparator() {
    return optionComparator;
  }

  /**
   * Set the comparator used to sort the options when they output in help text.
   * Passing in a null comparator will keep the options in the order they were declared.
   *
   * @param comparator the {@link Comparator} to use for sorting the options
   */
  public void setOptionComparator(Comparator<OptionModel> comparator) {
    this.optionComparator = comparator;
  }

  /**
   * Computes the help for <code>options</code> with the specified
   * command line syntax.
   *
   * @param buffer        the buffer to which the help will be written
   * @param cmdLineSyntax the syntax for this application
   * @param header        the banner to display at the beginning of the help
   * @param commandLine   the command line
   * @param footer        the banner to display at the end of the help
   * @param autoUsage     whether to compute an automatically generated
   *                      usage statement
   * @throws IllegalStateException if there is no room to build a line
   */
  public void computeCommandUsage(StringBuilder buffer, String cmdLineSyntax,
                                  String header, CommandLine commandLine,
                                  String footer, boolean autoUsage) {
    if (cmdLineSyntax == null || cmdLineSyntax.length() == 0) {
      throw new IllegalArgumentException("cmdLineSyntax not provided");
    }

    if (autoUsage) {
      computeUsage(buffer, cmdLineSyntax, commandLine);
    } else {
      computeUsage(buffer, cmdLineSyntax);
    }

    if (header != null && header.trim().length() > 0) {
      buildWrapped(buffer, header);
    }

    computeOptions(buffer, commandLine.getOptions());

    if (footer != null && footer.trim().length() > 0) {
      buildWrapped(buffer, footer);
    }
  }

  /**
   * Appends the usage clause for an Option to a StringBuilder.
   *
   * @param buff     the StringBuilder to append to
   * @param option   the Option to append
   * @param required whether the Option is required or not
   */
  private void appendOption(StringBuilder buff, OptionModel option, boolean required) {
    if (option.isHidden()) {
      return;
    }

    if (!required) {
      buff.append("[");
    }

    if (option.getShortName() != null) {
      buff.append("-").append(option.getShortName());
    } else {
      buff.append("--").append(option.getLongName());
    }

    // if the Option has a value and a non blank argname
    if (option.acceptMoreValues() && (option.getArgName() == null || option.getArgName().length() != 0)) {
      buff.append(option.getShortName() == null ? longOptSeparator : " ");
      buff.append("<").append(option.getArgName() != null ? option.getArgName() : getArgName()).append(">");
    }

    // if the Option is not a required option
    if (!required) {
      buff.append("]");
    }
  }

  /**
   * Appends the usage clause for an Argument to a StringBuilder.
   *
   * @param buff     the StringBuilder to append to
   * @param argument the argument to add
   * @param required whether the Option is required or not
   */
  private void appendArgument(StringBuilder buff, ArgumentModel argument, boolean required) {
    if (!required) {
      buff.append("[");
    }

    buff.append(argument.getArgName());

    // if the Option is not a required option
    if (!required) {
      buff.append("]");
    }
  }

  public void computeUsage(StringBuilder buffer, String cmdLineSyntax) {
    int argPos = cmdLineSyntax.indexOf(' ') + 1;
    buildWrapped(buffer, getUsagePrefix().length() + argPos, getUsagePrefix() + cmdLineSyntax);
  }

  public void computeUsage(StringBuilder buffer, String cli, CommandLine commandLine) {
    // initialise the string buffer
    StringBuilder buff = new StringBuilder(getUsagePrefix()).append(cli).append(" ");


    if (getOptionComparator() != null) {
      Collections.sort(commandLine.getOptions(), getOptionComparator());
    }

    // iterate over the options
    for (OptionModel option : commandLine.getOptions()) {
      appendOption(buff, option, option.isRequired());
      buff.append(" ");
    }

    // iterate over the arguments
    for (ArgumentModel arg : commandLine.getArguments()) {
      appendArgument(buff, arg, arg.isRequired());
      buff.append(" ");
    }

    buildWrapped(buffer, buff.toString().indexOf(' ') + 1, buff.toString());
  }


  /**
   * Computes the help for the specified Options to the specified writer.
   *
   * @param buffer  The buffer to write the help to
   * @param options The command line Options
   */
  public void computeOptions(StringBuilder buffer, List<OptionModel> options) {
    renderOptions(buffer, options);
    buffer.append(newLine);
  }

  /**
   * Builds the specified text to the specified buffer.
   *
   * @param buffer The buffer to write the help to
   * @param text   The text to be written to the buffer
   */
  public void buildWrapped(StringBuilder buffer, String text) {
    buildWrapped(buffer, 0, text);
  }

  /**
   * Builds the specified text to the specified buffer.
   *
   * @param buffer          The buffer to write the help to
   * @param nextLineTabStop The position on the next line for the first tab.
   * @param text            The text to be written to the buffer
   */
  public void buildWrapped(StringBuilder buffer, int nextLineTabStop, String text) {
    renderWrappedTextBlock(buffer, width, nextLineTabStop, text);
    buffer.append(newLine);
  }

  protected StringBuilder renderCommands(StringBuilder sb, Collection<Command> commands) {
    final String lpad = createPadding(leftPad);
    final String dpad = createPadding(descPad);

    // We need a double loop to compute the longest command name
    int max = 0;
    List<StringBuilder> prefixList = new ArrayList<>();

    for (Command command : commands) {
      if (! command.hidden()) {
        StringBuilder buf = new StringBuilder();
        buf.append(lpad).append("   ").append(command.name());
        prefixList.add(buf);
        max = buf.length() > max ? buf.length() : max;
      }
    }

    int x = 0;

    // Use an iterator to detect the last item.
    for (Iterator<Command> it = commands.iterator(); it.hasNext(); ) {
      Command command = it.next();
      if (command.hidden()) {
        continue;
      }

      StringBuilder buf = new StringBuilder(prefixList.get(x++).toString());

      if (buf.length() < max) {
        buf.append(createPadding(max - buf.length()));
      }

      buf.append(dpad);

      int nextLineTabStop = max + descPad;
      buf.append(command.summary());
      renderWrappedText(sb, width, nextLineTabStop, buf.toString());

      if (it.hasNext()) {
        sb.append(getNewLine());
      }
    }

    return sb;

  }

  /**
   * Renders the specified Options and return the rendered Options
   * in a StringBuilder.
   *
   * @param sb      The StringBuilder to place the rendered Options into.
   * @param options The command line Options
   * @return the StringBuilder with the rendered Options contents.
   */
  protected StringBuilder renderOptions(StringBuilder sb, List<OptionModel> options) {
    final String lpad = createPadding(leftPad);
    final String dpad = createPadding(descPad);

    // first create list containing only <lpad>-a,--aaa where
    // -a is opt and --aaa is long opt; in parallel look for
    // the longest opt string this list will be then used to
    // sort options ascending
    int max = 0;
    List<StringBuilder> prefixList = new ArrayList<>();

    if (getOptionComparator() != null) {
      Collections.sort(options, getOptionComparator());
    }

    for (OptionModel option : options) {
      if (option.isHidden()) {
        continue;
      }
      StringBuilder optBuf = new StringBuilder();

      if (option.getShortName() == null) {
        optBuf.append(lpad).append("   ").append(getLongOptionPrefix()).append(option.getLongName());
      } else {
        optBuf.append(lpad).append(getOptionPrefix()).append(option.getShortName());

        if (option.getLongName() != null) {
          optBuf.append(',').append(getLongOptionPrefix()).append(option.getLongName());
        }
      }

      if (option.acceptMoreValues()) {
        String argName = option.getArgName();
        if (argName != null && argName.length() == 0) {
          // if the option has a blank argname
          optBuf.append(' ');
        } else {
          optBuf.append(option.getLongName() != null ? longOptSeparator : " ");
          optBuf.append("<").append(argName != null ? option.getArgName() : getArgName()).append(">");
        }
      }

      prefixList.add(optBuf);
      max = optBuf.length() > max ? optBuf.length() : max;
    }

    int x = 0;

    // Use an iterator to detect the last item.
    for (Iterator<OptionModel> it = options.iterator(); it.hasNext(); ) {
      OptionModel option = it.next();
      if (option.isHidden()) {
        continue;
      }
      StringBuilder optBuf = new StringBuilder(prefixList.get(x++).toString());

      if (optBuf.length() < max) {
        optBuf.append(createPadding(max - optBuf.length()));
      }

      optBuf.append(dpad);

      int nextLineTabStop = max + descPad;

      if (option.getDescription() != null) {
        optBuf.append(option.getDescription());
      }

      renderWrappedText(sb, width, nextLineTabStop, optBuf.toString());

      if (it.hasNext()) {
        sb.append(getNewLine());
      }
    }

    return sb;
  }

  /**
   * Render the specified text and return the rendered Options
   * in a StringBuilder.
   *
   * @param sb              The StringBuilder to place the rendered text into.
   * @param width           The number of characters to display per line
   * @param nextLineTabStop The position on the next line for the first tab.
   * @param text            The text to be rendered.
   * @return the StringBuilder with the rendered Options contents.
   */
  protected StringBuilder renderWrappedText(StringBuilder sb, int width,
                                            int nextLineTabStop, String text) {
    int pos = findWrapPos(text, width, 0);

    if (pos == -1) {
      sb.append(rtrim(text));

      return sb;
    }
    sb.append(rtrim(text.substring(0, pos))).append(getNewLine());

    if (nextLineTabStop >= width) {
      // stops infinite loop happening
      nextLineTabStop = 1;
    }

    // all following lines must be padded with nextLineTabStop space characters
    final String padding = createPadding(nextLineTabStop);

    while (true) {
      text = padding + text.substring(pos).trim();
      pos = findWrapPos(text, width, 0);

      if (pos == -1) {
        sb.append(text);

        return sb;
      }

      if (text.length() > width && pos == nextLineTabStop - 1) {
        pos = width;
      }

      sb.append(rtrim(text.substring(0, pos))).append(getNewLine());
    }
  }

  /**
   * Renders the specified text width a maximum width. This method differs
   * from renderWrappedText by not removing leading spaces after a new line.
   *
   * @param sb              The StringBuilder to place the rendered text into.
   * @param width           The number of characters to display per line
   * @param nextLineTabStop The position on the next line for the first tab.
   * @param text            The text to be rendered.
   */
  private Appendable renderWrappedTextBlock(StringBuilder sb, int width, int nextLineTabStop, String text) {
    try {
      BufferedReader in = new BufferedReader(new StringReader(text));
      String line;
      boolean firstLine = true;
      while ((line = in.readLine()) != null) {
        if (!firstLine) {
          sb.append(getNewLine());
        } else {
          firstLine = false;
        }
        renderWrappedText(sb, width, nextLineTabStop, line);
      }
    } catch (IOException e) //NOPMD
    {
      // cannot happen
    }

    return sb;
  }

  /**
   * Finds the next text wrap position after <code>startPos</code> for the
   * text in <code>text</code> with the column width <code>width</code>.
   * The wrap point is the last position before startPos+width having a
   * whitespace character (space, \n, \r). If there is no whitespace character
   * before startPos+width, it will return startPos+width.
   *
   * @param text     The text being searched for the wrap position
   * @param width    width of the wrapped text
   * @param startPos position from which to start the lookup whitespace
   *                 character
   * @return position on which the text must be wrapped or -1 if the wrap
   * position is at the end of the text
   */
  protected int findWrapPos(String text, int width, int startPos) {
    // the line ends before the max wrap pos or a new line char found
    int pos = text.indexOf('\n', startPos);
    if (pos != -1 && pos <= width) {
      return pos + 1;
    }

    pos = text.indexOf('\t', startPos);
    if (pos != -1 && pos <= width) {
      return pos + 1;
    }

    if (startPos + width >= text.length()) {
      return -1;
    }

    // look for the last whitespace character before startPos+width
    for (pos = startPos + width; pos >= startPos; --pos) {
      final char c = text.charAt(pos);
      if (c == ' ' || c == '\n' || c == '\r') {
        break;
      }
    }

    // if we found it - just return
    if (pos > startPos) {
      return pos;
    }

    // if we didn't find one, simply chop at startPos+width
    pos = startPos + width;

    return pos == text.length() ? -1 : pos;
  }

  /**
   * Return a String of padding of length <code>len</code>.
   *
   * @param len The length of the String of padding to create.
   * @return The String of padding
   */
  protected String createPadding(int len) {
    char[] padding = new char[len];
    Arrays.fill(padding, ' ');

    return new String(padding);
  }

  /**
   * Remove the trailing whitespace from the specified String.
   *
   * @param s The String to remove the trailing padding from.
   * @return The String of without the trailing padding
   */
  protected String rtrim(String s) {
    if (s == null || s.length() == 0) {
      return s;
    }

    int pos = s.length();

    while (pos > 0 && Character.isWhitespace(s.charAt(pos - 1))) {
      --pos;
    }

    return s.substring(0, pos);
  }

  /**
   * This class implements the <code>Comparator</code> interface
   * for comparing Options.
   */
  private static class OptionComparator implements Comparator<OptionModel> {

    /**
     * Compares its two arguments for order. Returns a negative
     * integer, zero, or a positive integer as the first argument
     * is less than, equal to, or greater than the second.
     *
     * @param opt1 The first Option to be compared.
     * @param opt2 The second Option to be compared.
     * @return a negative integer, zero, or a positive integer as
     * the first argument is less than, equal to, or greater than the
     * second.
     */
    public int compare(OptionModel opt1, OptionModel opt2) {
      return opt1.getKey().compareToIgnoreCase(opt2.getKey());
    }
  }
}
