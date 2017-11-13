package edu.rice.primes;

import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.util.Log;
import edu.rice.util.Performance;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;
import java.util.function.Supplier;

import static edu.rice.list.LazyList.iterate;
import static edu.rice.list.LazyList.rangeInt;
import static edu.rice.primes.Primes.*;

/**
 * This code runs our prime number generators, over and over, to ultimately produce a CSV chart of the
 * performance (nanoseconds per prime) and how that changes as the number of primes we're looking for grows.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class PrimeBenchmarking {
  private static final String TAG = "PrimeBenchmarking";

  // We're going to run these tests over and over, reporting the *best* result of N trials. If you're
  // doing performance profiling, you can probably set this to 1 or 2.
  static final int BEST_OF_N = 10;

  // If you want a run that takes a reasonable amount of time, keep it around 2 million. You can't go
  // any larger than Integer.MAX_VALUE. See the engineering notes below.
//  static final int MAX_N = 8000000;

  // This number, roughly 500 million, will cause the benchmark to run for many hours on a fast multi-core computer.
  static final int MAX_N = Integer.MAX_VALUE >> 2;

  // We're going for a log-log plot, so we're going to ramp up the sizes in an exponential fashion.
  // Note the extra logic to deal with integer overflow, which can happen with big values of MAX_N.
  static final IList<Integer> SIZES = iterate(100, x -> (x * 3) / 2).takeWhile(x -> x < MAX_N && x > 0);

  static double nanoSecsPerPrime(Function<Integer,IList<Integer>> func, int size) {
    // Note the use of force(): since we might be dealing with a lazy list, we want to measure the cost of
    // computing *all* of the primes in the list. This forces all of them to be computed while we're
    // doing the benchmarking.
    return Performance.nanoBenchmark(() -> func.apply(size).force()) / (double) size;
  }

  static double nanoSecsPerPrimeBestOfN(Function<Integer,IList<Integer>> func, int size) {
    return rangeInt(1,BEST_OF_N)
        .map(n -> nanoSecsPerPrime(func, size))
        .foldl(Double.MAX_VALUE, (a,b) -> a < b ? a : b); // take min size
  }

  static IList<Double> perfVsN(Function<Integer,IList<Integer>> func, int max) {
    return SIZES
        .takeWhile(x -> x < max)
        .map(size -> nanoSecsPerPrimeBestOfN(func, size));
  }

  static IList<KeyValue<String, Supplier<IList<Double>>>> perfPerFunc() {
    return IList.mapkv(FUNCS, funcName -> () -> {
      Log.i(TAG, () -> "starting " + funcName);
      return perfVsN(REGISTRY.oget(funcName).get(), // won't fail because we know everything is there
          MAX_FOR_FUNC.oget(funcName).getOrElse(Integer.MAX_VALUE));
    });
  }

  /**
   * Runs the benchmark suite, prints CSV suitable for reading into a spreadsheet.
   */
  public static void main(String []args) {
    Log.i(TAG, "Prime number generator benchmarks");

    System.out.println("Size," + SIZES.join(","));

    final long runTime = Performance.nanoBenchmark(() ->
        perfPerFunc().foreach(kv ->
            System.out.println(kv.getKey() + "," +
                kv.getValue().get().map(n -> String.format("%.4f", n)).join(","))));

    System.out.println(String.format("Total runtime: %.3fs", 1e-9 * runTime));
  }
}

