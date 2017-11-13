/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.primes;

import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.tree.IMap;
import edu.rice.tree.TreapMap;
import edu.rice.util.Log;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

import static edu.rice.list.LazyList.rangeInt;
import static edu.rice.primes.Primes.REGISTRY;
import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

/**
 * Primality testers with various different runtime performance. These turn out to be an excellent
 * stress test on the lazy list system, because if it's not actually lazy, these computations will run
 * big-O slower. As you crank up the size of the search, you can also see how the big-O complexity
 * drives the wall-clock time for each algorithm. You may find the results surprising!
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class PrimalityTest {
  private static final String TAG = "PrimalityTest";

  static class Counter {
    public int counter = 0;

    public Counter() {
    }
  }

  @Rule
  public final ErrorCollector collector = new ErrorCollector();

  private void basicTests(String name, Function<Integer,IList<Integer>> primeFunc) {
    Log.i(TAG, () -> "checking " + name);
    collector.checkThat(name, List.of(1), equalTo(primeFunc.apply(1)));
    collector.checkThat(name, List.of(1,2), equalTo(primeFunc.apply(2)));
    collector.checkThat(name, List.of(1,2,3,5,7,11), equalTo(primeFunc.apply(12)));
    collector.checkThat(name, List.of(1,2,3,5,7,11,13,17,19,23), equalTo(primeFunc.apply(23)));
    collector.checkThat(name, List.of(1,2,3,5,7,11,13,17,19,23), equalTo(primeFunc.apply(25)));
    collector.checkThat(name, List.of(1,2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53), equalTo(primeFunc.apply(56)));
  }

  @Test
  public void basicTestsForAll() {
    REGISTRY.keys().foreach(name -> basicTests(name, REGISTRY.oget(name).get()));
  }

  @Test
  public void testLaziness() throws Exception {
    int maxPrime = 40000;
    Counter iterations = new Counter();

    int maxFactor = (int) ceil(sqrt(maxPrime));

    // Here's an excerpt from one of the O(n log n) versions that uses takeWhile(). We
    // want to ensure that only one value in each internal run through the loop
    // is greater than maxPrime, which will cause takeWhile() to truncate that list.
    // If the system were to have run the map() on its entire input first, then the
    // iteration counter would be much higher.

    // We could have done this without the Counter, instead using Mockito, but then
    // we wouldn't have the nice lexical closures of the lambda, wherein the inner lambda
    // can still see i from the outer lambda. A Mockito version would have been much uglier,
    // and worse, would be sufficiently *different* that a laziness issue in the "real" code
    // versus the "mock" code might be hard to spot.

    IList<Integer> outerProduct =
        rangeInt(2, maxFactor).flatmap(i ->
            rangeInt(2, maxPrime).map(j -> {
              if (i * j > maxPrime) {
                // By making queries against the outerProduct lazy-list then looking
                // at this counter, we can see how much of the lazy-list was realized.
                iterations.counter++;
              }
              return i * j;
            }).takeWhile(n -> n <= maxPrime));

    assertEquals(0, iterations.counter); // initial conditions, nothing evaluated yet...

    // the first time through, i = 2, so once j > maxPrime/2, we should have bumped the counter by only one
    int desiredLength2 = (maxPrime / 2) + 10;
    assertEquals(desiredLength2, outerProduct.limit(desiredLength2).length());
    assertEquals(1, iterations.counter);

    // let's do it again for i=3, and we should bump the counter only one more time
    int desiredLength3 = (maxPrime / 3) + 10;
    assertEquals(desiredLength2 + desiredLength3, outerProduct.limit(desiredLength2 + desiredLength3).length());
    assertEquals(2, iterations.counter);

    // now, we're going to run the whole thing out, for which we should increment the counter one time per loop
    outerProduct.force();
    assertEquals(maxFactor - 1, iterations.counter);
  }

  @Test
  public void testBiggerPrimes() throws Exception {
    final int MAX = 5000;
    Log.i(TAG, "Testing bigger primes, max(" + MAX + ")");

    // Engineering note: if there's a failure that only happens with larger sizes, we want to induce that.
    // We're not concerned with *performance*, since that's evaluated in the PrimeBenchmarking class.

    IList<String> allFuncs = REGISTRY.keys();

    IMap<String, IList<Integer>> result =
        TreapMap.fromList(allFuncs, funcName -> REGISTRY.oget(funcName).get().apply(MAX));

    // Of all our prime number generators, this is the simplest and, thus, most likely to be correct.
    IList<Integer> reference = result.oget("O(n sqrt n)").get();

    allFuncs.foreach(funcName -> collector.checkThat(funcName, reference, equalTo(result.oget(funcName).get())));
  }
}
