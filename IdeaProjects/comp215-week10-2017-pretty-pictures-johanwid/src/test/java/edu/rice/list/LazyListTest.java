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
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.function.Supplier;

import static edu.rice.list.LazyList.lazyConcat;
import static edu.rice.util.Performance.nanoBenchmark;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@SuppressWarnings("JdkObsolete")
@ParametersAreNonnullByDefault
@CheckReturnValue
public class LazyListTest {
  private static IList<Integer> nextLazyList(int i) {
    return LazyList.make(i, () -> nextLazyList(i + 1));
  }

  @Test
  public void testLength() throws Exception {
    IList<String> tlist = LazyList.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    assertEquals(tlist.length(), 5);
  }

  @Test
  public void testEmpty() throws Exception {
    IList<String> tlist = LazyList.of();

    assertEquals(tlist.empty(), true);
    assertEquals(tlist.length(), 0);
  }

  @Test
  public void testHead() throws Exception {
    IList<String> tlist = LazyList.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    assertEquals(tlist.head(), "Alice");
  }

  @Test
  public void testTail() throws Exception {
    IList<String> tlist = LazyList.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    assertEquals(tlist.tail().head(), "Bob");
    assertEquals(tlist.tail().tail().head(), "Charlie");
  }

  @Test
  public void testReverse() throws Exception {
    IList<String> tlist = LazyList.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    IList<String> tlistr = LazyList.of("Eve", "Dorothy", "Charlie", "Bob", "Alice");

    assertEquals(tlist.reverse().toString(), tlistr.toString());
    assertTrue(tlist.reverse().equals(tlistr));
  }

  @Test
  public void testEquals() throws Exception {
    IList<String> tlist = LazyList.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    IList<String> tlistPlus = LazyList.of("Zero", "Alice", "Bob", "Charlie", "Dorothy", "Eve");

    assertEquals(tlist, tlist);
    assertEquals(tlistPlus, tlist.add("Zero"));
    assertEquals(tlist.add("Zero"), tlistPlus);
  }

  @Test
  public void testToString() throws Exception {
    IList<String> tlist = LazyList.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");

    assertEquals(tlist.toString(), "List(\"Alice\", \"Bob\", \"Charlie\", \"Dorothy\", \"Eve\")");
  }

  @Test
  public void testMap() throws Exception {
    IList<String> emptyList = LazyList.makeEmpty();
    IList<String> list1 = emptyList.add("Alice").add("Bob").add("Charlie");
    IList<String> list2 = emptyList.add("alice").add("bob").add("charlie");

    assertTrue(list1.map(String::toLowerCase).equals(list2));
  }

  @Test
  public void testFlatMap() throws Exception {
    IList<String> list1 = LazyList.of("Alice", "Bob", "Charlie");
    IList<String> list2 = LazyList.of("1", "2", "3");

    // we're computing something of a cross-product here
    IList<String> result = list1.flatmap(name -> list2.map(number -> name + number));
    IList<String> resultCheck =
        LazyList.of("Alice1", "Alice2", "Alice3", "Bob1", "Bob2", "Bob3", "Charlie1", "Charlie2", "Charlie3");

    assertEquals(resultCheck, result);
  }

  @Test
  public void testFilter() throws Exception {
    IList<Integer> tlist = LazyList.of(1, 2, 3, 4, 5);
    IList<Integer> tlistEven = LazyList.of(2, 4);

    assertEquals(tlistEven, tlist.filter(x -> (x >> 1) << 1 == x));
  }

  @Test
  public void testConcat() throws Exception {
    IList<Integer> tlist1 = LazyList.of(1, 2, 3, 4, 5);
    IList<Integer> tlist2 = tlist1.concat(tlist1);

    assertEquals(List.of(1,2,3,4,5,1,2,3,4,5), tlist2);
  }

  @Test(expected = NullPointerException.class)
  public void testNullVarargs() throws Exception {
    // the null value should be rejected, causing an exception to be thrown
    final IList<String> names = LazyList.of("Alice", "Bob", null, "Charlie", "Dorothy");

    assertTrue(names.contains("Nobody")); // control flow will never get here
    fail();
  }

