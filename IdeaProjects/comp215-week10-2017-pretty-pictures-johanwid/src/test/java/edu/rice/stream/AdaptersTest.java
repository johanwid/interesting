/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.stream;

import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import edu.rice.tree.IMap;
import edu.rice.tree.ISet;
import edu.rice.tree.TreapMap;
import edu.rice.tree.TreapSet;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static edu.rice.stream.Adapters.*;
import static edu.rice.util.Performance.nanoBenchmarkVal;
import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class AdaptersTest {
  @Test
  public void testListToIterator() throws Exception {
    IList<String> list = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    Iterator<String> iterator = listToIterator(list);
    assertTrue(iterator.hasNext());
    assertEquals("Alice", iterator.next());
    assertEquals("Bob", iterator.next());
    assertEquals("Charlie", iterator.next());
    assertEquals("Dorothy", iterator.next());
    assertEquals("Eve", iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testListLaziness() throws Exception {
    Stream<Integer> evenStream = Stream.iterate(0, x -> x + 2);
    IList<Integer> evenList = LazyList.iterate(0, x -> x + 2);

    // if this test never completes, then the resulting list from streamToList was eager
    assertEquals(evenList.limit(20), streamToList(evenStream).limit(20));
  }

  @Test
  public void testSetToIterator() throws Exception {
    ISet<String> set = TreapSet.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    Iterator<String> iterator = setToIterator(set, false);
    assertTrue(iterator.hasNext());
    assertTrue(set.contains(iterator.next()));
    assertTrue(set.contains(iterator.next()));
    assertTrue(set.contains(iterator.next()));
    assertTrue(set.contains(iterator.next()));
    assertTrue(set.contains(iterator.next()));
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testMapToIterator() throws Exception {
    IMap<String, Integer> map = TreapMap.of(KeyValue.make("Alice", 10), KeyValue.make("Bob", 20));
    Iterator<KeyValue<String, Integer>> iterator = mapToIterator(map, false);

    int total = 0;
    assertTrue(iterator.hasNext());
    total += iterator.next().getValue();
    total += iterator.next().getValue();
    assertFalse(iterator.hasNext());
    assertTrue(total == 30);
  }

  @Test
  public void testMapToStream() throws Exception {
    final IMap<String, Integer> simpleMap = TreapMap.of(KeyValue.make("Alice", 10), KeyValue.make("Bob", 20));

    // multiply the value by two: dumb but whatever
    UnaryOperator<KeyValue<String,Integer>> squareValOp = kv -> KeyValue.make(kv.getKey(), kv.getValue() * 2);

    IMap<String,Integer> resultStreamMapScalar =
        streamToMap(mapToStream(simpleMap, false).map(squareValOp));
    IMap<String,Integer> resultStreamMapParallel =
        streamToMap(mapToStream(simpleMap, true).map(squareValOp));
    IMap<String,Integer> resultStreamMapParallelArray =
        streamToMap(listToArrayStream(simpleMap.toList(), true).map(squareValOp));
    IMap<String,Integer> resultNormalMap =
        TreapMap.fromList(simpleMap.toList().map(squareValOp));

    assertEquals(resultNormalMap, resultStreamMapScalar);
    assertEquals(resultNormalMap, resultStreamMapParallel);
    assertEquals(resultNormalMap, resultStreamMapParallelArray);
  }

  @Test
  public void testListToStream() throws Exception {
    IList<String> list = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");

    IList<String> result = streamToList(
        listToStream(list, false).map(String::toUpperCase));

    assertEquals(list.map(String::toUpperCase), result); // test that ordering is preserved for non-parallel streams
  }

  @Test
  public void testSetToStream() throws Exception {
    IList<String> list = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    IList<String> upperCaseList = list.map(String::toUpperCase);
    ISet<String> set = TreapSet.fromList(list);
    ISet<String> allCapsSet = TreapSet.fromList(upperCaseList);

    Stream<String> stream = setToStream(set, false).map(String::toUpperCase);
    ISet<String> result = streamToSet(stream);

    assertEquals(allCapsSet, result); // set contents should be the same
  }

  @Test
  public void testParallelSpeedup() throws Exception {
    System.out.println("Parallel speedup tests! Number of available CPUs: " + Runtime.getRuntime().availableProcessors());

    // The command below hypothetically lets you tell Java to use more threads; making it bigger doesn't help.
    // Making it smaller, however, definitely slows things down.

//        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "30");

    // Further reading on concurrency in Java
    // http://blog.takipi.com/forkjoin-framework-vs-parallel-streams-vs-executorservice-the-ultimate-benchmark/
    // http://stackoverflow.com/questions/21163108/custom-thread-pool-in-java-8-parallel-stream

    testParallel(10000,  1, true);
    testParallel(10000, 10, true);
    testParallel(1000, 100, true);
    testParallel(100, 1000, true);
  }

  private void testParallel(int listLength, int hashRepeats, boolean printThings) {
    //
    // Engineering note: we need an operation that's amazingly slow, such that when we run it in parallel we'll
    // be observing the speedup from parallel dispatch, rather than measuring the overheads associated with streams.
    // So what then? Applying SHA-256 and doing it over and over again? Yeah, that will be slow. The "hashRepeats"
    // variable lets us dial in the slowness.
    //
    Function<Integer, Long> slowFunction = val -> {
      MessageDigest md;

      try {
        //
        // We need a distinct instance of the message digest every time because there's a ton of internal
        // state that mutates as the digest is doing its work. If we had a common instance, we'd get weird
        // results. These things are *not* functional.
        //
        md = MessageDigest.getInstance("SHA-256");
      } catch (Throwable throwable) {
        fail("can't find hash function!");
        return 0L;
      }

      byte[] result = longToBytes(val);
      for (int i = 0; i < hashRepeats; i++) {
        result = md.digest(result);
      }
      return bytesToLong(result);
    };


    if (printThings) {
      System.out.println(
          String.format("=========== List vs. Stream performance (listLength = %d, hashRepeats = %d) =========== ",
              listLength, hashRepeats));
    }

    // first, we'll insert random numbers; performance should be similar
    Random random = new Random();
    IList<Integer> numberList = LazyList.generate(random::nextInt).limit(listLength); // one million random numbers
    numberList.force(); // causes the random number to be called 1M times, so it's not part of the timing

    final IMap<Integer, Long> result1 = nanoBenchmarkVal(
        () -> {
          IList<KeyValue<Integer, Long>> hashedValues = IList.mapkv(numberList, slowFunction);
          return TreapMap.fromList(hashedValues);
        })
        .match((time, result) -> {
          if (printThings) {
            System.out.println(String.format(" regular IList     : %7.3f μs per hash",
                time / (listLength * 1000.0)));
          }
          return result;
        });


    // Engineering notes: the following two calls (streamToList then TreapMap.fromList) can be collapsed together
    // with streamToMap in your own code. For testing purposes, we want to see the intermediate list to validate
    // its ordering.
    final long time2 = nanoBenchmarkVal(
        () -> {
          IList<KeyValue<Integer, Long>> hashedValuesStream = streamToList(
              listToStream(numberList, false).map(val -> KeyValue.make(val, slowFunction.apply(val))));
          return TreapMap.fromList(hashedValuesStream);
        })
        .match((time, result) -> {
          if (printThings) {
            System.out.println(String.format(" sequential stream : %7.3f μs per hash",
                time / (listLength * 1000.0)));
          }
          assertEquals(result1, result);
          return time;
        });

    final long time3 = nanoBenchmarkVal(
        () -> {
          IList<KeyValue<Integer, Long>> hashedValuesParallelStream = streamToList(
              listToStream(numberList, true).map(val -> KeyValue.make(val, slowFunction.apply(val))));
          return TreapMap.fromList(hashedValuesParallelStream);
        })
        .match((time, result) -> {
          if (printThings) {
            System.out.println(String.format(" parallel stream   : %7.3f μs per hash",
                time / (listLength * 1000.0)));
          }
          assertEquals(result1, result);
          return time;
        });

    //
    // and now, a version using ArrayList, which should be about as fast as these things can go
    //
    nanoBenchmarkVal(
        () -> {
          IList<KeyValue<Integer, Long>> hashedValuesParallelStreamArray =
              streamToList(listToArrayStream(numberList, true).map(val -> KeyValue.make(val, slowFunction.apply(val))));
          return TreapMap.fromList(hashedValuesParallelStreamArray);
        })
        .consume((time, result) -> {
          if (printThings) {
            System.out.println(String.format(" par-array stream  : %7.3f μs per hash",
                time / (listLength * 1000.0)));
            System.out.println(String.format("PARALLEL  STREAM SPEEDUP: %.3fx",
                (double) time2 / (double) time3));
            System.out.println(String.format("PAR-ARRAY STREAM SPEEDUP: %.3fx",
                (double) time2 / (double) time));
          }
          assertEquals(result1, result);
        });
  }

  private static byte[] longToBytes(long val) {
    byte[] result = new byte[8];
    int i = 0;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i] = (byte) (val & 0xff);

    return result;
  }

  private static long bytesToLong(byte[] bytes) {
    int val = 0;
    // inefficient, but who cares?
    for (int i = 0; i < 8; i++) {
      val = val | (bytes[i] & 0xFF);
      val = val << 8;
    }
    return val;
  }
}
