/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.util;

import org.intellij.lang.annotations.Language;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.RegEx;
import java.io.UnsupportedEncodingException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.apache.commons.text.StringEscapeUtils.escapeJava;

/**
 * These static utility functions are helpful when converting arbitrary Java objects to strings.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface Strings {
  /**
   * This helper function converts any object to a String, with special handling for null
   * and for String. Strings will be "escaped" and surrounded by quotation marks.
   * All the rest just get Object.toString() called on them.
   */
  static String objectToEscapedString(@Nullable Object o) {
    if (o instanceof String) {
      // escapeJava comes to us from the Apache Commons-Text library.
      return "\"" + escapeJava((String) o) + "\"";
    } else {
      return objectToString(o);
    }
  }

  /**
   * This helper function converts any object to a String, with special handling for null.
   * All the rest just get Object.toString() called on them.
   */
  static String objectToString(@Nullable Object o) {
    if (o == null) {
      return "null";
    } else {
      return o.toString();
    }
  }

  /**
   * This helper function converts a String to its UTF-8 representation. If there's an
   * error in the internal conversion, an array of length 0 is returned.
   */
  static byte[] stringToUTF8(String input) {
    try {
      return input.getBytes("UTF-8");
    } catch (UnsupportedEncodingException uee) {
      return new byte[0];
    }
  }

  /**
   * This helper function takes a string and a "default value". If the given string is
   * non-null, then that's what's returned. If the given string is null, then the default
   * value is returned instead. This helper is useful when dealing with libraries that
   * want to return null as an error condition, but you would much prefer something else.
   */
  static String stringOrDefault(@Nullable String input, String defaultIfNull) {
    return input == null ? defaultIfNull : input;
  }

  /**
   * This helper function converts from a string to a long, with error handling using
   * our Try class.
   */
  static Try<Long> stringToTryLong(String s) {
    return Try.of(() -> Long.decode(s));
  }

  /**
   * This helper function converts from a string to a long, or returns an Option.none
   * if there's a failure. If you want to be able to get the exception, then use
   * {@link #stringToTryLong(String)}.
   */
  static Option<Long> stringToOptionLong(String s) {
    return stringToTryLong(s).toOption();
  }

  /**
   * This helper function converts from a string to an integer, with error handling using
   * our Try class.
   */
  static Try<Integer> stringToTryInteger(String s) {
    return stringToTryInteger(s, 10);
  }

  /**
   * This helper function converts from a string to an integer, with error handling using
   * our Try class, and allows you to specify the base (e.g., base 16, base 10, etc.).
   */
  static Try<Integer> stringToTryInteger(String s, int base) {
    return Try.of(() -> Integer.valueOf(s, base));
  }

  /**
   * This helper function converts from a string to an integer, or returns an Option.none
   * if there's a failure. If you want to be able to get the exception, then use
   * {@link #stringToTryInteger(String)}.
   */
  static Option<Integer> stringToOptionInteger(String s) {
    return stringToTryInteger(s).toOption();
  }

  /**
   * This helper function converts from a string to an integer, or returns an Option.none
   * if there's a failure. If you want to be able to get the exception, then use
   * {@link #stringToTryInteger(String)}. You can also specify the base (e.g., base 16, base 10, etc.).
   */
  static Option<Integer> stringToOptionInteger(String s, int base) {
    return stringToTryInteger(s, base).toOption();
  }

  /**
   * This helper function converts from a string to a double, with error handling using
   * our Try class.
   */
  static Try<Double> stringToTryDouble(String s) {
    return Try.of(() -> Double.valueOf(s));
  }

  /**
   * This helper function converts from a string to a double, or returns an Option.none
   * if there's a failure. If you want to be able to get the exception, then use
   * {@link #stringToTryDouble(String)}.
   */
  static Option<Double> stringToOptionDouble(String s) {
    return stringToTryDouble(s).toOption();
  }

  /**
   * This helper function converts a regular expression into a predicate on a string.
   */
  static Predicate<String> regexToPredicate(@RegEx @Language("RegExp") String regex)  {
    // Engineering note: we're using two separate annotations here to indicate that the argument is a regular
    // expression. Why? The @Language annotation causes IntelliJ to do full syntax highlighting on the arguments.
    // Very convenient! The former is understood by FindBugs and other common tools but not by IntelliJ.
    // See the bug report below.
    // https://youtrack.jetbrains.com/issue/IDEA-172271

    return Pattern.compile(regex).asPredicate();
  }

  /**
   * Normalizes line endings in strings from CR-LF and CR to be only LF. (That is, given Windows or older Mac line endings,
   * convert everything to Unix-style.) Useful for unit tests.
   */
  static String stringToUnixLinebreaks(String input) {
    return input.replaceAll("\r\n", "\n") // Windows CR-LF -> Unix LF
        .replaceAll("\r", "\n"); // Mac CR -> Unix LF
  }
}
