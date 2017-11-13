/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.regex;

import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import org.intellij.lang.annotations.Language;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.regex.Pattern;

import static edu.rice.util.Option.none;
import static edu.rice.util.Option.some;
import static edu.rice.util.Strings.stringOrDefault;

/**
 * A relatively pleasant wrapper around the entirely unpleasant java.util.regex.* library for a
 * variety of common use-case scenarios.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class Matcher {
  @SuppressWarnings("unused")
  private static final String TAG = "Matcher";
  private final Pattern pattern;

  /**
   * Builds a regular expression matcher using the supplied regular expression.
   */
  public Matcher(@Language("RegExp") String regex) {
    this.pattern = Pattern.compile(regex);
  }

  /**
   * If you just want to find all the places in your input that match the regex, then this is the
   * method for you.
   */
  public IList<String> getMatches(String input) {
    java.util.regex.Matcher jmatcher = pattern.matcher(input);
    return LazyList.ogenerate(() ->
      jmatcher.find()
          ? some(jmatcher.group())
          : none());
  }

  /**
   * If your regex has groups in it, this will find the first instance in your regex where it
   * matches and return a list of all the strings matching the corresponding groups.
   *
   * <p>Warning, per {@link java.util.regex.Matcher#group(int)}: "If the match was successful but the group
   * specified failed to match any part of the input sequence, then null is returned. Note that some
   * groups, for example (a*), match the empty string. This method will return the empty string when
   * such a group successfully matches the empty string in the input."
   *
   * <p>If we get "null" back from java.util.regex.Matcher, we'll replace it with the empty string.
   * @see java.util.regex.Matcher#group(int)
   */
  public IList<String> getGroupMatches(String input) {
    java.util.regex.Matcher jmatcher = pattern.matcher(input);
    if (!jmatcher.find()) {
      return List.makeEmpty();
    }
    int numGroups = jmatcher.groupCount();
    if (numGroups == 0) {
      return List.makeEmpty();
    }
    return LazyList.rangeInt(1, numGroups).map(i -> stringOrDefault(jmatcher.group(i), ""));
  }
}
