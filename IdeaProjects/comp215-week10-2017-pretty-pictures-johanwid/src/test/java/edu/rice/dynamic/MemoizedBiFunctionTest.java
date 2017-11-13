package edu.rice.dynamic;

import edu.rice.util.TriFunction;
import org.junit.Test;
import org.mockito.AdditionalAnswers;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class MemoizedBiFunctionTest {
  private final BiFunction<Integer, Integer, Integer> adder = (x, y) -> x + y;

  @Test
  public void makeBasics() throws Exception {
    final BiFunction<Integer, Integer, Integer> memoizedAdder = MemoizedBiFunction.make(adder);
    assertEquals((Integer) 2, memoizedAdder.apply(1, 1));
    assertEquals((Integer) 5, memoizedAdder.apply(4, 1));
    assertEquals((Integer) 5, memoizedAdder.apply(4, 1));
  }

  @Test
  public void makeOnlyOnce() throws Exception {
    // See the engineering note in MemoizedFunctionTest, which discusses how and why we're calling mock() like this.

    @SuppressWarnings("unchecked")
    BiFunction<Integer, Integer, Integer> spyAdder = mock(BiFunction.class, AdditionalAnswers.delegatesTo(adder));

    final BiFunction<Integer, Integer, Integer> memoizedAdder = MemoizedBiFunction.make(spyAdder);

    verify(spyAdder, never()).apply(1, 1);
    verify(spyAdder, never()).apply(4, 1);
    verify(spyAdder, never()).apply(1, 4);
    verify(spyAdder, never()).apply(4, 4);

    assertEquals((Integer) 2, memoizedAdder.apply(1, 1));
    assertEquals((Integer) 5, memoizedAdder.apply(4, 1));
    assertEquals((Integer) 5, memoizedAdder.apply(4, 1));

    verify(spyAdder, atMost(1)).apply(1, 1);
    verify(spyAdder, atMost(1)).apply(4, 1);
    verify(spyAdder, never()).apply(1, 4);
    verify(spyAdder, never()).apply(4, 4);
  }

  /**
   * Pascal's triangle: https://en.wikipedia.org/wiki/Pascal%27s_triangle
   * <code><pre>
   * T[0,0] = 1
   * T[level, 0] = 1
   * T[level, level] = 1
   * T[level, offset] = T[level - 1, offset] + T[level - 1, offset - 1]
   * </pre></code>
   *
   * <p>pascalsTriangle is defined here in a style suitable for passing to {@link MemoizedBiFunction#makeRecursive(TriFunction)}
   * which then returns an ordinary {@link BiFunction}.
   */
  final TriFunction<BiFunction<Long, Long, Long>, Long, Long, Long> pascalsTriangle = (self, level, offset) -> {
    if (offset == 0 || offset >= level || offset < 0 || level < 0) {
      return 1L;
    } else {
      return self.apply(level - 1, offset) + self.apply(level - 1, offset - 1);
    }
  };

  private static long factorial(long n) {
    if (n == 0) {
      return 1;
    }

    long accumulator = 1;

    for (long i = 2; i <= n; i++) {
      accumulator *= i;
    }

    return accumulator;
  }

  /**
   * Computes n-choose-r = n! / (n-r)!r! -- the closed-form solution to any given part of Pascal's triangle.
   */
  private static long choose(long n, long r) {
    // Engineering note: We could optimize this in a variety of ways, since we're multiplying a bunch of things
    // in the numerator only to cancel them out from the denominator. Because we're only using this method to
    // test our Pascal's Triangle implementation, we're much more interested in correctness than in performance.
    // If you really did need a high-performance version of choose(), you'd still keep this one around for
    // unit testing, to make sure they get the same answer.

    // Curiously, one place they'd differ is when you have integer overflow. The numerator or denominator here
    // could get much larger than the resulting solution. If you went to the trouble of optimizing it, you'd
    // be less likely to have overflow, and thus have correct answers for bigger inputs.

    // If you *really* cared about that, you'd use java.math.BigInteger instead.

    return factorial(n) / (factorial(n - r) * factorial(r));
  }


  @Test
  public void testPascalsTriangle() throws Exception {
    BiFunction<Long, Long, Long> pascal = MemoizedBiFunction.makeRecursive(pascalsTriangle);

    for (long n = 0; n < 10; n++) {
      for (long r = 0; r <= n; r++) {
        assertEquals((Long) choose(n, r), pascal.apply(n, r));
      }
    }
  }

  @Test
  public void testMemoization() throws Exception {
    @SuppressWarnings("unchecked")
    final TriFunction<BiFunction<Long, Long, Long>, Long, Long, Long> spyPascal =
        mock(TriFunction.class, AdditionalAnswers.delegatesTo(pascalsTriangle));
    final BiFunction<Long, Long, Long> pascal = MemoizedBiFunction.makeRecursive(spyPascal);

    verify(spyPascal, never()).apply(any(), eq(0L), eq(0L));
    verify(spyPascal, never()).apply(any(), eq(0L), eq(1L));
    verify(spyPascal, never()).apply(any(), eq(1L), eq(0L));
    verify(spyPascal, never()).apply(any(), eq(1L), eq(1L));

    for (long n = 0; n < 10; n++) {
      for (long r = 0; r <= n; r++) {
        assertEquals((Long) choose(n, r), pascal.apply(n, r));
      }
    }

    verify(spyPascal, atMost(1)).apply(any(), eq(0L), eq(0L));
    verify(spyPascal, atMost(1)).apply(any(), eq(0L), eq(1L));
    verify(spyPascal, atMost(1)).apply(any(), eq(1L), eq(0L));
    verify(spyPascal, atMost(1)).apply(any(), eq(1L), eq(1L));
  }
}