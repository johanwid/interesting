/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.dynamic;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * MemoizedFunction for use in dynamic programming. You build one of these with a lambda for the function you're
 * trying to evaluate, and it will store all pairs of input/output, so the lambda never get evaluated twice for
 * the same input.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface MemoizedFunction {
  // Engineering notes: MemoizedFunction just returns Functions so the resulting instance types can
  // be used anywhere that a lambda might be expected. Also, we're internally using java.util.HashMap.
  // In week4, we'll introduce an alternative, "TreapMap". The details below will make more sense then, but
  // anyway: We're using Java's HashMap for two reasons.
  //
  // One: fundamentally we're mutating the map and we never need to save an old version. There's no benefit to
  // using a functional map.
  //
  // Two: TreapMap requires its keys to be Comparable, which would be unnecessarily constraining. HashMap
  // only requires its keys to support equals() and hashCode(), which are part of java.lang.Object. This
  // means we can define MemoizedFunction without any type constraints on T.

  /**
   * Given a lambda that maps from inputs to outputs, returns a Memoized version of that lambda. If you need
   * recursion, use {@link #makeRecursive(BiFunction)} instead.
   */
  static <T, R> Function<T, R> make(Function<T, R> function) {
    return new Standard<>(function);
  }

  /**
   * If you want to memoize a recursive function, then your function needs something to call besides itself.
   * So, rather than using a lambda that maps input to output, here we expect a lambda that takes two arguments,
   * the first of which will be something you can call when you want to be recursive. The second argument is
   * the usual argument to your function.
   *
   * <p>As an example, say you were using this to implement a memoized Fibonacci function, you could define
   * that function like so:
   *
   * <pre>
   * <code>
   * Function&lt;Long, Long&gt; memoFibonacci =
   *     MemoizedFunction.makeRecursive((self, n) -&gt; {
   *
   *   if (n &lt; 2) {
   *     return 1L;
   *   } else {
   *     return self.apply(n - 1) + self.apply(n - 2);
   *   }
   * });
   * </code>
   * </pre>
   *
   * <p>Calls to <code>memoFibonacci</code> will run in linear time because the underlying recursive calls are
   * memoized, versus exponential time for a naïve Fibonacci implementation.
   *
   * <p>More on Leonardo Bigollo (a.k.a., Fibonacci): https://www.bbvaopenmind.com/en/fibonacci-and-his-magic-numbers/
   */
  static <T, R> Function<T, R> makeRecursive(BiFunction<Function<T, R>, T, R> function) {
    return new Recursive<>(function);
  }

  class Standard<T, R> implements Function<T, R> {
    private final Function<T, R> function;
    private final Map<T, R> map = new HashMap<>(); // yes, a mutating hashmap!
    // Note: saying it's "final" just means tha we only assign to the field in the constructor,
    // but doesn't say anything about whether the class mutates on the inside.

    private Standard(Function<T, R> function) {
      this.function = function;
    }

    @Override
    public R apply(T input) {
      return map.computeIfAbsent(input, function); // basically does everything we need, a standard feature of Java8's HashMap
    }
  }

  class Recursive<T, R> implements Function<T, R> {
    private final BiFunction<Function<T, R>, T, R> function;
    private final Map<T, R> map = new HashMap<>(); // yes, a mutating hashmap!

    private Recursive(BiFunction<Function<T, R>, T, R> function) {
      this.function = function;
    }

    @Override
    public R apply(T input) {
      // This is a little more complicated than the "Standard" version, above, because we need to pass along the "recursive"
      // function to the lambda. In this case, the recursive function is simply "this" (i.e., shorthand for x -> this.apply(x))

      return map.computeIfAbsent(input, funcIn -> function.apply(this, funcIn));
    }
  }
}
