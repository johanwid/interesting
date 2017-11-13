/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.tree;

import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.util.Strings;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;

/**
 * A standard binary-heap implementation of a mutating priority queue. Among other things, this provides
 * a relatively straightforward O(n log n) way of sorting a list of things. Just insert them here then
 * drain them out again in order.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class BinaryHeap<T> implements IPriorityQueue<T> {
  //
  // ArrayList comes from java.util.ArrayList, providing us with a mutating array-like structure with
  // constant-time insert and fetch, as well as automatically growing the array as necessary, so we
  // don't need to worry about that in our own code.
  //
  private final ArrayList<T> storage;

  private final BiPredicate<? super T, ? super T> lessThanFunc;
  private int elements;

  /**
   * This make-method lets you define a binary heap over any type at all, so long as you can define
   * lessThanFunction (e.g., (a,b) -&gt; a &lt; b) over the type to give it an ordering.
   */
  public static <T> IPriorityQueue<T> make(BiPredicate<? super T, ? super T> lessThanFunc) {
    return new BinaryHeap<>(lessThanFunc);
  }

  /**
   * Private constructor, forcing users to instead use the public make-method or BinaryHeap.of(),
   * both of which force the return type to be the IPriorityQueue interface.
   */
  private BinaryHeap(BiPredicate<? super T, ? super T> lessThanFunc) {
    storage = new ArrayList<>();
    elements = 0;
    this.lessThanFunc = lessThanFunc;
  }

  /**
   * This static function is a shorthand for building a heap by inserting the given values.
   * The given values are required to be Comparable; their internal compareTo function is used for ordering.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  public static <T extends Comparable<T>> IPriorityQueue<T> of(@Nullable T... array) {
    IPriorityQueue<T> result = new BinaryHeap<>((a, b) -> a.compareTo(b) < 0);

    LazyList.fromArray(array).foreach(result::insert); // mutation!

    return result;
  }

  @Override
  public void insert(T val) {
    elements++;
    if (storage.size() < elements) {
      storage.add(val);
    } else {
      storage.set(elements - 1, val);
    }

    // okay, new element stuck on the end, time to heapify!
    for (int index = elements - 1; index > 0; index = parent(index)) {
      T currentElem = storage.get(index);
      T parentElem = storage.get(parent(index));

      if (lessThanFunc.test(currentElem, parentElem)) {
        // we need to swap and continue onward
        storage.set(index, parentElem);
        storage.set(parent(index), currentElem);
      } else {
        // the heap property is now satisfied and we're done
        return;
      }
    }
  }

  @Override
  public int size() {
    return elements;
  }

  @Override
  public T getMin() {
    if (elements == 0) {
      throw new NoSuchElementException("can't call getMin() on an empty binary heap");
    }

    // this is the result that the user actually wants
    final T result = storage.get(0);

    // remove the last thing and stick it in front
    storage.set(0, storage.get(elements - 1));
    elements--;

    int index = 0;

    // and now, heapify!
    while (child1(index) < elements) {
      T currentElem = storage.get(index);
      T child1elem = storage.get(child1(index));

      if (child2(index) < elements) {
        // we have to decide whether child1 or child2 is smaller, and then compare
        // that to the current element
        T child2elem = storage.get(child2(index));

        if (lessThanFunc.test(child1elem, child2elem)) { // child1 is "less than" child2
          if (lessThanFunc.test(currentElem, child1elem)) {
            // current is smaller, so we're done
            break; // out of the while loop
          }

          // swap the two elements
          storage.set(index, child1elem);
          storage.set(child1(index), currentElem);

          // continue with child1
          index = child1(index);
        } else {
          if (lessThanFunc.test(currentElem, child2elem))  {
            // current is smaller, so we're done
            break; // out of the while loop
          }

          // swap the two elements
          storage.set(index, child2elem);
          storage.set(child2(index), currentElem);

          // continue with child2
          index = child2(index);
        }
      } else {
        // child2 doesn't exist, but we still have to compare child1 to the current element

        if (lessThanFunc.test(currentElem, child1elem)) {
          // current is smaller, so we're done
          break; // out of the while loop
        }

        // swap the two elements
        storage.set(index, child1elem);
        storage.set(child1(index), currentElem);

        // continue with child1
        index = child1(index);
      }
    }

    // at this point, we've properly heapified things, so we're ready to return the result
    return result;
  }

  private static int child1(int index) {
    return (index * 2) + 1;
  }

  private static int child2(int index) {
    return (index * 2) + 2;
  }

  private static int parent(int index) {
    return (index - 1) / 2;
  }

  /**
   * Returns whether or not the heap property is satisfied; useful for unit tests.
   */
  @Override
  public boolean valid() {
    boolean result = true;
    for (int i = 0; i < elements; i++) {
      T valParent = storage.get(i);
      if (child1(i) < elements) {
        T valChild1 = storage.get(child1(i));
        // either the parent is less than the child or they're equal
        result = result && (lessThanFunc.test(valParent, valChild1) || valParent.equals(valChild1));
      }
      if (child2(i) < elements) {
        T valChild2 = storage.get(child2(i));
        // either the parent is less than the child or they're equal
        result = result && (lessThanFunc.test(valParent, valChild2) || valParent.equals(valChild2));
      }
    }

    return result;
  }

  private IList<T> storageAsList() {
    return LazyList.rangeInt(0, elements - 1).map(storage::get);
  }

  @Override
  public String toString() {
    // this returns the heap in its *internal* order, not in sorted order
    return "PriorityQueue(" +
        storageAsList()
            .map(Strings::objectToEscapedString)
            .join(", ")
        + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BinaryHeap<?>)) {
      return false;
    }

    final BinaryHeap<?> oHeap = (BinaryHeap<?>) o;

    // Philosophical question: what does it mean to compare equality of two binary heaps? Should we
    // test whether they contain the same *set* of entries, regardless of order, or should we test
    // whether they contain the same entries in the same order? Since a BinaryHeap is not a Set, we'll
    // go with an ordering-specific solution.

    return storageAsList().equals(oHeap.storageAsList());
  }

  @Override
  public int hashCode() {
    return storageAsList().hashCode();
  }
}
