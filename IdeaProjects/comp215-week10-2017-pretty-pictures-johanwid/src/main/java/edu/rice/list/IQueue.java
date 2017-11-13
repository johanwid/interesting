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

import edu.rice.util.Log;
import edu.rice.util.Option;
import edu.rice.util.Pair;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This is the interface for a *functional* FIFO (first in, first out) queue.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface IQueue<T> {
  /**
   * Adds the new element to the end of the queue.
   */
  IQueue<T> insert(T t);

  /**
   * Gets the head of the queue; throws an exception if empty.
   */
  T head();

  /**
   * Gets everything but the head of the queue; returns an empty queue if empty.
   */
  IQueue<T> tail();

  /**
   * Optional getter: returns the head of the queue, and the remainder without the head, or Option.none() if
   * it's an empty queue. You may prefer the structural pattern matching variant {@link IQueue#match(Function, BiFunction)}.
   */
  Option<Pair<T, IQueue<T>>> oget();

  /**
   * Returns how many elements are in the queue.
   */
  int size();

  /**
   * General-purpose deconstructing structural pattern matching on a queue.
   *
   * @param emptyFunc
   *     called if the queue is empty
   * @param nonEmptyFunc
   *     called if the queue is non-empty, gives the front element of the queue and a queue with the remainder
   * @param <Q>
   *     the return type of either emptyFunc or nonEmptyFunc
   * @return returns the value of whichever function matches
   */
  default <Q> Q match(Function<? super IQueue<T>, ? extends Q> emptyFunc,
                      BiFunction<? super T, ? super IQueue<T>, ? extends Q> nonEmptyFunc) {
    if (empty()) {
      return emptyFunc.apply(this);
    } else {
      return nonEmptyFunc.apply(head(), tail());
    }
  }

  /**
   * Returns whether or not there are any contents in the queue.
   */
  default boolean empty() {
    return false;
  }

  /**
   * Returns a lazy list that iterates over the queue in FIFO order.
   */
  default IList<T> toLazyList() {
    // As part of your week3 project, you need to make a LazyList from your Queue. You should write this
    // such that it can work with *any* implementation of IQueue, not just the ListQueue you're implementing
    // in week3. You'll be using LazyList.make() and/or LazyList.makeEmpty(). Also, because you're up here
    // in IQueue, all you can do is call other methods from IQueue. You can't (and shouldn't try to) see
    // anything specific to ListQueue.

    // throw new RuntimeException("toLazyList not implemented yet");
    return match(
        emptyQueue -> LazyList.makeEmpty(),
        (head, tail) -> LazyList.make(head, tail::toLazyList));
  }

  interface Empty<T> extends IQueue<T> {
    @Override
    default boolean empty() {
      return true;
    }

    @Override
    default T head() {
      Log.e("IQueue.Empty", "can't take head() of an empty queue");
      throw new NoSuchElementException("can't take head() of an empty queue");
    }

    @Override
    default IQueue<T> tail() {
      Log.e("IQueue.Empty", "can't take tail() of an empty queue");
      throw new NoSuchElementException("can't take tail() of an empty queue");
    }

    @Override
    default Option<Pair<T, IQueue<T>>> oget() {
      return Option.none();
    }

    @Override
    default int size() {
      return 0;
    }
  }
}
