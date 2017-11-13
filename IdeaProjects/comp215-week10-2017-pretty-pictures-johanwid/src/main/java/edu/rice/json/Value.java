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

import edu.rice.lens.Lens;
import edu.rice.lens.MonoLens;
import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.list.LazyList;
import edu.rice.list.Memo;
import edu.rice.tree.IMap;
import edu.rice.tree.TreapMap;
import edu.rice.util.Option;
import org.intellij.lang.annotations.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.apache.commons.text.StringEscapeUtils.escapeJson;
import static org.apache.commons.text.StringEscapeUtils.unescapeJson;

/**
 * A JSON value can be many different things: a JSON object, array, string, number, boolean, or null.
 * This interface supports all these different JSON value types. If you're looking for a nice fluent/pipelined
 * way of dealing with typecasting, use the various "as" methods to do your casting for you or check out the
 * {@link #match(Function, Function, Function, Function, Function, Function)} structural pattern matcher.
 *
 * <p>If you wish to parse a String into a JSON Value, check out the {@link Parser} class.
 *
 * <p>If you wish to build up a JSON structure from scratch, perhaps for pretty-printing or whatever else,
 * check out the {@link Builders} class, which has convenient functions for this purpose.
 *
 * <p>If you wish to treat a JSON structure as a database that you can query and update (in a functional
 * way, of course), then check out the {@link Operations} class, which offers many such features.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface Value {
  /**
   * General-purpose structural pattern matching on a JSON value, with one lambda per concrete
   * type of the Value. Typical usage:
   * <pre><code>
   *     Value val = ... ;
   *     Option&lt;Whatever&gt; oresult = val.match(
   *         jObject -&gt; Option.none(),
   *         jArray -&gt; Option.none(),
   *         jString -&gt; Option.some(jString.something()),
   *         jNumber -&gt; Option.some(jNumber.somethingElse()),
   *         jBoolean -&gt; Option.none(),
   *         jNull -&gt; Option.none());
   * </code></pre>
   */
  default <Q> Q match(Function<? super JObject, ? extends Q> jobjectF,
                      Function<? super JArray, ? extends Q> jarrayF,
                      Function<? super JString, ? extends Q> jstringF,
                      Function<? super JNumber, ? extends Q> jnumberF,
                      Function<? super JBoolean, ? extends Q> jboolF,
                      Function<? super JNull, ? extends Q> jnullF) {

    // Engineering note: Some purists would say that we shouldn't do this switch statement here, but should instead
    // dispatch to a one-line method in each of the classes. If we did that, then each class would need this huge
    // match method (repeated code... yuck!). The counter-argument is that we have to do all the casting here
    // (asJObject(), asJArray(), etc.) which wouldn't be necessary if we did it in the concrete classes. Ultimately,
    // there's no really great way to do this in Java.

    if (this instanceof JObject) {
      return jobjectF.apply(asJObject());
    } else if (this instanceof JArray) {
      return jarrayF.apply(asJArray());
    } else if (this instanceof JString) {
      return jstringF.apply(asJString());
    } else if (this instanceof JNumber) {
      return jnumberF.apply(asJNumber());
    } else if (this instanceof JBoolean) {
      return jboolF.apply(asJBoolean());
    } else if (this instanceof JNull) {
      return jnullF.apply(asJNull());
    } else {
      // This will never actually happen, but we're being suitably paranoid.
      throw new RuntimeException("this should never happen! unexpected JSON value type: " + this.getClass().getName());
    }
  }

  /**
   * If you know that this Value is <b>definitely</b> a JObject, this method does the casting for you
   * in a nice, pipelined fashion. If the concrete type isn't JObject, this will throw a
   * runtime {@link ClassCastException}, so don't use this if you're not sure. If you think it
   * <b>might</b> be a JObject, then you might prefer the Option-variant, {@link #asOJObject()}.
   * If you have no idea what the concrete type might be and you need to handle each case differently,
   * then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default JObject asJObject() {
    return (JObject) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a JArray, this method does the casting for you
   * in a nice, pipelined fashion. If the concrete type isn't JArray, this will throw a
   * runtime {@link ClassCastException}, so don't use this if you're not sure. If you think it
   * <b>might</b> be a JArray, then you might prefer the Option-variant, {@link #asOJArray()}.
   * If you have no idea what the concrete type might be and you need to handle each case differently,
   * then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default JArray asJArray() {
    return (JArray) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a JString, this method does the casting for you
   * in a nice, pipelined fashion. If the concrete type isn't JString, this will throw a
   * runtime {@link ClassCastException}, so don't use this if you're not sure. If you think it
   * <b>might</b> be a JString, then you might prefer the Option-variant, {@link #asOJString()}.
   * If you have no idea what the concrete type might be and you need to handle each case differently,
   * then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default JString asJString() {
    return (JString) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a JNumber, this method does the casting for you
   * in a nice, pipelined fashion. If the concrete type isn't JNumber, this will throw a
   * runtime {@link ClassCastException}, so don't use this if you're not sure. If you think it
   * <b>might</b> be a JNumber, then you might prefer the Option-variant, {@link #asOJNumber()}.
   * If you have no idea what the concrete type might be and you need to handle each case differently,
   * then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default JNumber asJNumber() {
    return (JNumber) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a JBoolean, this method does the casting for you
   * in a nice, pipelined fashion. If the concrete type isn't JBoolean, this will throw a
   * runtime {@link ClassCastException}, so don't use this if you're not sure. If you think it
   * <b>might</b> be a JBoolean, then you might prefer the Option-variant, {@link #asOJBoolean()}.
   * If you have no idea what the concrete type might be and you need to handle each case differently,
   * then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default JBoolean asJBoolean() {
    return (JBoolean) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a JNull, this method does the casting for you
   * in a nice, pipelined fashion. If the concrete type isn't JNull, this will throw a
   * runtime {@link ClassCastException}, so don't use this if you're not sure. If you think it
   * <b>might</b> be a JNull, then you might prefer the Option-variant, {@link #asOJNull()}.
   * If you have no idea what the concrete type might be and you need to handle each case differently,
   * then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default JNull asJNull() {
    return (JNull) this;
  }

  /**
   * If you think this Value <b>might</b> be a JObject, but you're not sure, then this will
   * return an Option.some() if it is, or Option.none() if the Value is some other concrete
   * type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default Option<JObject> asOJObject() {
    return (this instanceof JObject)
      ? Option.some((JObject) this)
      : Option.none();
  }

  /**
   * If you think this Value <b>might</b> be a JArray, but you're not sure, then this will
   * return an Option.some() if it is, or Option.none() if the Value is some other concrete
   * type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default Option<JArray> asOJArray() {
    return (this instanceof JArray)
      ? Option.some((JArray) this)
      : Option.none();
  }

  /**
   * If you think this Value <b>might</b> be a JString, but you're not sure, then this will
   * return an Option.some() if it is, or Option.none() if the Value is some other concrete
   * type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default Option<JString> asOJString() {
    return (this instanceof JString)
      ? Option.some((JString) this)
      : Option.none();
  }

  /**
   * If you think this Value <b>might</b> be a JNumber, but you're not sure, then this will
   * return an Option.some() if it is, or Option.none() if the Value is some other concrete
   * type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default Option<JNumber> asOJNumber() {
    return (this instanceof JNumber)
      ? Option.some((JNumber) this)
      : Option.none();
  }

  /**
   * If you think this Value <b>might</b> be a JBoolean, but you're not sure, then this will
   * return an Option.some() if it is, or Option.none() if the Value is some other concrete
   * type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default Option<JBoolean> asOJBoolean() {
    return (this instanceof JBoolean)
      ? Option.some((JBoolean) this)
      : Option.none();
  }

  /**
   * If you think this Value <b>might</b> be a JNull, but you're not sure, then this will
   * return an Option.some() if it is, or Option.none() if the Value is some other concrete
   * type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default Option<JNull> asOJNull() {
    return (this instanceof JNull)
      ? Option.some((JNull) this)
      : Option.none();
  }

  /**
   * Convert to a nicely indented JSON string.
   *
   * <p>You may alternatively wish to use the classic toString() method, which will
   * give you everything on the same line, without any pretty indentation.
   * @see Object#toString()
   */
  default String toIndentedString() {
    return toIndentedString("");
  }

  /**
   * Convert to a nicely indented JSON string with the given String prefix applied
   * before every line.
   *
   * <p>You may alternatively wish to use the classic toString() method, which will
   * give you everything on the same line, without any pretty indentation.
   * @see Object#toString()
   */
  default String toIndentedString(String prefix) {
    // Engineering note: This method is something that we expect some JSON production types to override
    // while others are perfectly happy with this default implementation.

    // In an ideal world, this wouldn't be part of the outward-facing interface of Value. We only really
    // want it as part of our recursive internal implementation.

    // Turns out, Java9 has a notion of a "private default method", which would exactly solve our problem, but
    // Java9 is only hitting its "feature complete" milestone in the summer of 2016, which is way too soon for
    // us to trust it in Comp215 for the fall of 2016. Maybe the year afterward? We'll see.

    // Further reading:
    // http://aruld.info/private-interface-methods-in-java-9/
    // http://openjdk.java.net/jeps/213

    // Hey, how come we're not prepending the prefix here? Because we're only doing that when
    // a newline is being introduced. If you look downward to the JObject and JArray versions
    // of this function, you'll see more interesting behavior.

    // Deeper thoughts: What's going on here is called "pretty printing", and it has a long and fun
    // history. Here's a paper from 1973 that talks about how the early LISP systems solved
    // the same problem:

    // http://www.softwarepreservation.org/projects/LISP/MIT/AIM-279-Goldstein-Pretty_Printing.pdf

    // If we tried to be more sophisticated about our pretty-printing, as this paper suggests, it
    // might look good for JSON arrays -- which are pretty much the same thing as LISP s-expressions,
    // but it wouldn't work nearly as well for JSON objects, where the key/value tuples really should
    // be stacked vertically. Since JSON can mix it up with objects and arrays, we'd have to work a lot
    // harder to come up with a good pretty printer that used less vertical space.

    return toString();
  }

  /**
   * Helper function to avoid saying <code>lens.get(Option.some(jvalue))</code>, when it
   * would be more convenient to just write <code>jvalue.lensGet(lens)</code>.
   * @see Operations#lensPath(String)
   * @see Lens#get(Object)
   */
  default Option<Value> lensGet(MonoLens<Option<Value>> lens) {
    return lens.get(Option.some(this));
  }

  /**
   * Helper function to avoid saying <code>lens.set(Option.some(jvalue), newValue)</code>, when
   * it would be more convenient to just write <code>jvalue.lensSet(lens, newValue)</code>.
   * @see Operations#lensPath(String)
   * @see Lens#set(Object, Object)
   */
  default Option<Value> lensSet(MonoLens<Option<Value>> lens, Option<Value> newValue) {
    return lens.set(Option.some(this), newValue);
  }

  /**
   * Helper function to avoid saying <code>lens.update(Option.some(jvalue), modFunc)</code>, when
   * it would be more convenient to just write <code>jvalue.lensUpdate(lens, modFunc)</code>.
   * @see Operations#lensPath(String)
   * @see Lens#update(Object, UnaryOperator)
   */
  default Option<Value> lensUpdate(MonoLens<Option<Value>> lens, UnaryOperator<Option<Value>> mod) {
    return lens.update(Option.some(this), mod);
  }

  /**
   * A JObject is a set of key/value tuples, where the keys are strings and the values can be any
   * JSON value, including another object.
   */
  class JObject implements Value {
    // We're using Memo's to store these things because they're relatively expensive to compute -- O(n) --
    // and we're not going to need them for every use of JObject. This way, they'll only ever be computed
    // once, on demand, and then the result will be saved.
    private final Supplier<IMap<String, Value>> mapVal;
    private final Supplier<IList<KeyValue<String, Value>>> listVal;

    /**
     * This constructor is something you can use from edu.rice.json.Parser, but it's not intended for public use.
     */
    JObject(IList<JKeyValue> contents) {
      mapVal = Memo.make(() -> TreapMap.fromList(contents.map(JKeyValue::toKeyValue)));

      // we're constructing the listVal from the mapVal rather than directly from "contents" because this
      // normalizes it. otherwise, if the same key/value tuple occurred more than once in the input, it
      // would appear more than once in the output as well.
      listVal = Memo.make(() -> mapVal.get().toSortedList());
    }

    JObject(IMap<String, Value> newMapVal) {
      mapVal = Memo.make(() -> newMapVal);
      listVal = Memo.make(() -> mapVal.get().toSortedList());
    }

    static JObject fromList(IList<JKeyValue> contents) {
      return new JObject(contents);
    }

    /**
     * Replace a given key/value tuple in this object with some other key/value tuple. Note that the String
     * argument here is *unescaped* (i.e., it's a normal Java string). The function maps from the old value
     * to the new value. If there's no value already there, Option.none() will be passed as the argument
     * to updateFunc. If Option.none() is returned, that will be treated as an instruction to remove the
     * key/value (i.e., this is a useful way to remove a key as well as to change it).
     */
    public Option<Value> updateKeyValue(String key, UnaryOperator<Option<Value>> updateFunc) {
      return Option.some(new JObject(mapVal.get().update(key, updateFunc)));
    }

    /**
     * Looks for the key in the JObject. If it's there, the corresponding Value is returned.
     */
    public Option<Value> oget(String key) {
      return getMap().oget(key);
    }

    /**
     * Converts from the internal representation to a nice IMap of the key/value pairs stored
     * within. Each will be an Value.
     */
    public IMap<String, Value> getMap() {
      return mapVal.get();
    }

    /**
     * Looks for any keys matching the predicate and returns those KeyValue tuples in a list.
     */
    public IList<KeyValue<String, Value>> getMatching(Predicate<String> keyPredicate) {
      return getContents().filter(kv -> keyPredicate.test(kv.getKey()));
    }

    /**
     * Converts from the internal representation to a list of KeyValues, suitable for all sorts of
     * general-purpose processing (note that this is edu.rice.list.KeyValue, not
     * edu.rice.json.Parser.JKeyValue).
     *
     * @see KeyValue
     */
    public IList<KeyValue<String, Value>> getContents() {
      return listVal.get();
    }

    @Override
    public int hashCode() {
      return listVal.get().hashCode();
    }

    @Override
    public String toString() {
      return "{ " + listVal.get().map(kv -> JKeyValue.of(kv).toString()).join(", ") + " }";
    }

    @Override
    public String toIndentedString(String prefix) {
      String nextPrefix = prefix + "  ";
      return "{ "
          + listVal.get().map(kv -> JKeyValue.of(kv).toIndentedString(nextPrefix)).join(",\n" + nextPrefix)
          + " }";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JObject)) {
        return false;
      }

      JObject jObject = (JObject) o;

      return listVal.get().equals(jObject.getContents());
    }
  }

  /**
   * These are used internally for JSON "Objects", which consist of a list of key/value pairs.
   * @see JObject#getMap()
   */
  class JKeyValue {
    private final JString string;
    private final Value value;

    /**
     * Don't use this constructor. Use one of the of() methods instead.
     */
    private JKeyValue(JString string, Value value) {
      this.string = string;
      this.value = value;
    }

    static JKeyValue of(JString key, Value value) {
      return new JKeyValue(key, value);
    }

    static JKeyValue of(KeyValue<String, Value> kv) {
      return of(JString.fromUnescapedString(kv.getKey()), kv.getValue());
    }

    @Override
    public String toString() {
      return string + ": " + value.toString();
    }

    /**
     * Sometimes you want to convert from JKeyValue, which is in the guts of the parser, to an KeyValue,
     * which works nicely with other edu.rice classes like TreapMap. This helps you do that.
     */
    public KeyValue<String,Value> toKeyValue() {
      return KeyValue.make(string.toUnescapedString(), value);
    }

    /**
     * Used as part of the recursive conversion of JSON objects to indented strings.
     * @see Value#toIndentedString()
     */
    String toIndentedString(String prefix) {
      // we're recursively calling toIndentedLines(), but we're not changing the prefix because if the value
      // is an object or an array, they'll make the prefix larger on their own; we'd like the closing bracket
      // to line up with the beginning of our keyvalue tuple, and this is the way to do it.
      return string + ": " + value.toIndentedString(prefix);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JKeyValue)) {
        return false;
      }

      JKeyValue jKeyValue = (JKeyValue) o;

      return string.equals(jKeyValue.string) && value.equals(jKeyValue.value);
    }

    @Override
    public int hashCode() {
      return string.hashCode() * 31 + value.hashCode();
    }
  }

  /**
   * This internal class parses an array of values as stored in JSON square-brackets.
   */
  class JArray implements Value {
    private final IList<Value> valueList;
    private final int length;

    // We're memoizing this because it's O(n) to compute, and many users of JArray will never need it.
    // If/when we need it, we'll compute it once and remember the result thereafter.
    private final Supplier<IList<KeyValue<Integer, Value>>> kvMap;

    /**
     * This constructor is something you can use from edu.rice.json.Parser, but it's not intended for public use.
     */
    JArray(IList<Value> valueList) {
      this.valueList = valueList;
      this.length = valueList.length();

      // we're pairing up sequential integers with the elements of the array, so a JSON array
      // like ["hello", "rice", "owls"] becomes a map like: { 0 -> hello, 1 -> rice, 2 -> owls }
      this.kvMap = Memo.make(
          () -> getList().zip(LazyList.rangeInt(0, length), (val, num) -> KeyValue.make(num, val)));
    }

    /**
     * Returns a list of the Values in the JArray.
     */
    public IList<Value> getList() {
      return valueList;
    }

    /**
     * Returns a list of KeyValue tuples from integers to Values, where the integers represent the index of each
     * value in the array. Useful if you want to pretend the JArray is something like a JObject.
     */
    public IList<KeyValue<Integer,Value>> getKVList() {
      return kvMap.get();
    }

    /**
     * Returns a map from integers to Values, where the integers represent the index of each
     * value in the array. Useful if you want to pretend the JArray is something like a JObject.
     */
    public IMap<Integer,Value> getMap() {
      return TreapMap.fromList(kvMap.get());
    }

    static JArray fromList(IList<Value> valueList) {
      return new JArray(valueList);
    }

    /**
     * Returns the nth value in the array, if present.
     */
    public Option<Value> nth(int i) {
      return valueList.nth(i);
    }

    /**
     * Update the nth value in the array, if present with updateFunc applied to the original value.
     * If the replacement is empty, then the nth value of the array is removed.
     */
    public JArray updateNth(int n, Function<Value, Option<? extends Value>> updateFunc) {
      return new JArray(valueList.updateNth(n, updateFunc));
    }

    @Override
    public String toString() {
      return "[ " + valueList.join(", ") + " ]";
    }

    @Override
    public String toIndentedString(String prefix) {
      String nextPrefix = prefix + "  ";
      return "[ " + valueList.map(value -> value.toIndentedString(nextPrefix)).join(",\n" + nextPrefix) + " ]";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JArray)) {
        return false;
      }

      JArray jArray = (JArray) o;

      return valueList.equals(jArray.valueList);
    }

    @Override
    public int hashCode() {
      return valueList.hashCode();
    }
  }

  /**
   * This internal class deals with JSON strings. Note there are two separate ways of reading out
   * the results here: toString() and toUnescapedString(). The former gives you something suitable
   * for pretty-printing, including the surrounding quotation marks. The latter gives you something
   * suitable for internal use. Backslash-escapes and other such things are converted to their
   * proper meaning (or, as proper as {@link org.apache.commons.text.StringEscapeUtils#unescapeJson(String)}
   * know how to handle).
   */
  class JString implements Value, Comparable<JString> {
    /**
     * This string is in the JSON wire format, i.e., it potentially has escapes (backslashes) in it.
     * If you're starting from an "unescaped" String (i.e., a native Java string that might have
     * newlines or whatnot in it), then DON'T STORE THAT HERE.
     *
     * @see #fromEscapedString(String)
     * @see #fromUnescapedString(String)
     */
    private final String string;

    /**
     * This constructor is something you can use from edu.rice.json.Parser, but it's not intended for public use.
     * Instead, use {@link Builders#javaString(String)}, {@link Builders#jsonString(String)},
     * {@link #fromEscapedString(String)} or {@link #fromUnescapedString(String)} as appropriate.
     */
    JString(String string) {
      this.string = string;
    }

    /**
     * JSON has very particular rules about backslashes. This method takes as input a Java string.
     * The Java string is assumed to <b>already be unescaped</b>. For example, it might have actual
     * newline characters in it rather than a backslash followed by a 'n'.
     *
     * @param string
     *     a fully escaped string, such as you might naturally deal with in Java
     * @return a JString corresponding to the input
     */
    static JString fromUnescapedString(String string) {
      return new JString(escapeJson(string));
    }

    /**
     * JSON has very particular rules about backslashes. This method takes as input a JSON string.
     * The JSON string is assumed to have <b>escaped</b> backslashes in it, as raw JSON text might
     * have.
     *
     * @param string
     *     a raw JSON string, such as you might get from a text file on disk or from a network
     *     packet
     * @return a JString corresponding to the input
     */
    static JString fromEscapedString(@Pattern(Scanner.jsonStringPatternNoQuotes) String string) {
      // Engineering note: this is subtle. We're unescaping the raw JSON input, then escaping it
      // back. Why?
      // - we want to *normalize* JSON strings (see, e.g., the backslash/slash issue)
      // - normalized strings ensure that the JString.equals() method can just delegate to String.equals()
      // - we've decided that the internal string needs to be in normalized and escaped format, but the input
      //   we're getting here is *not* normalized, so therefore,
      // - a round-trip through the unescape/escape routines will ensure that we have the same
      //   final representation, no matter the input format
      return new JString(escapeJson(unescapeJson(string)));
    }

    /**
     * If you've read in a JSON string, perhaps as part of parsing some larger JSON object,
     * and you want to convert that JSON string back to a Java String, you've got two choices.
     * This method will give you "unescaped" output, which means for example that a newline
     * will be a single character. If you want a backslash followed by a "n", and quotation
     * marks around your string, such that it's something a JSON parser might be willing to
     * read back in, then you're going to want to use {@link #toString()} instead.
     */
    public String toUnescapedString() {
      return unescapeJson(string);
    }

    @Override
    public int compareTo(JString other) {
      return string.compareTo(other.string);
    }

    /**
     * If you're using the {@link Builders} methods to build JSON, and you want syntactically valid JSON
     * output, then this method, as with all toString() methods, will give you a fully valid JSON
     * representation of the string inside this JString.
     *
     * <p>If, on the other hand, you're extracting values from the JSON structure for use elsewhere
     * in your program, then you should be using {@link #toUnescapedString()}, which will give you
     * what you need, with all the JSON "escapes" handled correctly.
     */
    @Override
    public String toString() {
      return "\"" + string + "\"";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JString)) {
        return false;
      }

      JString jString = (JString) o;

      return string.equals(jString.string);
    }

    @Override
    public int hashCode() {
      return string.hashCode();
    }
  }

  /**
   * This is the internal class for JSON numbers. While the external representation in a JSON string may be an
   * integer, the internal representation is always a Java double.
   */
  class JNumber implements Value, Comparable<JNumber> {
    private final double number;

    /**
     * This constructor is something you can use from edu.rice.json.Parser, but it's not intended for public use.
     */
    JNumber(double number) {
      this.number = number;
    }

    /**
     * Gives you back the number inside the JNumber as a double.
     */
    public double get() {
      return number;
    }

    @Override
    public String toString() {
      String tmp = Double.toString(number);
      if (tmp.endsWith(".0")) {
        return tmp.substring(0, tmp.length() - 2); // chop off the .0 for integers
      } else {
        return tmp;
      }
    }

    @Override
    public int compareTo(JNumber other) {
      return Double.compare(number, other.number);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JNumber)) {
        return false;
      }

      JNumber jNumber = (JNumber) o;

      return Double.compare(jNumber.number, number) == 0;

    }

    @Override
    public int hashCode() {
      return Double.hashCode(number);
    }
  }

  /**
   * This internal class handles the JSON 'true' and 'false' values, smashing them together into a
   * unified boolean.
   */
  class JBoolean implements Value {
    private final boolean bool;
    private static final JBoolean TRUE = new JBoolean(true);
    private static final JBoolean FALSE = new JBoolean(false);

    /**
     * Don't use this constructor. Use JBoolean.of() instead, as it will return one of the
     * singletons.
     */
    private JBoolean(boolean bool) {
      this.bool = bool;
    }

    static JBoolean of(boolean bool) {
      return (bool) ? TRUE : FALSE;
    }

    public boolean get() {
      return bool;
    }

    @Override
    public String toString() {
      return Boolean.toString(bool);
    }


    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
      // This code is fine because we're using singletons for JBoolean, so there is exactly
      // one instance for true and one for false.
      return this == o;
    }

    @Override
    public int hashCode() {
      return (bool ? 1 : 2);
    }
  }

  /**
   * This internal class handles the JSON 'null' value.
   */
  class JNull implements Value {
    private static final JNull SINGLETON = new JNull();

    private JNull() { }

    /**
     * This make method is something you can use from edu.rice.json.Parser, but it's not intended for public use.
     * (Use this rather than the constructor, because we want to have a singleton instance of JNull.)
     */
    static JNull make() {
      return SINGLETON;
    }

    @Override
    public String toString() {
      return "null";
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
      return this == o;
    }

    @Override
    public int hashCode() {
      return 1;
    }
  }
}
