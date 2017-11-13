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
import edu.rice.util.Option;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * This interface describes a <b>mutating</b> priority queue, where the priorities come from the objects
 * being inserted. {@link BinaryHeap} contains the classic implementation of this interface.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface IPriorityQueue<T> {
  /**
   * Inserts a new element into the priority queue. <b>Warning: THIS IS A MUTATING OPERATION.</b>
   */
  void insert(T val);

  /**
   * Returns a list of the elements in sorted order. <b>Warning: THIS IS A MUTATING OPERATION.</b> When
   * this is complete, the priority queue will be empty. If you insert to the queue while also
   * iterating on the lazy list, the results will be undefined. This method is best used if
   * you've added everything you're ever going to add to the queue and you just want to get its
   * values out in sorted order and then discard the queue. Of course, the resulting list is
   * functional and will have all the usual properties of any functional list.
   *
   * <p>If you call {@link IList#force()} on the resulting lazy list, you will then be guaranteed that
   * the priority queue is empty.
   */
  default IList<T> drainToLazyList() {
    return LazyList.ogenerate(() ->
        empty()
            ? Option.none()
            : Option.some(getMin()));
  }

  /**
   * Returns the number of elements in the queue.
   */
  int size();

  /**
   * Returns whether the queue has elements or not.
   */
  default boolean empty() {
    return size() == 0;
  }

  /**
   * Returns the lowest-priority item in the queue, and removes it from the queue.
   * <b>Warning: THIS IS A MUTATING OPERATION.</b>
   */
  T getMin();

  /**
   * Checks to make sure that the internal structure of the priority queue is consistent.
   * Useful for unit tests.
   */
  boolean valid();
}
