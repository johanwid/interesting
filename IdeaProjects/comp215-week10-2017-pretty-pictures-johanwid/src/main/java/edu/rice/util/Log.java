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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static edu.rice.util.Strings.objectToEscapedString;
import static edu.rice.util.Strings.objectToString;

/**
 * This is a simplified version of the Android logging system
 * (<a href="http://developer.android.com/reference/android/util/Log.html">http://developer.android.com/reference/android/util/Log.html</a>)
 * that uses slf4j / <a href="https://logback.qos.ch/">Logback</a> as its backend.
 *
 * <p>Also notable: this code is extremely fast when a log level is disabled. If you'd be logging a string
 * that takes time to construct, you can put that in a lambda which supplies the string. The lambda will
 * only be called if the log level is enabled.
 * <pre>
 *   <code>
 *     Log.i(TAG, "result of computation: " + result.toString()); // always computes result.toString()
 *     Log.i(TAG, () -&gt; "result of computation: " + result.toString()); // much more efficient when logging is disabled
 *   </code>
 * </pre>
 *
 * <p>There are two ways you can change the logging level. You can call {@link Log#setLogLevel(int)} somewhere
 * in your program, or you can edit the resources/logback.xml configuration, which also allows you to turn on
 * and off logging for any given tag.
 *
 * <p>See the logback configuration manual for details:
 * <a href="http://logback.qos.ch/manual/configuration.html">http://logback.qos.ch/manual/configuration.html</a>
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class Log {
  private Log() { } // this class should never be instantiated

  // Engineering note: later in the semester, we'll be introducing our own alternatives to HashMap, but since this
  // code will be here from week 1, we're using a mutating Map class from java.util. You won't be doing anything
  // like this in your own code. The broad idea is that we need to maintain one "logger" per "tag". We keep all
  // of that inside this loggerMap.
  private static final Map<String,Logger> loggerMap = new HashMap<>();

  /**
   * logging level: everything goes into the log.
   */
  public static final int ALL = 1;
  /**
   * logging level: only errors go into the log.
   */
  public static final int ERROR = 0;
  /**
   * logging level: nothing is logged at all.
   */
  public static final int NOTHING = -1;

  private static final String TAG = "Log";
  private static int logLevel = ALL;

  static {
    i(TAG, "Comp215 log support ready (JDK" + System.getProperty("java.version") + ")");
  }

  private static Logger logger(String tag) {
    // Once we have a Logback logger for a tag, we don't want to make a new one, so we save
    // the old one. Java's HashMap supports exactly this sort of functionality via it's
    // computeIfAbsent method. In other words, we're *memoizing*, which we'll talk about more
    // later in the semester.
    return loggerMap.computeIfAbsent(tag, LoggerFactory::getLogger);
  }

  /**
   * Set the log level.
   *
   * @param level
   *     (one of Log.ALL, Log.ERROR, or Log.NOTHING)
   */
  public static void setLogLevel(int level) {
    if (level == ALL || level == ERROR || level == NOTHING) {
      logLevel = level;
    } else {
      throw new IllegalStateException("Unknown log level: " + level);
    }
  }

  /**
   * Many of the logging functions let you delay the computation of the log string, such that if logging
   * is turned off, then that computation will never need to happen. That means hiding the computation
   * inside a lambda. So far so good.
   *
   * <p>Normally, we'd just call msgFunc.get() to fetch the string behind the lambda, but what if
   * there's an exception generated in the process of returning that string? We don't want the Log
   * library to ever throw an exception. Solution? We quietly eat exceptions here and, when they do
   * occur, the ultimate log string will reflect that failure, but THE SHOW MUST GO ON!
   */
  private static String safeGet(Supplier<?> msgFunc) {
    try {
      return objectToString(msgFunc.get());
    } catch (Throwable throwable) {
      return String.format("Log string supplier failure!: %s", throwable.toString());
    }
  }

  /**
   * Information logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag
   *     String indicating which code is responsible for the log message
   * @param msgFunc
   *     Lambda providing the string or object to be logged
   * @param th
   *     Throwable, exception, error, etc. to be included in the log
   */
  public static void i(String tag, Supplier<?> msgFunc, Throwable th) {
    //
    // Engineering / performance note:
    //
    // This logging function and every other logging function tries to bail out as early as possible, to avoid
    // any unnecessary computation if the logging level is disabled.
    //
    // There are actually two opportunities for us to detect when a log event will never happen. First, we can
    // check the logLevel, which is internal to edu.rice.util.Log. After that, Logback has its own checking that it
    // will do. We make both checks explicitly here before calling safeGet() to extract the string we're about
    // to log.
    //
    // Elsewhere in Comp215, you shouldn't go to the level of trouble that we do in edu.rice.util.Log, especially since it
    // appears to violate our "don't repeat yourself" principle, but since it's our goal to make these functions
    // outrageously cheap when logging is disabled, we need to go through some extra hoops.
    //
    if (logLevel == ALL) {
      Logger l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(safeGet(msgFunc), th);
      }
    }
  }

  /**
   * Information logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag
   *     String indicating which code is responsible for the log message
   * @param msgFunc
   *     Lambda providing the string or object to be logged
   */
  public static void i(String tag, Supplier<?> msgFunc) {
    if (logLevel == ALL) {
      Logger l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(safeGet(msgFunc));
      }
    }
  }

  /**
   * Information logging. Logs the message.
   *
   * @param tag
   *     String indicating which code is responsible for the log message
   * @param msg
   *     String or object to be logged
   */
  public static void i(String tag, Object msg) {
    if (logLevel == ALL) {
      Logger l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(objectToString(msg));
      }
    }
  }

  /**
   * Information logging. Logs the message.
   *
   * @param tag
   *     String indicating which code is responsible for the log message
   * @param msg
   *     String or object to be logged
   * @param th
   *     Throwable, exception, error, etc. to be included in the log
   */
  public static void i(String tag, Object msg, Throwable th) {
    if (logLevel == ALL) {
      Logger l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(objectToString(msg), th);
      }
    }
  }

  /**
   * Error logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag
   *     String indicating which code is responsible for the log message
   * @param msgFunc
   *     Lambda providing the string or object to be logged
   */
  public static void e(String tag, Supplier<?> msgFunc) {
    if (logLevel >= ERROR) {
      Logger l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(safeGet(msgFunc));
      }
    }
  }

  /**
   * Error logging. Logs the message.
   *
   * @param tag
   *     String indicating which code is responsible for the log message
   * @param msg
   *     String or object to be logged
   */
  public static void e(String tag, Object msg) {
    if (logLevel >= ERROR) {
      Logger l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(objectToString(msg));
      }
    }
  }

  /**
   * Error logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag
   *     String indicating which code is responsible for the log message
   * @param msgFunc
   *     Lambda providing the string or object to be logged
   * @param th
   *     Throwable, exception, error, etc. to be included in the log
   */
  public static void e(String tag, Supplier<?> msgFunc, Throwable th) {
    if (logLevel >= ERROR) {
      Logger l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(safeGet(msgFunc), th);
      }
    }
  }

  /**
   * Error logging. Logs the message.
   *
   * @param tag
   *     String indicating which code is responsible for the log message
   * @param msg
   *     String or object to be logged
   * @param th
   *     Throwable, exception, error, etc. to be included in the log
   */
  public static void e(String tag, Object msg, Throwable th) {
    if (logLevel >= ERROR) {
      Logger l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(objectToString(msg), th);
      }
    }
  }

  /**
   * This higher-order function wraps / decorates an input function, such that every time the
   * resulting function is called, its input and output are logged (via Log.i) with the given tag.
   * If, for example, you wanted to log all the input/output pairs while mapping over a list of
   * strings, you might say something like:
   * <pre>
   * <code>
   *         IList&lt;String&gt; source = List.of("Hello", "World", "My", "Friends");
   *         IList&lt;Integer&gt; result = source.map(Log.iwrap(TAG, x -&gt; x.length()));
   * </code>
   * </pre>
   * The result would still have the mapping, as usual (i.e., (5, 5, 2, 7)) while the
   * logging system would print:
   * <pre>
   * <code>
   *         "Hello" -&gt; 5
   *         "World" -&gt; 5
   *         "My" -&gt; 2
   *         "Friends" -&gt; 7
   * </code>
   * </pre>
   *
   * <p>The internal values are converted to strings by calling their toString() methods, with
   * special handling for Strings.
   * @see Strings#objectToEscapedString(Object)
   */
  public static <A, B> Function<A, B> iwrap(String tag, Function<A, B> func) {
    return input -> {
      B output = func.apply(input);
      i(tag, () -> String.format("%s -> %s", objectToEscapedString(input), objectToEscapedString(output)));
      return output;
    };
  }


  /**
   * This higher-order function wraps / decorates a binary input function, such that every time the
   * resulting function is called, its inputs and output are logged (via {@link Log#i(String, Object)})
   * with the given tag. If, for example, you wanted to log all the input/output pairs while folding a
   * list of strings, you might write something like this:
   * <pre>
   * <code>
   *         IList&lt;String&gt; source = List.of("Hello", "World", "My", "Friends");
   *         int result = source.foldl(Log.iwrap(TAG, (sum, elem) -&gt; sum + elem.length()), 0);
   * </code>
   * </pre>
   * The result would still have the fold's result, as usual (i.e., 19 which equals 5+5+2+7)
   * while the logging system would log the following lines:
   * <pre>
   * <code>
   *         (0, "Hello") -&gt; 5
   *         (5, "World") -&gt; 10
   *         (10, "My") -&gt; 12
   *         (12, "Friends") -&gt; 19
   * </code>
   * </pre>
   *
   * <p>The internal values are converted to strings by calling their toString() methods, with
   * special handling for Strings.
   * @see Strings#objectToEscapedString(Object)
   */
  public static <A, B, C> BiFunction<A, B, C> iwrap(String tag, BiFunction<A, B, C> func) {
    return (input1, input2) -> {
      C output = func.apply(input1, input2);
      i(tag, () ->
          String.format("(%s, %s) -> %s",
              objectToEscapedString(input1), objectToEscapedString(input2), objectToEscapedString(output)));
      return output;
    };
  }

  /**
   * If you've got a deep pipeline of of functions calling functions, and you want to log an
   * intermediate result without having to create a temporary variable to hold it, then this
   * function is here to help you. Typical usage:
   *
   * <pre>
   * <code>
   *     // Beforehand:
   *     Result result = a(b(c(d("Whatever"))))
   *
   *     // Afterward, logging the result of the call to "c":
   *     Result result = a(b(Log.ivalue(TAG, "result from c: ", c(d("Whatever")))))
   * </code>
   * </pre>
   *
   * <p>This function returns whatever is passed to its "value" parameter, while internally
   * calling {@link Log#i(String, Object)} using the tag and explanation. The value parameter's toString() method
   * is used to get its string representation for printing, with special handling for Strings.
   * @see Strings#objectToEscapedString(Object)
   */
  public static <T> T ivalue(String tag, String explanation, T value) {
    i(tag, () -> explanation + objectToEscapedString(value));
    return value;
  }

  /**
   * Passthrough logging of a value. Lambda version.
   * @see Log#ivalue(String, String, Object)
   */
  public static <T> T ivalue(String tag, Supplier<String> explanation, T value) {
    i(tag, () -> explanation.get() + objectToEscapedString(value));
    return value;
  }

  /**
   * Passthrough logging of a value.
   * @see Log#ivalue(String, String, Object)
   */
  public static <T> T evalue(String tag, String explanation, T value) {
    e(tag, () -> explanation + objectToEscapedString(value));
    return value;
  }

  /**
   * Passthrough logging of a value. Lambda version with an exception/throwable to also be logged.
   * @see Log#ivalue(String, String, Object)
   */
  public static <T> T evalue(String tag, Supplier<String> explanation, T value, Throwable error) {
    e(tag, () -> explanation.get() + objectToEscapedString(value), error);
    return value;
  }

  /**
   * Passthrough logging of a value with an exception/throwable to also be logged.
   * @see Log#ivalue(String, String, Object)
   */
  public static <T> T evalue(String tag, String explanation, T value, Throwable error) {
    e(tag, () -> explanation + objectToEscapedString(value), error);
    return value;
  }

  /**
   * Passthrough logging of a value. Lambda version.
   * @see Log#ivalue(String, String, Object)
   */
  public static <T> T evalue(String tag, Supplier<String> explanation, T value) {
    e(tag, () -> explanation.get() + objectToEscapedString(value));
    return value;
  }
}
