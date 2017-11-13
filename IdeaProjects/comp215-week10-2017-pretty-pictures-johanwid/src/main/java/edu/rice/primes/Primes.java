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
import edu.rice.list.KeyValue;
import edu.rice.list.List;
import edu.rice.tree.IMap;
import edu.rice.tree.ISet;
import edu.rice.tree.TreapMap;
import edu.rice.tree.TreapSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;
import java.util.stream.IntStream;

import static edu.rice.list.LazyList.rangeInt;
import static edu.rice.stream.Adapters.streamToEagerList;
import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

/**
 * These static functions give you lists of prime numbers. They're useful both for testing list laziness (which several
 * of them rely on for performance) and for general-purpose performance observations.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class Primes {
  // Here are all the prime-number generation functions that we support, in a convenient registry
  static final IMap<String, Function<Integer,IList<Integer>>> REGISTRY = TreapMap.of(
      KeyValue.make("O(n sqrt n)", Primes::primesSimple),
      KeyValue.make("O(n sqrt n) PARALLEL", Primes::primesSimpleParallel),
      KeyValue.make("O(n sqrt n) PARALLEL2", Primes::primesSimpleParallel2),
      KeyValue.make("O(n sqrt n / log n) PARALLEL", Primes::primesTwoPhaseParallel),
      KeyValue.make("O(n log log n)", Primes::primesEratosthenes),
      KeyValue.make("O(n log log n) OPT", Primes::primesEratosthenesStorageOpt),
      KeyValue.make("O(n log n) NO LISTS", Primes::primesFasterStillNoLists),
      KeyValue.make("O(n log n) PARALLEL", Primes::primesFasterStillParallel),
      KeyValue.make("O(n log n) PARA NOLST", Primes::primesFasterStillParallelNoLists),
      KeyValue.make("O(n log n)", Primes::primesFasterStill),
      KeyValue.make("O(n log^2 n)", Primes::primesFaster),
      KeyValue.make("O(n^2 log n)", Primes::primesSlow) );

  // Some of these algorithms are slower than others. When doing benchmarking or "big" tests, this mapping
  // offers suggestions for problem sizes that you'd rather not exceed.
  static final IMap<String, Integer> MAX_FOR_FUNC = TreapMap.of(
      KeyValue.make("O(n log n) PARA NOLST", 20000000),
      KeyValue.make("O(n log n) NO LISTS",   20000000),
      KeyValue.make("O(n log n) PARALLEL",    5000000),
      KeyValue.make("O(n sqrt n)",            5000000),
      KeyValue.make("O(n log n)",              200000),
      KeyValue.make("O(n log^2 n)",             50000),
      KeyValue.make("O(n^2 log n)",              5000) );

  // In the order that we'd like to report benchmark results.
  static final IList<String> FUNCS = List.of(
      "O(n^2 log n)",
      "O(n log^2 n)",
      "O(n log n)",
      "O(n log n) PARALLEL",
      "O(n log n) NO LISTS",
      "O(n log n) PARA NOLST",
      "O(n sqrt n)",
//      "O(n sqrt n) PARALLEL",
      "O(n sqrt n) PARALLEL2",
      "O(n sqrt n / log n) PARALLEL",
      "O(n log log n)",
      "O(n log log n) OPT"
  );

  // Engineering notes: you may wonder why we're doing everything with Integer rather than Long values,
  // especially since we're dancing so close to Integer.MAX_VALUE. Turns out, Java doesn't support
  // array sizes larger than Integer.MAX_VALUE entries. You can't have a contiguous array bigger than
  // that! Yeah, sure, we could simulate it, probably most easily with an array of arrays and some
  // logic in front to do getters and setters. Our benchmark runs long enough, as-is, without needing
  // to work on larger problem-set sizes. Also, if we were really serious about performance, we'd
  // do bit-vectors rather than arrays of boolean. And if we were *really* serious, we'd consider
  // some form of *compressed* bitmaps, since as we get to larger primes, they're spaced out more.
  // That would cost more CPU time per query, to manage the bitmap, but we might get better memory
  // cache behavior, more than making up for the added CPU cost.

  // Further reading:
  //    https://github.com/RoaringBitmap/RoaringBitmap
  //    https://github.com/lemire/javaewah

  // Lastly, if you look at the runPrimeBenchmarking task in build.gradle, you'll see that it sets a
  // variety of unusual arguments for the Java virtual machine. If you do web searches on Java
  // performance tuning, you'll see many suggestions for flags that can modify the behavior of the
  // garbage collector. Try changing these arguments around and see if you can make the benchmarks
  // run any faster!


  // don't instantiate this class
  private Primes() { }

  /**
   * List of all primes from 1 to maxPrime. O(n sqrt n). Tests each number
   * individually for primality. Very simple.
   */
  public static IList<Integer> primesSimple(int maxPrime) {
    if (maxPrime < 2) {
      return List.of(1);
    }

    return rangeInt(1, maxPrime).filter(Primes::isPrime);
  }

  /**
   * Searches factors to determine if query is prime.
   * Runs in O(sqrt(n)) time.
   */
  public static boolean isPrime(int n) {
    if (n < 1) {
      return false;
    } else if (n < 4) {
      return true;
    } else if ((n & 1) == 0) {
      return false;
    }

    int max = (int) ceil(sqrt(n));

    for (int i = 3; i <= max; i += 2) {
      if (n % i == 0) {
        return false;
      }
    }

    return true;
  }

  /**
   * Searches factors to determine if query is prime.
   * Runs in O(sqrt(n)) time, but with parallelism.
   */
  public static boolean isPrimeParallel(int n) {
    if (n < 1) {
      return false;
    } else if (n < 4) {
      return true;
    } else if ((n & 1) == 0) {
      return false;
    }

    int max = (int) ceil(sqrt(n));

    // some inspiration here: https://dzone.com/articles/whats-wrong-java-8-part-vii

    return IntStream.rangeClosed(3, max) // numbers we're going to try
        .parallel()
        .filter(i -> n % i == 0) // if any of them divides evenly into n
        .noneMatch(x -> true); // if none match, then it must be prime
  }

  /**
   * List of all primes from 1 to maxPrime. O(n^2 log n)
   */
  public static IList<Integer> primesSlow(int maxPrime) {
    if (maxPrime < 2) {
      return List.of(1);
    }

    // We're only using the numbers less than sqrt(max) because for any composite number in [1,maxPrime], there
    // will exist a factor less than or equal to sqrt(max). Note that we're taking the "ceiling" of the sqrt
    // rather than the "floor" out of a sense of paranoia. Example: what if maxPrime was 25, and sqrt(25) gave
    // us 4.999999? Safer to round up than down.
    int maxFactor = (int) ceil(sqrt(maxPrime));

    IList<Integer> numRange = rangeInt(2, maxPrime); // list of integers from 2..maxPrime
    IList<Integer> outerProduct = rangeInt(2, maxFactor)
        .flatmap(i ->
            numRange.map(j -> i * j)
                .takeWhile(n -> n <= maxPrime));

    // Engineering notes: it's essential that we use lazy lists rather than eager lists, which ensures that
    // the takeWhile() part will terminate our search once the products we're looking at are greater than maxPrime.
    // If we did this with eager lists, we'd blow well past products > n, and waste a ton of work before the takeWhile()
    // threw away the stuff we didn't care about. That's a nice optimization.

    // The outer product here has runtime O(n sqrt n), without the takeWhile(), but it's going to be smaller than
    // that because the takeWhile() will terminate each search once the results are >N (and we don't need them).

    // How does this exactly improve the big-O complexity? Think about a number like 16. We could arrive with that by
    // computing 2*8 or 4*4, so composite numbers like 16 that can appear potentially multiple times in the list, so
    // long as one of the factors is less than sqrt(n). So then how big is the outerProduct list? It's certainly bounded
    // by O(n sqrt n). Now think about a number that's a power of two. How many ways are there to factor such a number
    // into a pair of composites? If the number is 2^n, then we have n factorizations. Stated slightly differently, the
    // number 2^n will appear in the outerProduct at most n times. This generalizes to other numbers, so we can also
    // derive an upper bound on outerProduct of O(n log n). log n is smaller than sqrt n, so O(n log n) wins.

    // Consequently, the code above is O(n log n) runtime, and O(n log n) list length.

    // The filter, below, is then n passes over the list above, thus the total run time of O(n^2 log n).

    return numRange.filter(i -> !outerProduct.contains(i)).add(1);
  }

  /**
   * List of all primes from 1 to maxPrime. O(n log^2 n)
   */
  public static IList<Integer> primesFaster(int maxPrime) {
    if (maxPrime < 2) {
      return List.of(1);
    }

    int maxFactor = (int) ceil(sqrt(maxPrime));

    IList<Integer> numRange = rangeInt(2, maxPrime); // list of integers from 2..maxPrime

    // Engineering note: the type annotations here are redundant for IntelliJ, but javac can't figure them out.
    //noinspection RedundantTypeArguments
    ISet<Integer> outerProduct =
        TreapSet.<Integer>fromList(
            rangeInt(2, maxFactor).flatmap(i ->
                numRange.map(j -> i * j) // list of all pairwise products of those ints
                    .takeWhile(n -> n <= maxPrime)));  // but don't bother collecting anything greater than the max value

    // Engineering notes: basically the same as above, but we also avoid the linear scans of the list, as above,
    // instead using more efficient TreapSet data structure.

    // Big-O, we're doing O(n log n) inserts, as above, but duplicates will be ignored, so the size of the set is just O(n),
    // but it's going to take O(log n) work for each insert. Set creation cost is O(n log^2 n).
    // Subsequently, we're doing N queries of cost O(log n).

    // Total runtime cost is O(n log^2 n)

    // Also, that TreapSet.<Integer>fromList() thing... IntelliJ claims that the "explicit type arguments can be inferred",
    // but if you leave it out, the Java compiler can't figure it out, even though IntelliJ can. We'd prefer
    // to leave it out, but we'll take a warning from IntelliJ over an error from Javac.
    // https://bugs.openjdk.java.net/browse/JDK-8154501

    return numRange.filter(i -> !outerProduct.contains(i)).add(1); // each contains() query is O(log n)
  }

  /**
   * List of all primes from 1 to maxPrime. O(n log n). Feel the power of mutation!
   */
  public static IList<Integer> primesFasterStillNoLists(int maxPrime) {
    if (maxPrime < 2) {
      return List.of(1);
    }

    int maxFactor = (int) ceil(sqrt(maxPrime));

    boolean[] notPrimes = new boolean[maxPrime + 1]; // start off false

    for (int i = 2; i <= maxFactor; i++) {
      for (int j = 2; j <= maxPrime; j++) {
        int product = i * j;
        if (product > maxPrime) {
          break; // breaks the j-loop, continues the i-loop
        }
        notPrimes[product] = true;
      }
    }

    return rangeInt(1, maxPrime).filter(i -> !notPrimes[i]);
  }

  /**
   * List of all primes from 1 to maxPrime. O(n log n). Feel the power of mutation!
   */
  public static IList<Integer> primesFasterStill(int maxPrime) {
    if (maxPrime < 2) {
      return List.of(1);
    }

    int maxFactor = (int) ceil(sqrt(maxPrime));

    IList<Integer> numRange = rangeInt(2, maxPrime); // lazy list of integers from 2..maxPrime
    IList<Integer> numFactor = rangeInt(2, maxFactor); // lazy list of integers from 2..maxFactor
    boolean[] notPrimes = new boolean[maxPrime + 1]; // these start off false

    numRange.flatmap(
        i -> numFactor.map(j -> i * j).takeWhile(product -> product <= maxPrime))
      .foreach(x -> notPrimes[x] = true);

    // Total runtime cost is O(n log n).

    return numRange.filter(i -> !notPrimes[i]).add(1); // these queries are now constant time, since they look in the array
  }

  /**
   * List of all primes from 1 to maxPrime. O(n log n). Feel the power of mutation and parallel streams!
   */
  public static IList<Integer> primesFasterStillParallel(int maxPrime) {
    if (maxPrime < 2) {
      return List.of(1);
    }

    int maxFactor = (int) ceil(sqrt(maxPrime));

    IList<Integer> numRange = rangeInt(2, maxPrime); // lazy list of integers from 2..maxPrime
    boolean[] notPrimes = new boolean[maxPrime + 1]; // initially false

    // Engineering note: you'd normally convert from an IList to a Stream doing something like so:
    //    Adapters.listToArrayStream(rangeInt(2, maxFactor), true)
    // But since we're going for maximum speed, we'll use the native streams way of doing it.
    // Note that we're only using streams for the outer loop here. We're doing the inner loop
    // sequentially, using our regular IList code, taking advantage of the .takeWhile() method,
    // which shaves off all the work greater than the maximum prime we'd otherwise do. A method
    // like .takeWhile() is only meaningful when you have an ordered list, and a parallel stream
    // isn't ordered, so there's no equivalent thing except for a .filter(), for which, despite the
    // parallelism, we'd still end up doing more work.

    IntStream.rangeClosed(2, maxFactor)
        .parallel() // we have to state this explicitly
        .forEach(i -> numRange // from here on down, we're operating on a lazy list of ints
            .map(j -> i * j)
            .takeWhile(n -> n <= maxPrime)
            .foreach(k -> notPrimes[k] = true));

    // Total runtime cost is still O(n log n), same as the original primesFasterStill.

    // No opportunity for parallelism when reading out the array, but this part is linear, so relatively fast.
    return numRange.filter(i -> !notPrimes[i]).add(1);
  }

  /**
   * List of all primes from 1 to maxPrime. O(n log n). Feel the power of mutation and parallel streams,
   * with this version carefully engineered to get rid of all the IList stuff that makes the previous
   * version slightly more readable.
   */
  public static IList<Integer> primesFasterStillParallelNoLists(int maxPrime) {
    if (maxPrime < 2) {
      return List.of(1);
    }

    int maxFactor = (int) ceil(sqrt(maxPrime));

    boolean[] notPrimes = new boolean[maxPrime + 1]; // initially false

    IntStream.rangeClosed(2, maxFactor)
        .parallel() // we have to state this explicitly
        .forEach((i -> {
          for (int j = 2; j <= maxPrime; j++) {
            int product = i * j;
            if (product > maxPrime) {
              break;
            }
            notPrimes[product] = true;
          }
        }));


    // Total runtime cost is still O(n log n), same as the original primesFasterStill.

    // No opportunity for parallelism when reading out the array, but this part is only linear.
    return rangeInt(1, maxPrime).filter(i -> !notPrimes[i]);
  }

  /**
   * List of all primes from 1 to maxPrime. O(n sqrt n) with parallelism.
   */
  public static IList<Integer> primesSimpleParallel(int maxPrime) {
    if (maxPrime < 2) {
      return List.of(1);
    }

    return rangeInt(1, maxPrime).filter(Primes::isPrimeParallel);
  }

  /**
   * List of all primes from 1 to maxPrime. O(n sqrt n) with parallelism.
   */
  public static IList<Integer> primesSimpleParallel2(int maxPrime) {
    if (maxPrime < 2) {
      return List.of(1);
    }

    // Engineering note: let's compare primesSimpleParallel vs. primesSimpleParallel2.
    // The former has a *sequential* list of numbers, each of which is tested for
    // primality using a *parallelized* test. The latter has a *parallel* list of
    // numbers, each of which is tested using a *sequential* test. We would expect
    // the latter design (i.e., the code right below here) to run radically faster
    // since the tasks being performed are relatively heavyweight and are completely
    // independent of one another. This is, indeed, what happens, which is why we're only
    // running the latter during our big benchmark runs. Feel free to play with the
    // settings and compare them yourself.

    return streamToEagerList(
        IntStream.rangeClosed(1, maxPrime)
          .parallel()
          .filter(Primes::isPrime)
          .boxed());
  }

  /**
   * List of all primes from 1 to maxPrime. O(n sqrt n) with parallelism + array output
   */
  public static int[] primesSimpleParallelArrayOutput(int maxPrime) {
    if (maxPrime < 2) {
      return new int[] {1};
    }

    return
        IntStream.rangeClosed(1, maxPrime)
            .parallel()
            .filter(Primes::isPrime)
            .toArray();
  }

  /**
   * Just like {@link #isPrime(int)}, but only considers the list of known
   * primes as possible factors to check.
   */
  private static boolean isPrimeKnownFactors(int n, int []knownPrimes) {
    if (n < 1) {
      return false;
    } else if (n == 1 || n == 2) {
      return true;
    }

    int max = (int) ceil(sqrt(n));

    // we're going to assume that knownPrimes starts with 1, so we need to skip that
    // and just test the rest.
    for (int i = 1; i < knownPrimes.length; i++) {
      int currentPrime = knownPrimes[i];
      if (currentPrime > max) {
        return true; // no point looking at anything bigger than sqrt(n)
      }

      if (n % currentPrime == 0) {
        return false;
      }
    }

    // we didn't find a factor, so it must be true
    return true;
  }

  /**
   * The fastest prime number sieve we've got: O((n sqrt n) / (log n)) with parallelism.
   */
  public static IList<Integer> primesTwoPhaseParallel(int maxPrime) {
    // We pre-compute all of the primes below sqrt(maxPrime) and then use that to speedup
    // the primality testing (via isPrimeKnownFactors()). This runs in time
    // O(n^{.75} + n * pi(sqrt n)), where pi(n) is the number of primes less than n.
    // According to the Riemann Hypothesis: pi(n) = O(n / log n). Simplified, this algorithm
    // then runs in O((n sqrt n) / (log n)), which is a non-trivial speedup relative to
    // primesSimpleParallel()'s O(n sqrt n).

    if (maxPrime < 2) {
      return List.of(1);
    }

    int maxFactor = (int) ceil(sqrt(maxPrime));

    int[] primesBelowMaxFactor = primesSimpleParallelArrayOutput(maxFactor);

    return rangeInt(0, primesBelowMaxFactor.length - 1)
        .map(i -> primesBelowMaxFactor[i]) // LazyList.fromArray won't work because it only does object-arrays, not int-arrays
        .concat(
            streamToEagerList(
                IntStream.rangeClosed(maxFactor + 1, maxPrime)
                    .parallel()
                    .filter(n -> isPrimeKnownFactors(n, primesBelowMaxFactor))
                    .boxed()));
  }

  /**
   * The classic Sieve of Eratosthenes. O(n log log n), written in a fully mutating style for
   * maximum speed (and minimum comprehensibility).
   */
  public static IList<Integer> primesEratosthenes(int maxPrime) {
    if (maxPrime < 2) {
      return List.of(1);
    }

    boolean[] notPrime = new boolean[maxPrime + 1]; // these start off initialized to false

    int maxFactor = (int) ceil(sqrt(maxPrime));

    // special case for 2, then standard case afterward
    for (int i = 4; i <= maxPrime; i += 2) {
      notPrime[i] = true;
    }

    for (int i = 3; i <= maxFactor; i += 2) {
      if (!notPrime[i]) {
        int skip = 2 * i; // optimization: odd + odd = even, so we can avoid half of the work
        for (int j = i * i; j <= maxPrime; j += skip) {
          notPrime[j] = true;
        }
      }
    }

    return rangeInt(1, maxPrime).filter(i -> !notPrime[i]);
  }

  /**
   * An improved Sieve of Eratosthenes. O(n log log n), written in a fully mutating style for
   * maximum speed (and minimum comprehensibility). Internally, we never store entries for
   * even numbers to save space, running noticeably faster as the internal memory usage grows.
   */
  public static IList<Integer> primesEratosthenesStorageOpt(int maxPrime) {
    if (maxPrime < 2) {
      return List.of(1);
    } else if (maxPrime < 3) {
      return List.of(1, 2);
    }

    // Engineering note: To keep this code as close to the original as possible, we're using the two
    // helper functions above, rather than trying to have those expressions appear inline, which
    // would be harder to debug. We're counting on the compiler to notice that it can do the inlining
    // for us. Note the absence of the first for-loop which sets half of the notPrime[] array to true?
    // We got rid of all those entries, so there's no need to initialize them!

    boolean[] notPrime = new boolean[intToIndex(maxPrime) + 1]; // these start off initialized to false

    int maxFactor = (int) ceil(sqrt(maxPrime));

    for (int i = 3; i <= maxFactor; i += 2) {
      if (!notPrime[intToIndex(i)]) {
        int skip = 2 * i; // optimization: odd + odd = even, so we can avoid half of the work
        for (int j = i * i; j <= maxPrime; j += skip) {
          notPrime[intToIndex(j)] = true;
        }
      }
    }

    return rangeInt(1, notPrime.length - 1)
        .filter(i -> !notPrime[i] && indexToInt(i) <= maxPrime)
        .map(Primes::indexToInt)
        .add(2).add(1);
  }

  // Helper functions for primesEratosthenesStorageOpt(). See engineering note, above.
  private static int intToIndex(int i) {
    return i >> 1;
  }

  private static int indexToInt(int index) {
    return index * 2 + 1;
  }
}
