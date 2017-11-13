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

import org.junit.Test;
import org.mockito.AdditionalAnswers;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class MemoizedFunctionTest {
  private final Function<Integer, Integer> incrementer = x -> x + 1;

  @Test
  public void makeBasics() throws Exception {
    final Function<Integer,Integer> memoizedIncrementer = MemoizedFunction.make(incrementer);
    assertEquals((Integer) 2, memoizedIncrementer.apply(1));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));
  }

  @Test
  public void makeOnlyOnce() throws Exception {
    // Engineering note: since we're testing a memoization system, we want to verify that the function
    // we're memoizing is only ever called once with any given argument. To do that, we're going to wrap
    // a mock around the real lambda that we're passing into the memoizer, and make a series of Mockito
    // verify() calls to make sure that the lambda is called at most once with any given argument.
    //
    // If we were doing this for a normal Java class instance, then we could just say spyFoo = spy(foo),
    // using spyFoo in place of foo for our tests. (spyFoo delegates every method call to the real foo.)
    //
    // Sadly, even the very latest version of Mockito doesn't support spying on lambdas, forcing us to
    // instead "mock" the Function class with this ungainly AdditionalAnswers.delegateTo() call.
    //
    // Wouldn't it be nicer to instead write: spyIncrementer = spy(x -> x + 1)?
    // https://github.com/mockito/mockito/issues/1068

    @SuppressWarnings("unchecked")
    final Function<Integer, Integer> spyIncrementer = mock(Function.class, AdditionalAnswers.delegatesTo(incrementer));
    final Function<Integer, Integer> memoizedIncrementer = MemoizedFunction.make(spyIncrementer);

    verify(spyIncrementer, never()).apply(1);
    verify(spyIncrementer, never()).apply(2);
    verify(spyIncrementer, never()).apply(3);
    verify(spyIncrementer, never()).apply(4);

    assertEquals((Integer) 2, memoizedIncrementer.apply(1));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));

    verify(spyIncrementer, atMost(1)).apply(1);
    verify(spyIncrementer, never()).apply(2);
    verify(spyIncrementer, never()).apply(3);
    verify(spyIncrementer, atMost(1)).apply(4);
  }

  private final BiFunction<Function<Long, Long>, Long, Long> fibonacci = (self, n) -> {
    // 1 1 2 3 5 8 13 ...
    if (n < 2) {
      return 1L;
    } else {
      return self.apply(n - 1) + self.apply(n - 2);
    }
  };


  @Test
  public void makeRecursive() throws Exception {
    final Function<Long, Long> memoFibonacci = MemoizedFunction.makeRecursive(fibonacci);

    assertEquals((Long) 13L, memoFibonacci.apply(6L));
  }

  @Test
  public void makeRecursiveOnlyOnce() throws Exception {
    // See engineering note above for discussion on the weird mock call here.

    @SuppressWarnings("unchecked")
    final BiFunction<Function<Long, Long>, Long, Long> spyFibonacci =
        mock(BiFunction.class, AdditionalAnswers.delegatesTo(fibonacci));

    final Function<Long, Long> memoFibonacci = MemoizedFunction.makeRecursive(spyFibonacci);

    verify(spyFibonacci, never()).apply(any(), any());

    assertEquals((Long) 13L, memoFibonacci.apply(6L));

    verify(spyFibonacci, atMost(1)).apply(any(), eq(0L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(1L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(2L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(3L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(4L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(5L));
  }

}