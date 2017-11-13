/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week1lists;

import java.util.NoSuchElementException;

/**
 * Interface for a functional list over generic types.
 */
public interface GList<T> {
  // Data definition: a GList is one of two things:
  // - Cons: an element of type T, and another GList<T>
  // - Empty

  // Engineering notes: Where did we get a goofy word like "Cons" to
  // represent these two things going together? Turns out, it's a historic
  // term that go all the way back to a particular IBM computer of the late
  // 1950's. "Cons", and other terms from that computer, are still widely used:
  //
  //     https://en.wikipedia.org/wiki/CAR_and_CDR
  //
  // In Comp215, we'll say "cons" often, but we won't be using "car" or "cdr".
  // For those, we'll instead say "head" and "tail", since those are more intuitive.
  // You may well bump into car/cdr at some point in your career, though, so you
  // can at least say you saw it here first.

  /**
   * Create a new empty list of the given parameter type.
   */
  static <T> GList<T> makeEmpty() {
    @SuppressWarnings("unchecked")
    GList<T> castSingleton = (GList<T>) Empty.SINGLETON;
    return castSingleton;
  }

  /**
   * Returns a new list with the given value in the front.
   */
  default GList<T> add(T value) {
    return new Cons<>(value, this);
  }

  /**
   * Returns the value of the first element in the list. Throws an
   * exception if the list is empty.
   */
  T head();

  /**
   * Returns a new list equal to the old list without its head() element.
   * Throws an exception if the list is empty.
   */
  GList<T> tail();

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
    public String toString() {
      // this check isn't strictly necessary, but we don't want to add whitespace
      // between the last list element and the empty-list afterward
      if (tailVal.empty()) {
        return headVal.toString();
      } else {
        return headVal.toString() + " " + tailVal.toString();
      }
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
    public String toString() {
      return "";
    }
  }
}
