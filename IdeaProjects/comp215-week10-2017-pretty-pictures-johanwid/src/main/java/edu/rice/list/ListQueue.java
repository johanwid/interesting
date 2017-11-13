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

import edu.rice.util.Option;
import edu.rice.util.Pair;
import edu.rice.util.Strings;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Use our functional linked list class to implement a functional queue.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface ListQueue<T> extends IQueue<T> {
  /**
   * Creates an empty list queue of the given type parameter.
   */
  static <T> IQueue<T> makeEmpty() {
    @SuppressWarnings("unchecked")
    IQueue<T> typedEmptyQueue = (IQueue<T>) Empty.SINGLETON;
    return typedEmptyQueue;
  }

  /**
   * Variadic helper function, creates a list-queue from the arguments given. The first argument will be at the front
   * of the queue.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  static <T> IQueue<T> of(@Nullable T... vals) {
    // vals, when we convert them to a list with fromArray, will be in the proper FIFO order,
    // so we'll put them in the outbox and start with an empty inbox.
    return new Queue<>(List.makeEmpty(), LazyList.fromArray(vals));
  }

  class Queue<T> implements ListQueue<T> {
    private final IList<T> inbox;
    private final IList<T> outbox;

    // Don't call this externally; use the "of" method or the makeEmpty() method instead.
    private Queue(IList<T> inbox, IList<T> outbox) {
      this.inbox = inbox;
      this.outbox = outbox;
    }

    @Override
    public int hashCode() {
      return toLazyList().hashCode(); // let the list's hash worry about it
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof IQueue)) {
        return false;
      }

      IQueue<?> otherQueue = (IQueue<?>) o;

      return otherQueue == this || toLazyList().equals(otherQueue.toLazyList());
    }

    @Override
    public String toString() {
      return "Queue(" + toLazyList().map(Strings::objectToEscapedString).join(", ") + ")";
    }

    @Override
    public IQueue<T> insert(T t) {
      return new Queue<>(inbox.add(t), outbox).fixup();
    }

    // call this whenever you make a new Queue; if the outbox has
    // nothing in it, then it will reverse the inbox and add that in
    // the outbox. This ensures a useful property:
    //
    // IF there's something to dequeue, THEN it will be the head of the outbox.
    //
    // and:
    //
    // IF the queue would be entirely empty, THEN the SINGLETON empty queue is returned
    //
    private IQueue<T> fixup() {
      return outbox.match(
          emptyOutbox -> inbox.match(
              emptyInbox -> ListQueue.makeEmpty(), // the queue is completely empty
              (head, tail) -> new Queue<>(List.makeEmpty(), inbox.reverse())),
          (head, tail) -> this); // there's still something in the outbox, so we do nothing
    }

    @Override
    public T head() {
      return outbox.head();
    }

    @Override
    public IQueue<T> tail() {
      return new Queue<>(inbox, outbox.tail()).fixup();
    }

    @Override
    public Option<Pair<T, IQueue<T>>> oget() {
      return Option.some(new Pair<>(head(), tail()));
    }

    @Override
    public int size() {
      return inbox.length() + outbox.length();
    }
  }

  /**
   * Empty queues, however implemented, have a lot of things in common. Since we're big believers in
   * not repeating ourselves, they'll all share this interface, and thus pick up all these default methods.
   *
   * @see IQueue
   */
  class Empty<T> implements ListQueue<T>, IQueue.Empty<T> {
    private static final IQueue<?> SINGLETON = new ListQueue.Empty<>();

    // external users, don't call this; use makeEmpty()
    private Empty() { }

    @Override
    public IQueue<T> insert(T newbie) {
      // Engineering note: while non-empty lists always bottom out with an empty-list, so therefore we have
      // to do some gymnastics to support type casting for empty-list singletons, we don't need to do anything
      // like that for functional queues. Instead, when you try to insert something into an empty functional
      // queue, we'll just return a non-empty one.

      return ListQueue.of(newbie);
    }

    @Override
    public String toString() {
      return "Queue()";
    }
  }
}
