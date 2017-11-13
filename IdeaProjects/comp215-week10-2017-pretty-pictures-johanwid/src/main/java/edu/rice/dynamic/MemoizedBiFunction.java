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

import edu.rice.util.Pair;
import edu.rice.util.TriFunction;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * MemoizedBiFunction for use in dynamic programming. You build one of these with a lambda for the function you're
 * trying to evaluate, and it will store all pairs of input/output, so the lambda never get evaluated twice for
 * the same input.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface MemoizedBiFunction {
  // Engineering notes: Just like MemoizedFunction returns Functions, so the resulting instance types can
  // be used anywhere that a Function lambda might be expected, we do the same here for BiFunction (i.e., functions
  // taking two arguments as input rather than one).
  //
  // Internally, we're just using MemoizedFunction and we're wrapping the two arguments into a Pair, which keeps
  // everything relatively simple. This only works because Pair implements equals() and hashCode() correctly
  // over its contents. If it didn't, then you wouldn't be able to look up a pair as the key of a hashtable.
  //
  // The lambda expressions in the make methods here are a bit hairy to read, but from the outside, everything
  // works simply, as in the Pascal's triangle example in the Javadoc. If you want to understand these expressions,
  // try teasing them apart with variable declarations for the internal subexpressions. The code gets longer, but perhaps
  // a bit less mysterious.
  //
  // Does this seem too simple? Want a more complicated solution that uses more memory for no appreciable benefit?
  // Here you go! https://dzone.com/articles/java-8-automatic-memoization
  //
  // What they're doing is treating a BiFunction as if it's really two separate functions. The first function, applied
  // to the first argument, gives you a *second* function, which you then apply to the second argument. There's
  // a fancy name for this (https://en.wikipedia.org/wiki/Currying), and a beautiful theory behind it,
  // but we won't be getting into that in Comp215.

  /**
   * Given a lambda that maps from two inputs, returns a Memoized version of that lambda. If you need
   * recursion, use {@link #makeRecursive(TriFunction)}
   */
  static <T1, T2, R> BiFunction<T1, T2, R> make(BiFunction<T1, T2, R> function) {
    Function<Pair<T1, T2>, R> memoFunc = MemoizedFunction.make(pair -> function.apply(pair.a, pair.b));
    return (a, b) -> memoFunc.apply(new Pair<>(a, b));
  }

  /**
   * If you want to memoize a recursive function, then your function needs something to call besides itself.
   * So, rather than using a lambda that takes  two inputs, here we expect a lambda that takes <b>three</b> arguments,
   * the first of which will be something you can call when you want to be recursive. The latter two arguments are
   * the usual arguments to your function.
   *
   * <p>As an example, say you were using this to implement a memoized Pascal triangle function, you could define
   * that function like so:
   * <pre>
   * <code>
   *     BiFunction&lt;Integer,Integer,Long&gt; pascal = MemoizedBiFunction.makeRecursive((self, level, offset) -&gt; {
   *       if (offset == 0 || offset &gt;= level || offset &lt; 0 || level &lt; 0) {
   *         return 1L;
   *       } else {
   *         return self.apply(level - 1, offset) + self.apply(level - 1, offset - 1);
   *       }
   *     });
   * </code>
   * </pre>
   *
   * <p>Calls to <code>pascal</code> will run in O(n^2) time and use O(n^2) space, filling out the triangle without repeating.
   * Otherwise, a normal recursive implementation would take O(2^n) time and O(log n) space. Memoization is clearly the
   * preferred strategy. (And yes, it's possible to write down a closed-form solution to Pascal's triangle. You can see
   * that in our unit tests.)
   *
   * <p>More on Pascal's triangle: http://www.mathsisfun.com/pascals-triangle.html
   */
  static <T1, T2, R> BiFunction<T1, T2, R> makeRecursive(TriFunction<BiFunction<T1, T2, R>, T1, T2, R> function) {
    Function<Pair<T1, T2>, R> memoizedFunc =
        MemoizedFunction.makeRecursive((self, pair) -> function.apply((a, b) -> self.apply(new Pair<>(a, b)), pair.a, pair.b));
    return (a, b) -> memoizedFunc.apply(new Pair<>(a, b));
  }
}