  /**
   * Every time you call get(), you get the next integer in sequence, starting with zero.
   */
  private static class SequentialIntSupplier implements Supplier<Integer> {
    private int counter = 0;

    @Override
    public Integer get() {
      return counter++;
    }
  }

  @Test
  public void testGenerator() throws Exception {
    IList<Integer> range1 = LazyList.rangeInt(0, 99);
    assertEquals(100, range1.length());

    IList<Integer> range2 = LazyList.iterate(0, x -> x + 1).limit(100);

    // three different tests that should all do the same exact thing
    assertTrue(range1.equals(range2));
    assertEquals(range1, range2);
    assertEquals(range1.toString(), range2.toString());

    IList<Integer> range3 = LazyList.generate(new SequentialIntSupplier()).limit(100);

    assertTrue(range1.equals(range3));

    IList<Integer> range4 = LazyList.rangeInt(0, 99);
    assertEquals(100, range4.length());
    assertEquals(50, range4.limit(50).length());
    assertEquals(100, range4.limit(200).length());

    assertEquals((Integer) 50, range4.nth(50).getOrElse(-1));

    // make sure we can go backwards as well
    IList<Integer> descending = LazyList.rangeInt(10, 0, -1);
    assertEquals(List.of(10,9,8,7,6,5,4,3,2,1,0), descending);

    // this will return an infinite list!
    IList<Integer> ones = LazyList.rangeInt(1, 1, 0);
    ones.limit(100).foreach(i -> assertEquals((Integer) 1, i));
  }

  @Test
  public void testSpeed() throws Exception {
    System.out.println("=========== List iteration performance =========== ");
    IList<Integer> range = LazyList.rangeInt(0, 1000);

    final Integer expectedSum = 500500; // Integer, instead of int, because otherwise the asserts have type errors

    System.out.println(String.format("Lazy list, 1K, first  iteration: %7.3f μs per item",
        1e-6 * nanoBenchmark(range::force)));
    System.out.println(String.format("Lazy list, 1K, second iteration: %7.3f μs per item",
        1e-6 * nanoBenchmark(range::force)));

    System.out.println(String.format("Lazy list, 1K, foldl:            %7.3f μs per item",
        1e-6 * nanoBenchmark(() -> assertEquals(expectedSum, range.foldl(0, (x, y) -> x + y)))));
    System.out.println(String.format("Lazy list, 1K, foldr:            %7.3f μs per item",
        1e-6 * nanoBenchmark(() -> assertEquals(expectedSum, range.foldr(0, (x, y) -> x + y)))));

    // we use foldr instead of foldl because foldl would reverse the list, which we don't want
    IList<Integer> eagerRange = range.foldr(List.makeEmpty(), (val, list) -> list.add(val));

    System.out.println(String.format("Eager list, 1K, foldl:           %7.3f μs per item",
        1e-6 * nanoBenchmark(() -> assertEquals(expectedSum, eagerRange.foldl(0, (x, y) -> x + y)))));
    System.out.println(String.format("Eager list, 1K, foldr:           %7.3f μs per item",
        1e-6 * nanoBenchmark(() -> assertEquals(expectedSum, eagerRange.foldr(0, (x, y) -> x + y)))));

    // this time, we'll use "streams" and Java's built-in LinkedList to do the same thing.
    LinkedList<Integer> llist = new LinkedList<>();
    range.foreach(llist::addFirst);

    System.out.println(String.format("Java stream, 1K, reduce:         %7.3f μs per item",
        1e-6 * nanoBenchmark(() -> assertEquals(expectedSum, llist.stream().reduce(0, (x, y) -> x + y)))));

    // and, lastly, we'll use an old-fashioned Java iterator and a mutating total value
    System.out.println(String.format("Java iterator, 1K:               %7.3f μs per item",
        1e-6 * nanoBenchmark(() -> {
          int sum = 0;
          for (int i : llist) {
            sum += i;
          }
          assertEquals(expectedSum, (Integer) sum);
        })));
  }

  @Test
  public void testConstructors() throws Exception {
    java.util.List<Integer> jlist = Arrays.asList(1, 2, 3, 4, 5);
    IList<Integer> list1 = LazyList.fromIterator(jlist.iterator());
    IList<Integer> list2 = LazyList.of(1, 2, 3, 4, 5);
    IList<Integer> list3 = LazyList.rangeInt(1, 5);

    assertEquals(list1, list2);
    assertEquals(list2, list3);
  }

