/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week2lists;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.NoSuchElementException;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Interface for a functional list over generic types.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface GList<T> {
  // Data definition: a GList is one of two things:
  // - Cons: an element of type T, and another GList<T>
  // - Empty

  // Engineering notes: this week we're introducing a bunch of code
  // annotations (everything with an @ in front of it). Initially, just
  // ignore them when you read this code, since they don't actually change
  // anything about how the program runs. Their job is to help you find bugs
  // *before* your program runs. These annotations tell the Java compiler,
  // and the IntelliJ system, that there are certain things going on. For
  // example, when you say @Override on a method that's implementing
  // an interface, then the compiler will generate an error if there isn't
  // a method that you're overriding.
  //
  // When we write @ParametersAreNonnullByDefault
  // on the class, then the annotation system will try to yell at you,
  // at compile time, if it sees a possibility of a parameter being
  // null *anywhere* in the class, on any method.
  //
  // The @CheckReturnValue annotations are also very useful. They will catch
  // you if you call the function and don't use its result, because in
  // the world of functional programming, ignoring the result of a function
  // means you didn't need to call that function in the first place (and,
  // thus, your code is probably buggy). In particular, if you've got prior experience
  // with a mutating list library, and you just say list.add(newElement), assuming
  // that the contents of the list will change, the @CheckReturnValue annotation here will
  // produce an error and highlight the code for you, to remind you that you're
  // making the mistake. Bugs found early are bugs fixed early, and that's a win.

  /**
   * Create a new empty list of the given parameter type.
   */
  static <T> GList<T> makeEmpty() {
    @SuppressWarnings("unchecked")
    GList<T> castSingleton = (GList<T>) Empty.SINGLETON;
    return castSingleton;
  }

  /**
   * Returns the value of the first element in the list.
   */
  T head();

  /**
   * Returns a new list equal to the old list without its head() element. If the list
   * is empty, this will throw an exception.
   */
  GList<T> tail();

  /**
   * Returns a new list with the given value in the front.
   */
  default GList<T> add(T val) {
    return new Cons<>(val, this);
  }

  /**
   * Computes the number of elements in the list.
   */
  int length();

  /**
   * Returns whether the list is empty or not.
   */
  boolean empty();

  /**
   * Returns whether the value o is somewhere in the list.
   */
  boolean contains(T o);

  /**
   * Returns a new list equal to all the elements in the old list satisfying the predicate.
   */
  GList<T> filter(Predicate<T> predicate);

  /**
   * Returns a new list equal to the old list with the function applied to each value.
   */
  <R> GList<R> map(Function<T, R> f);

  /**
   * Returns a value of type T equal to the elements of the list applied in sequence to one another
   * with the given operator. This happens from left-to-right (i.e., from head() to tail()). The zero
   * value is used to the left of the list's head. If the list is empty, the zero value is returned.
   *
   * <p>Example, to join a list of strings together, you might write:
   * <br>
   * String result = listOfStrings.foldl("", (x,y)-&gt;x+y);
   * <br>
   * The lambda concatenates two strings, and the zero is the empty-string.
   */
  T foldl(T zero, BinaryOperator<T> operator);

  /**
   * Returns a value of type T equal to the elements of the list applied in sequence to one another
   * with the given operator. This happens from right-to-left (i.e., from tail() to head()). The zero
   * value is used to the right of the list's last non-empty value. If the list is empty, the zero value is returned.
   *
   * <p>Example, to join a list of strings together, you might write:
   * <br>
   * String result = listOfStrings.foldr("", (x,y)-&gt;x+y);
   * <br>
   * The lambda concatenates two strings, and the zero is the empty-string.
   */
  T foldr(T zero, BinaryOperator<T> operator);

  /**
   * Returns a new list equal to the "other" list concatenated at the end of "this" list.
   *
   * <p>Examples:
   * <br>
   * {1,2,3}.concat({4,5}) returns {1,2,3,4,5}
   * <br>
   * emptyList.concat({1,2}) returns {1,2}
   * <br>
   * {1,2}.concat(emptyList} returns {1,2}
   */
  GList<T> concat(GList<T> other);

  /**
   * Returns a new list equal to at most the first n elements of "this" list. If n &gt; length(),
   * then the returned list will be equal to "this" list. If n &lt;= 0, an empty list will be returned.
   */
  GList<T> limit(int n);

  /**
   * Returns a list of integers, beginning at start and continuing by increment until the headVal
   * would be outside of [start,end] (i.e., the inclusive range).
   *
   * <p>Examples:
   * <br>
   * rangeInt(1,5,1) returns {1,2,3,4,5}
   * <br>
   * rangeInt(1,5,2) returns {1,3,5}
   * <br>
   * rangeInt(5,1,-1) returns {5,4,3,2,1}
   *
   */
  static GList<Integer> rangeInt(int start, int end, int increment) {
    //    throw new RuntimeException("rangeInt not implemented yet");
    if (start < end) {
      return Internal.rangeIntHelper(start, start, end, increment);
    } else {
      return Internal.rangeIntHelper(start, end, start, increment);
    }
  }

  /**
   * Used internally for non-public static methods. Java9 allows for "private" methods
   * in interfaces, which would have made this much nicer, but they're not in Java8.
   * Java9 isn't quite ready for Comp215 to use in 2017, but we'll probably switch
   * in 2018.
   */
  class Internal {
    // do not construct this class!
    private Internal() { }

    private static GList<Integer> rangeIntHelper(int current, int min, int max, int increment) {
      if (current < min || current > max) {
        return makeEmpty();
      } else {
        return rangeIntHelper(current + increment, min, max, increment).add(current);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////
  // the methods below will be implemented as part of the week 2 project
  // Unit tests are in Week2ProjectTest.java
  //////////////////////////////////////////////////////////////////////

  /**
   * For lists of comparable types, it's useful to compute their "minimum" based on the
   * comparison function. This is a static method rather than a member method because not
   * all lists are lists over comparable headVal types. If the input is an empty list, the
   * default headVal should be returned.
   *
   * <p>Examples:
   * GList.minimum(0, {5,2,9,3,7}) returns 2
   * <br>
   * GList.minimum(0, emptyList) returns 0
   * <br>
   * GList.minimum("", {"Charlie", "Alice", "Bob"}) returns "Alice"
   */
  static <T extends Comparable<T>> T minimum(T defaultValue, GList<T> list) {
    if (list.empty()) {
      return defaultValue;
    } else {
      return list.tail().foldl(list.head(), (a, b) -> (a.compareTo(b) < 0) ? a : b);
    }
    //    throw new RuntimeException("minimum not implemented yet");
  }

  /**
   * For lists of comparable types, it's useful to compute their "maximum" based on the
   * comparison function. This is a static method rather than a member method because not
   * all lists are lists over comparable headVal types. If the input is an empty list, the
   * default headVal should be returned.
   *
   * <p>Examples:
   * GList.maximum(0, {5,2,9,3,7}) returns 9
   * <br>
   * GList.maximum(0, emptyList) returns 0
   * <br>
   * GList.maximum("", {"Charlie", "Alice", "Bob"}) returns "Charlie"
   */
  static <T extends Comparable<T>> T maximum(T defaultValue, GList<T> list) {
    if (list.empty()) {
      return defaultValue;
    } else {
      return list.tail().foldl(list.head(), (a, b) -> (a.compareTo(b) > 0) ? a : b);
    }
    //    throw new RuntimeException("maximum not implemented yet");
  }

  /**
   * For lists of numbers, it's useful to compute their average.
   * This is a static method rather than a member method because not
   * all types allow you to do arithmetic with them. If the list is
   * empty, the default headVal should be returned.
   *
   * <p>Examples:
   * <br>
   * GList.average(0, {5,2,9,3,7}) returns 5.2
   * <br>
   * GList.average(0, emptyList) returns 0
   */
  static double average(double defaultValue, GList<Double> list) {
    if (list.empty()) {
      return defaultValue;
    } else {
      return list.foldl(0.0, (a, b) -> a + b) / list.length();
    }
    //    throw new RuntimeException("average not implemented yet");
  }

  class Cons<T> implements GList<T> {
    private final T headVal;
    private final GList<T> tailVal;

    private Cons(T value, GList<T> tailList) {
      this.headVal = value;
      this.tailVal = tailList;
    }

    @Override
    public T head() {
      return headVal;
    }

    @Override
    public GList<T> tail() {
      return tailVal;
    }

    @Override
    public int length() {
      return 1 + tailVal.length();
    }

    @Override
    public boolean empty() {
      return false;
    }

    @Override
    public boolean contains(T o) {
      if (o == headVal) {
        return true; // if they're pointing to the exact same object
      } else if (o.equals(headVal)) {
        return true; // we found it
      }

      // we didn't find it, so let's look recursively
      return tail().contains(o);
    }

    @Override
    public GList<T> filter(Predicate<T> predicate) {
      if (predicate.test(headVal)) {
        return tailVal.filter(predicate).add(headVal);
      } else {
        return tailVal.filter(predicate);
      }
    }

    @Override
    public <R> GList<R> map(Function<T,R> f) {
      return tailVal.map(f).add(f.apply(headVal));
    }

    @Override
    public T foldl(T zero, BinaryOperator<T> operator) {
      GList<T> currentList = this;

      while (!currentList.empty()) {
        zero = operator.apply(zero, currentList.head());
        currentList = currentList.tail();
      }
      return zero;
    }

    @Override
    public T foldr(T zero, BinaryOperator<T> operator) {
      return operator.apply(headVal, tailVal.foldr(zero, operator));
    }

    @Override
    public String toString() {
      // this check isn't strictly necessary, but we don't want to add whitespace
      // between the last list element and the empty-list afterward
      if (tailVal.empty()) {
        return headVal.toString();
      } else {
        return headVal.toString() + " " + tailVal.toString();
      }
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }

      if (!(other instanceof GList<?>)) {
        return false;
      }

      GList<?> otherList = (GList<?>) other;
      return head().equals(otherList.head()) && tail().equals(otherList.tail());
    }

    @Override
    public int hashCode() {
      return headVal.hashCode() + tailVal.hashCode() * 31; // a hack, but better than nothing
    }

    //////////////////////////////////////////////////////////////////////
    // The methods below will be implemented as part of the week 2 project.
    // Unit tests are in Week2ProjectTest.java
    //////////////////////////////////////////////////////////////////////

    @Override
    public GList<T> concat(GList<T> other) {
      return tailVal.concat(other).add(headVal);
  //    throw new RuntimeException("concat not implemented yet");
    }

    @Override
    public GList<T> limit(int n) {
      if (n < 1) {
        return makeEmpty();
      } else {
        return tailVal.limit(n - 1).add(headVal);
      }
  //    throw new RuntimeException("limit not implemented yet");
    }

  }

  class Empty<T> implements GList<T> {
    private Empty() { }

    private static final GList<?> SINGLETON = new Empty<>();

    @Override
    public T head() {
      throw new NoSuchElementException("can't take head() of an empty list");
    }

    @Override
    public GList<T> tail() {
      throw new NoSuchElementException("can't take tail() of an empty list");
    }

    @Override
    public int length() {
      return 0;
    }

    @Override
    public boolean empty() {
      return true;
    }

    @Override
    public boolean contains(T t) {
      return false;
    }

    @Override
    public GList<T> filter(Predicate<T> predicate) {
      return makeEmpty();
    }

    @Override
    public GList<T> concat(GList<T> other) {
      return other;
//      throw new RuntimeException("concat not implemented yet");
    }

    @Override
    public GList<T> limit(int n) {
      return this;
//      throw new RuntimeException("limit not implemented yet");
    }

    @Override
    public <R> GList<R> map(Function<T,R> f) {
      return makeEmpty();
    }

    @Override
    public T foldl(T zero, BinaryOperator<T> operator) {
      return zero;
    }

    @Override
    public T foldr(T zero, BinaryOperator<T> operator) {
      return zero;
    }

    @Override
    public String toString() {
      return "";
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof GList<?> &&
          ((GList<?>) other).empty();
    }

    @Override
    public int hashCode() {
      return 1; // a hack, but better than nothing
    }
  }
}
