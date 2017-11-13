/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.list;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

import static edu.rice.util.Strings.objectToEscapedString;

/**
 * General purpose interface for key-value classes, mapping keys to values. Unlike the Pair class, where the two
 * types are unconstrained, here the Key type must implement Comparable. This is required by
 * all of the {@link edu.rice.tree.IMap} and {@link edu.rice.tree.ISet} classes.
 *
 * <p><b>Warning: comparison of KeyValue types doesn't work how you might thing. This is
 * discussed in more detail in {@link KeyValue#compareTo(KeyValue)}.</b>
 *
 * @param <K> any comparable type
 * @param <V> any type
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface KeyValue<K extends Comparable<? super K>, V> extends Comparable<KeyValue<K, V>> {
  //
  // Data definition:
  //
  // A KeyValue is either a tuple of a key and a value or it's a key-with-no-value.
  // Implementation of the compareTo() method will only look at the keys, regardless
  // of which type of KeyValue we're using.
  //

  /**
   * This works much the same as the KeyValue constructor, which
   * ensures that our clients are always using the interface type.
   */
  static <K extends Comparable<? super K>, V> KeyValue<K, V> make(K key, V value) {
    return new Tuple<>(key, value);
  }

  /**
   * Special-purpose KeyValue variant for use when you don't actually have a value. This is something you might use when
   * searching for a KeyValue of a given key (using the compareTo method).
   */
  static <K extends Comparable<? super K>, V> KeyValue<K, V> makeNoValue(K key) {
    return new NoValue<>(key);
  }

  /**
   * Get the key from a KeyValue tuple.
   */
  K getKey();

  /**
   * Get the value from a KeyValue tuple.
   */
  V getValue();

  /**
   * General-purpose deconstructing matcher, takes a lambda with two arguments -- the key and value -- and returns
   * the result of calling that lambda on those two things. Note that this will fail if the KeyValue doesn't have
   * a value within.
   * @see #makeNoValue(Comparable)
   */
  default <T> T match(BiFunction<? super K, ? super V, ? extends T> func) {
    return func.apply(getKey(), getValue());
  }

  /**
   * Compare to another KeyValue tuple.
   *
   * <p><b>Warning: comparison of KeyValue types doesn't work how you might thing.</b>
   * Says the documentation for Comparable: It is strongly recommended, but not strictly required that
   * (x.compareTo(y)==0) == (x.equals(y)). Generally speaking, any class that implements
   * the Comparable interface and violates this condition should clearly indicate this
   * fact. The recommended language is "Note: this class has a natural ordering that
   * is inconsistent with equals."
   *
   * <p>This is subtle: For KeyValue, compareTo only looks at the keys, while equals looks at keys and values,
   * so compareTo returning 0 implies the keys are the same, but <i>says nothing about the values</i>.
   *
   * <p>a.equals(b) implies a.compareTo(b) == 0, but not the other way around.
   *
   * <p>This behavior makes it possible to build trees and other data structures with KeyValue
   * tuples as the internal nodes, going left or right on the basis of the compareTo function (which just
   * delegates to the key's compareTo function).
   *
   * @see #makeNoValue(Comparable) for creating a KeyValue with no Value inside, suitable for searching in trees
   */
  @Override
  default int compareTo(KeyValue<K, V> other) {
    return this.getKey().compareTo(other.getKey());
  }

  /**
   * General purpose KeyValue tuple class, mapping keys to values. Note that this class isn't public.
   * We expect clients to use {@link KeyValue#make(Comparable, Object)} instead.
   */
  class Tuple<K extends Comparable<? super K>, V> implements KeyValue<K,V> {
    private final K key;
    private final V value;

    /**
     * Create a new, immutable key/value tuple. Don't use this. Use KeyValue.make().
     *
     * @see KeyValue#make(Comparable, Object)
     */
    private Tuple(K key, V value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return value;
    }

    @Override
    public int hashCode() {
      return key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof KeyValue.Tuple<?,?>)) {
        return false;
      }

      // We don't know the concrete types of the other KeyValue tuple, thus the type wildcards
      // here. Nonetheless, the cast is safe because we know that it's an instance of KeyValue.
      // The recursive calls to equals() will sort out whether or not they're the same.

      Tuple<?, ?> other = (Tuple<?, ?>) o;
      return this.key.equals(other.key) && this.value.equals(other.value);
    }

    @Override
    public String toString() {
      return String.format("(%s => %s)", objectToEscapedString(key), objectToEscapedString(value));
    }
  }

  /**
   * Special-purpose KeyValue variant for use when you don't actually have a value. Note that this class
   * isn't public. We expect clients to use {@link KeyValue#makeNoValue(Comparable)} instead.
   */
  class NoValue<K extends Comparable<? super K>, V> implements KeyValue<K, V> {
    private final K key;

    /**
     * Create a new, immutable key/value tuple. Don't use this. Use KeyValue.makeNoValue()
     *
     * @see KeyValue#makeNoValue(Comparable)
     */
    private NoValue(K key) {
      this.key = key;
    }


    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      throw new NoSuchElementException("can't get a value from a KeyValue.NoValue instance");
    }

    @Override
    public int hashCode() {
      return key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof NoValue<?, ?>)) {
        return false;
      }

      // We don't know the concrete types of the other NoValue tuple, thus the type wildcards
      // here. Nonetheless, the cast is safe because we know that it's an instance of NoValue.
      // The recursive calls to equals() will sort out whether or not they're the same.

      NoValue<?, ?> other = (NoValue<?, ?>) o;
      return this.key.equals(other.key);
    }

    @Override
    public String toString() {
      return String.format("(%s => ∅)", objectToEscapedString(key));
    }
  }
}