  @Test
  public void testEnumerator() throws Exception {
    // Engineering note: A handful of old Java utility classes use Enumerator rather than Iterator,
    // but they're essentially the same idea. We just want to make sure that we work correctly with
    // Enumerator. And, yes, this needs to be Enumeration<Object> rather than
    // Enumeration<String>. Why? Because Enumeration *predates* when they added generics to Java.
    // Yes, really.

    Enumeration<Object> enumeration = new StringTokenizer("Hello Rice Owls!");
    IList<Object> list = LazyList.fromEnumeration(enumeration);
    assertEquals(List.of("Hello", "Rice", "Owls!"), list);
  }

  @Test
  public void testHandCodedInfiniteInts() throws Exception {
    IList<Integer> integerList = nextLazyList(0);

    assertEquals(List.of(0,1,2,3,4,5), integerList.limit(6));
  }

  @Test
  public void testOGenerate() throws Exception {
    Counter myCounter = new Counter(10);
    IList<Integer> counterList = LazyList.ogenerate(myCounter::next);
    assertEquals(List.of(10,9,8,7,6,5,4,3,2,1,0), counterList);
  }

  @Test
  public void testGenerateLazy() throws Exception {
    Counter myCounter = new Counter(10);
    IList<Integer> counterList = LazyList.ogenerate(myCounter::next).limit(6);
    assertEquals(List.of(10,9,8,7,6,5), counterList);
    assertEquals(4, myCounter.counter); // the number 4 shouldn't have been read out of counterList, because laziness
  }

  @Test
  public void testLaziness() throws Exception {
    // We'll use Mockito to create a mock lazy list, and we'll use that to verify that it's never been accessed.
    // What's Mockito? What's a mock? More on that coming in week5 or thereabouts.

    @SuppressWarnings("unchecked")
    Supplier<IList<String>> supplier = mock(Supplier.class);
    when(supplier.get()).thenReturn(LazyList.make("Eve")); // we'll be eavesdropping on whether this ever happens

    IList<String> lazy1 = LazyList.make("Dorothy", supplier);
    IList<String> lazy = LazyList.of("Alice", "Bob", "Charlie");
    IList<String> longerList = lazy.concat(lazy1);
    verify(supplier, never()).get(); // otherwise, the concat screwed something up
    IList<String> ucaseList = longerList.map(String::toUpperCase); // because of laziness, this shouldn't have gotten very far

    // okay, now we're going to read the elements from the lazy list in series
    assertEquals("ALICE", ucaseList.head());
    verify(supplier, never()).get();
    assertEquals("BOB", ucaseList.nth(1).get());
    verify(supplier, never()).get();
    assertEquals("CHARLIE", ucaseList.nth(2).get());
    verify(supplier, never()).get();
    assertEquals("DOROTHY", ucaseList.nth(3).get());
    verify(supplier, never()).get();
    assertEquals("EVE", ucaseList.nth(4).get());
    verify(supplier, atLeastOnce()).get();
  }

  @Test
  public void testLaziness2() throws Exception {
    // this version is roughly equivalent to the above, but does it without Mockito, to see how that might work

    Tripwire tripwire = new Tripwire();
    Supplier<IList<? extends String>> supplier = () -> {
      // Where Mockito lets us verify whether a mocked function was or wasn't called, here we're using mutation
      // to track whether or not this lambda was ever invoked.
      tripwire.tripped = true;
      return LazyList.make("Eve");
    };

    IList<String> lazy1 = LazyList.make("Dorothy", supplier);
    IList<String> lazy = LazyList.of("Alice", "Bob", "Charlie");
    IList<String> longerList = lazy.concat(lazy1);
    assertTrue(!tripwire.tripped);
    IList<String> ucaseList = longerList.map(String::toUpperCase); // because of laziness, this shouldn't have gotten far

    // okay, now we're going to read the elements from the lazy list in series
    assertEquals("ALICE", ucaseList.head());
    assertTrue(!tripwire.tripped);
    assertEquals("BOB", ucaseList.nth(1).get());
    assertTrue(!tripwire.tripped);
    assertEquals("CHARLIE", ucaseList.nth(2).get());
    assertTrue(!tripwire.tripped);
    assertEquals("DOROTHY", ucaseList.nth(3).get());
    assertTrue(!tripwire.tripped);
    assertEquals("EVE", ucaseList.nth(4).get());
    assertTrue(tripwire.tripped);
  }

