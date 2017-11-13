/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.json;

import edu.rice.json.Value.*;
import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.list.LazyList;
import org.intellij.lang.annotations.Subst;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * These functions help you build JSON expressions in a "fluent" way. For example:
 * <pre>
 *     <code>
 *         import static edu.rice.json.Builders.*;
 *
 *         Value simpleObject =
 *             jobject(
 *                 jpair("x", 1),
 *                 jpair("y", 2));
 *     </code>
 * </pre>
 * Once you've made a {@link Value} this way, you might then convert it to JSON using the
 * {@link Value#toString()} or {@link Value#toIndentedString()}
 * methods, or you might compare it to something that you've parsed,
 * for equality/testing purposes.
 *
 * <p>Note about strings: there are two ways to build a jstring: from a "Java string" and from a
 * "JSON string". The former represents something you might have in a Java data structure somewhere.
 * The latter represents raw input such as you might have from a network or other source of JSON
 * data. The essential difference comes down to special characters like newline. If you expect the
 * input to have a literal backslash and a literal n, then use the JSON string version. If you
 * expect it to have a newline character, then use the Java version.
 *
 * <p>You'll also notice that there are several versions of the jpair() function. JSON defines an object's
 * key/value pairs as mapping from strings to arbitrary JSON values. For your convenience, you can either
 * use some sort of jstring directly (via jsonString or javaString) or you can give a literal Java
 * string to the jpair() function. In this case, it uses javaString internally.
 *
 * <p>Also, jpair() is heavily overloaded to take native Java types for either of its arguments whenever
 * possible. You can generally avoid needing jboolean(), jnumber(), or jstring() as the second argument
 * of jpair().
 *
 * <p>Of course, if what you have is a collection of raw JSON text, then you won't be using the Builders
 * at all. You would instead use one of the {@link Parser} methods like {@link Parser#parseJsonObject(String)}
 * or {@link Parser#parseJsonValue(String)}.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface Builders {
  /**
   * Fluent builder for {@link JObject}.
   */
  static JObject jobject(JKeyValue... values) {
    return jobject(LazyList.fromArray(values));
  }

  /**
   * Fluent builder for {@link JObject}.
   */
  static JObject jobject(IList<JKeyValue> values) {
    return JObject.fromList(values);
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method uses the {@link KeyValue} class, used in
   * our list and map classes (not to be confused with {@link Value.JKeyValue}), and assumes the key string is
   * <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If you
   * want to start with raw JSON strings, with escaped special characters, then you need to use the
   * {@link #jsonString(String)} builder instead.
   */
  static JKeyValue jpair(KeyValue<String, Value> kv) {
    return JKeyValue.of(kv);
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs.
   */
  static JKeyValue jpair(JString key, Value value) {
    return JKeyValue.of(key, value);
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs.
   */
  static JKeyValue jpair(JString key, boolean bool) {
    return jpair(key, jboolean(bool));
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs.
   */
  static JKeyValue jpair(JString key, double number) {
    return jpair(key, jnumber(number));
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method assumes the value string is
   * <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If you
   * want to start with raw JSON strings, with escaped special characters, then you need to use the
   * {@link #jsonString(String)} builder instead.
   */
  static JKeyValue jpair(JString key, String value) {
    return jpair(key, javaString(value));
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method assumes the key string is
   * <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If you
   * want to start with raw JSON strings, with escaped special characters, then you need to use the
   * {@link #jsonString(String)} builder instead.
   */
  static JKeyValue jpair(String key, Value value) {
    return jpair(javaString(key), value);
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method assumes the key string is
   * <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If you
   * want to start with raw JSON strings, with escaped special characters, then you need to use the
   * {@link #jsonString(String)} builder instead.
   */
  static JKeyValue jpair(String key, boolean bool) {
    return jpair(javaString(key), jboolean(bool));
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method assumes the key string is
   * <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If you
   * want to start with raw JSON strings, with escaped special characters, then you need to use the
   * {@link #jsonString(String)} builder instead.
   */
  static JKeyValue jpair(String key, double number) {
    return jpair(javaString(key), jnumber(number));
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method assumes the key and value strings
   * are <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If you
   * want to start with raw JSON strings, with escaped special characters, then you need to use the
   * {@link #jsonString(String)} builder instead.
   */
  static JKeyValue jpair(String key, String value) {
    return jpair(javaString(key), javaString(value));
  }

  /**
   * Fluent builder for {@link JArray}.
   */
  static JArray jarray(Value... values) {
    return jarray(LazyList.fromArray(values));
  }

  /**
   * Fluent builder for {@link JArray}.
   */
  static JArray jarray(IList<Value> values) {
    return JArray.fromList(values);
  }

  /**
   * Fluent builder for {@link JString}.
   *
   * <p>JSON has very particular rules about backslashes. This method takes as input a JSON string.
   * The JSON string might, for example, have a backslash "escape" followed by an 'n' character, which you
   * might prefer to have "unescaped" to a single newline character. If this is the behavior you want,
   * then this is the method for you.
   *
   * @param string
   *     a raw JSON string, with escapes, such as you might get from a text file on disk or from a
   *     network message
   * @return a JString corresponding to the input
   */
  static JString jsonString(@Subst("") // @Pattern(Scanner.jsonStringPatternNoQuotes)
                                  String string) {
    // Engineering note: what's with all the annotations?

    // @Pattern(...): arbitrary Java strings aren't accepted here; we only want strings that are valid "escaped" JSON strings
    // @Subst(...): but when we're about to pass this string on to the next function, the analyzer isn't smart enough to
    //   do the information flow and notice that the input string above and the output string, below, will necessarily
    //   have the same exact constraints. We fix this by saying "for purposes of static constraint checking, just pretend
    //   we're dealing with the empty string". That's the "substitution" that we're making.

    // Also, why the commented-out annotations? Because of a bug in the annotation processor.
    // https://youtrack.jetbrains.com/issue/IDEA-171173

    return JString.fromEscapedString(string);
  }

  /**
   * Fluent builder for {@link JString}.
   *
   * <p>JSON has very particular rules about backslashes. This method takes as input a Java string.
   * The Java string is assumed to <b>already be unescaped</b>. For example, it might have actual
   * newline characters in it rather than a backslash followed by a 'n'. If that's what you have,
   * then this is the method for you.
   *
   * @param string
   *     a fully unescaped string, such as you might naturally deal with in Java
   * @return a JString corresponding to the input
   */
  static JString javaString(String string) {
    return JString.fromUnescapedString(string);
  }

  /**
   * Fluent builder for {@link JNumber}.
   */
  static JNumber jnumber(double number) {
    return new JNumber(number);
  }

  /**
   * Fluent builder for {@link JBoolean}.
   */
  static JBoolean jboolean(boolean bool) {
    return JBoolean.of(bool);
  }

  /**
   * Fluent builder for {@link JNull}.
   */
  static JNull jnull() {
    return JNull.make();
  }
}
