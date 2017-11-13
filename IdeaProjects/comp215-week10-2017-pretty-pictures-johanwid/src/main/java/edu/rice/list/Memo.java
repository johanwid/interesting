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
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

/**
 * Memoization support.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface Memo {
  /**
   * Given a supplier (i.e., a lambda that takes no arguments and returns a value), returns another supplier
   * which will call the given lambda exact once and then saves the result internally.
   *
   * @throws NullPointerException The supplier must return a non-null value, otherwise a NullPointerException
   *     will be thrown at runtime.
   */
  static <T> Supplier<T> make(Supplier<T> supplier) {
    return new MemoImpl<>(supplier);
  }

  // Engineering note: this is the only class, in the entirety of the Comp215 code library,
  // that stores 'null' values. We use this to make sure that we only call the supplier once,
  // and thereafter forget about it and just return the value that's saved in the contents
  // field. It's important to drop our reference to the supplier so it can be garbage
  // collected after we're done with it.

  // Of course, there are a ton of places where we're dealing with external Java libraries
  // that might happen to give us null values, often as a way of indicating an error. We
  // forbid that sort of thing inside Comp215, which means we have to be super careful
  // to make sure nulls don't creep in somewhere.

  class MemoImpl<T> implements Supplier<T> {
    @Nullable
    private T contents;
    @Nullable
    private Supplier<T> supplier;

    private MemoImpl(Supplier<T> supplier) {
      contents = null; // the cached result will eventually go here!
      this.supplier = supplier;
    }

    @Override
    public T get() {
      if (contents != null) {
        return contents;
      }

      // Advanced engineering notes: YOU'RE NOT REQUIRED TO UNDERSTAND ANY OF THIS.

      // We're fixing a concurrency problem we won't see until much later in the semester. We're
      // dealing with the unfortunate case when we might happen to have two separate
      // threads here at the exact same time; we only want to call the lambda exactly
      // once and save its result. This is just one example of how mutation can take
      // something seemingly straightforward like saving a result and turn it into an
      // occasional nightmare.

      // Note that we have "inconsistent synchronization" because we're reading "contents"
      // above without a lock while reading it later with a lock. This violates a common rule
      // for concurrent programming, namely that you always hold the same lock when accessing
      // a field that it protects. However we're okay here because once it's set it *never*
      // changes. Thus if it's non-null, then it's always safe to read and we can avoid a bunch
      // of work. Conversely, if it is null, then we only want to call the lambda exact once,
      // no matter how many concurrent threads are going on. If two threads both arrive here,
      // after finding null contents, this synchronized keyword assures that exactly one will
      // call the lambda. The other will patiently wait until the first is done.

      // You'll learn a lot more about multithreading and synchronization in later classes
      // like Comp321, Comp322, Comp421, and probably others. For now, what really matters
      // is that we want to invoke the supplier exactly once, no matter what, and we'll save
      // its results in contents.

      synchronized (this) {
        if (supplier != null) {
          contents = supplier.get();
          supplier = null;
        }

        if (contents == null) {
          throw new NullPointerException("memo's supplier returned null contents!");
        } else {
          return contents;
        }
      }
    }
  }
}