  @Test
  public void testLazyConcat1() throws Exception {
    @SuppressWarnings("unchecked")
    Supplier<IList<Integer>> supplier = mock(Supplier.class);
    final int MAX = 1000000; // if we're not lazy, then this computation will run really slowly

    when(supplier.get()).thenReturn(LazyList.rangeInt(1, MAX));

    // a list of lists of integers from 1..i with our laziness tester at the very end
    IList<IList<Integer>> listsOfListsWithSupplier = LazyList.rangeInt(1, MAX)
        .map(i -> (i == MAX) ? supplier.get() : LazyList.rangeInt(1,i));

    // we're exercising the lazyConcat variant that takes a giant list of lists
    IList<Integer> flatListWithSupplier = lazyConcat(listsOfListsWithSupplier);

    verify(supplier, never()).get(); // laziness checks

    IList<Integer> flatList = LazyList.rangeInt(1, MAX).flatmap(i -> LazyList.rangeInt(1,i));

    assertEquals(flatList.limit(10), flatListWithSupplier.limit(10));
    verify(supplier, never()).get(); // laziness checks

    assertEquals(flatList.limit(1000), flatListWithSupplier.limit(1000));
    verify(supplier, never()).get(); // laziness checks

    assertEquals(flatList.limit(100000), flatListWithSupplier.limit(100000));
    verify(supplier, never()).get(); // laziness checks
  }

  @Test
  public void testLazyConcat2() throws Exception {
    @SuppressWarnings("unchecked")
    Supplier<IList<Integer>> supplier = mock(Supplier.class);
    final int MAX = 1000000; // if we're not lazy, then this computation will run really slowly

    when(supplier.get()).thenReturn(LazyList.rangeInt(1, MAX));

    // a list of lists of integers from 1..i with our laziness tester at the very end,
    // using the other lazyConcat variant, which takes a lambda supplier of the list at the end
    IList<Integer> flatListWithSupplier =
        LazyList.lazyConcat(LazyList.rangeInt(1, MAX - 1).flatmap(i -> LazyList.rangeInt(1, i)), supplier);

    verify(supplier, never()).get(); // laziness checks

    IList<Integer> flatList = LazyList.rangeInt(1, MAX).flatmap(i -> LazyList.rangeInt(1,i));

    assertEquals(flatList.limit(10), flatListWithSupplier.limit(10));
    verify(supplier, never()).get(); // laziness checks

    assertEquals(flatList.limit(1000), flatListWithSupplier.limit(1000));
    verify(supplier, never()).get(); // laziness checks

    assertEquals(flatList.limit(100000), flatListWithSupplier.limit(100000));
    verify(supplier, never()).get(); // laziness checks
  }

  @Test
  public void testUpdateNth() throws Exception {
    IList<String> strings = LazyList.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    IList<String> strings2 = strings.updateNth(2, x -> Option.some(x.toUpperCase()));
    assertEquals(LazyList.of("Alice", "Bob", "CHARLIE", "Dorothy", "Eve"), strings2);
    IList<String> strings3 = strings.updateNth(27, x -> Option.some(x.toUpperCase()));
    assertEquals(strings3, strings);
  }

  /**
   * Used as part of testing LazyList.ogenerate(). Initialize the counter with an integer,
   * and then every time you call next, you'll get back the counter, which will be internally
   * decremented until it would have been below zero, after which you'll get back Option.none()
   */
  static class Counter {
    int counter;

    Counter(int counter) {
      this.counter = counter;
    }

    Option<Integer> next() {
      if (counter < 0) {
        return Option.none();
      } else {
        return Option.some(counter--);
      }
    }
  }

  /**
   * Used to test laziness.
   */
  static class Tripwire {
    boolean tripped = false;
  }
}

