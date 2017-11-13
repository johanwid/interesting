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
import edu.rice.util.Pair;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.function.BinaryOperator;

import static org.junit.Assert.*;

@SuppressWarnings("JdkObsolete")
@ParametersAreNonnullByDefault
@CheckReturnValue
public class ListTest {
  @Test
  public void testLength() throws Exception {
    final IList<String> emptyList = List.makeEmpty();
    final IList<String> list = emptyList.add("Alice").add("Bob").add("Charlie");

    assertEquals(0, emptyList.length());
    assertEquals(3, list.length());
  }

  @Test
  public void testEmpty() throws Exception {
    final IList<String> emptyList = List.makeEmpty();
    final IList<String> list = emptyList.add("Alice").add("Bob").add("Charlie");
    final IList<String> anotherEmpty = List.of();

    assertTrue(emptyList.empty());
    assertFalse(list.empty());
    assertTrue(anotherEmpty.empty());
  }

  @Test
  public void testMake() throws Exception {
    final IList<String> list3 = List.of("Alice", "Bob", "Charlie");
    final IList<String> list2 = List.of("Bob", "Charlie");

    // two equivalent ways of doing the same thing
    assertEquals(list3, List.make("Alice", list2));
    assertEquals(list3, list2.add("Alice"));
  }

  @Test(expected = NoSuchElementException.class)
  public void testHead() throws Exception {
    final IList<String> emptyList = List.makeEmpty();
    final IList<String> list = emptyList.add("Alice").add("Bob").add("Charlie");
    assertEquals("Charlie", list.head());

    assertTrue(emptyList.empty());

    // should throw an exception, which we're expecting (see the @Test above)
    assertFalse(emptyList.head() != null);
    fail("Exception should have been thrown!");
  }

  @Test
  public void testTail() throws Exception {
    final IList<String> emptyList = List.makeEmpty();
    final IList<String> list = emptyList.add("Alice").add("Bob").add("Charlie");

    assertEquals("Bob", list.tail().head());
    assertEquals("Alice", list.tail().tail().head());
  }

  @Test
  public void testFromList() throws Exception {
    // java.util.List keeps internal state and mutates as we add things to it. This is a completely
    // different world from the functional behavior we're doing in edu.rice.list.*
    final java.util.List<String> javaList = new LinkedList<>();
    javaList.add("Alice");
    javaList.add("Bob");
    javaList.add("Charlie");

    final IList<String> list = List.fromList(javaList);

    assertEquals(3, list.length());
    assertEquals("Alice", list.head());
    assertEquals("Bob", list.tail().head());
    assertEquals("Charlie", list.tail().tail().head());
    assertTrue(list.tail().tail().tail().empty());

    final java.util.List<String> jlist = Arrays.asList("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final IList<String> ilist = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");

    assertEquals(ilist, List.fromList(jlist));
  }

  @Test
  public void testAdd() throws Exception {
    final IList<String> emptyList = List.makeEmpty();
    final IList<String> list = emptyList.add("Alice").add("Bob").add("Charlie");

    assertEquals(list.head(), "Charlie");
  }

  @Test
  public void testReverse() throws Exception {
    final IList<String> emptyList = List.makeEmpty();
    final IList<String> list = emptyList.add("Alice").add("Bob").add("Charlie");
    final IList<String> reverseList = list.reverse();

    final IList<String> reversedEmptyList = emptyList.reverse();
    assertTrue(reversedEmptyList.empty());

    assertEquals("Alice", reverseList.head());
    assertEquals("Bob", reverseList.tail().head());
    assertEquals("Charlie", reverseList.tail().tail().head());
  }

  @Test
  public void testEquals() throws Exception {
    final IList<String> emptyList = List.makeEmpty();
    final IList<String> list1 = emptyList.add("Alice").add("Bob").add("Charlie");
    final IList<String> list2 = emptyList.add("Alice").add("Bob").add("Charlie");
    final IList<String> list3 = emptyList.add("XAlice").add("Bob").add("Charlie");
    final IList<String> list4 = emptyList.add("XAlice").add("XBob").add("XCharlie");

    //noinspection EqualsWithItself
    assertTrue(emptyList.equals(emptyList));
    //noinspection EqualsWithItself
    assertTrue(list1.equals(list1));
    assertTrue(list1.equals(list2));
    assertTrue(list2.equals(list1));

    assertFalse(list1.equals(list3));
    assertFalse(list1.equals(list4));
    assertFalse(list3.equals(list4));

    assertFalse(list3.equals(list1));
    assertFalse(list4.equals(list1));
    assertFalse(list4.equals(list3));
  }

  @Test
  public void testNth() throws Exception {
    final IList<Integer> emptyList = List.makeEmpty();

    final Option<Integer> foo = emptyList.nth(27);
    assertTrue(foo.isNone());

    final Option<Integer> foo2 = emptyList.nth(0);
    assertTrue(foo2.isNone());

    final IList<Integer> list = emptyList
        .add(50)
        .add(40)
        .add(30)
        .add(20)
        .add(10);

    assertEquals((Integer) 10, list.head());
    assertEquals((Integer) 10, list.nth(0).getOrElse(-1));

    assertEquals((Integer) 20, list.tail().head());
    assertEquals((Integer) 20, list.nth(1).getOrElse(-1));

    assertEquals((Integer) 30, list.nth(2).getOrElse(-1));
    assertTrue(list.nth(7).isNone());
  }

  @Test
  public void testToString() throws Exception {
    final IList<String> emptyList = List.makeEmpty();
    final IList<String> list1 = emptyList.add("Alice").add("Bob").add("Charlie");

    assertEquals("List(\"Charlie\", \"Bob\", \"Alice\")", list1.toString());
  }

  @Test
  public void testMap() throws Exception {
    final IList<String> emptyList = List.makeEmpty();
    final IList<String> list1 = emptyList.add("Alice").add("Bob").add("Charlie");
    final IList<String> list2 = emptyList.add("alice").add("bob").add("charlie");

    assertTrue(list1.map(String::toLowerCase).equals(list2));
  }

  @Test
  public void testFlatMap() throws Exception {
    final IList<String> list1 = List.of("Alice", "Bob", "Charlie");
    final IList<String> list2 = List.of("1", "2", "3");

    // we're computing something of a cross-product here
    final IList<String> result = list1.flatmap(name -> list2.map((number) -> name + number));
    final IList<String> resultCheck =
        List.of("Alice1", "Alice2", "Alice3", "Bob1", "Bob2", "Bob3", "Charlie1", "Charlie2", "Charlie3");

    assertEquals(resultCheck, result);
  }

  @Test
  public void testOFlatMap() throws Exception {
    //
    // note to the reader of this test:
    //
    // it might seem silly to map a string to an Option like this, rather than just doing a filter() operation
    // on the list. We're just doing this to test oflatmap(). There will be plenty of times when you're constructing
    // a list of option results, and it's handy to do a map on the ones that are present and drop out the ones
    // that are absent. That's fundamentally what's going on here.
    //

    // we're going to do a filter that extracts the uppercase words and returns empty for the lower-case words
    final IList<String> list1 =
        List.of("Alice", "ant", "Bob", "baby", "bicycle", "Charlie", "chair", "cheese",
            "Dan", "dubious", "dance", "Eve", "everything");

    // first make sure we understand how we're checking if the letters are uppercase
    assertTrue(Character.isUpperCase('A'));
    assertTrue(Character.isUpperCase("Alice".charAt(0)));

    final IList<String> result =
        list1.oflatmap(name ->
            (!name.isEmpty() && Character.isUpperCase(name.charAt(0))) // if the first character is uppercase
                ? Option.some(name) // then return an option.some with the string
                : Option.none()); // otherwise return an option.none

    final IList<String> resultCheck = List.of("Alice", "Bob", "Charlie", "Dan", "Eve");

    assertEquals(resultCheck, result);

    // and, can we pull this off without oflatmap at all?
    final IList<Option<String>> olist = list1.map(name ->
        (!name.isEmpty() && Character.isUpperCase(name.charAt(0))) // if the first character is uppercase
            ? Option.some(name) // then return an option.some with the string
            : Option.none()); // otherwise return an option.none

    final IList<String> result2 = olist.flatmap(Option::toList);
    final IList<String> result3 = olist.oflatmap(x -> x);

    assertEquals(resultCheck, result2);
    assertEquals(resultCheck, result3);

    // Engineering note: if we rearranged the way we define Option a little bit, such that Option wasn't just
    // "kinda like a list with zero or one element" but was fully compatible with IList and go could anywhere
    // that IList goes, then oflatmap() would cease to be necessary, because we could then just write:

    // IList<String> result4 = olist.flatmap(x -> x);

    // You can see this happening in Vavr's Option type, which is also a "Value", which then has the full complement
    // of methods that formalize the "kinda like a list" relationship we're talking about.
    //   https://github.com/vavr-io/vavr/blob/master/vavr/src/main/java/io/vavr/Value.java
    //   https://github.com/vavr-io/vavr/blob/master/vavr/src/main/java/io/vavr/control/Option.java

    // In Vavr's version of this code, the type of this lambda, x -> x, would be Function<Option<String>, Value<String>>
  }

  @Test
  public void testFilter() throws Exception {
    final IList<Integer> list1 = List.of(5,4,3,2,1);
    final IList<Integer> listEven = List.of(4,2);

    assertEquals(listEven, list1.filter(x -> (x >> 1) << 1 == x));
  }

  @Test
  public void testJoin() throws Exception {
    final IList<Integer> empty = List.makeEmpty();
    final IList<Integer> list1 = empty.add(1).add(2).add(3).add(4).add(5);

    final BinaryOperator<String> joiner = (a,b) -> a + "," + b;

    assertEquals(",5,4,3,2,1", list1.map(Object::toString).foldl("", joiner));
    assertEquals("5,4,3,2,1,", list1.map(Object::toString).foldr("", joiner));
    assertEquals("5,4,3,2,1", list1.join(","));
  }

  @Test
  public void testConcat() throws Exception {
    final IList<Integer> list1 = List.of(5,4,3,2,1);
    final IList<Integer> list2 = list1.concat(list1);

    assertEquals(List.of(5,4,3,2,1,5,4,3,2,1), list2);
  }

  @Test
  public void testSublist() throws Exception {
    final IList<Integer> empty = List.makeEmpty();
    final IList<Integer> testList = List.rangeInt(100, 130);
    final IList<Integer> testListFront = List.rangeInt(100, 115);
    final IList<Integer> testListBack = List.rangeInt(116, 130);

    final IList<Integer> subListFront = testList.sublist(0, 15);
    final IList<Integer> subListBack = testList.sublist(16, 30);
    final IList<Integer> subListEmpty = testList.sublist(31, 36);

    assertEquals(testListFront, subListFront);
    assertEquals(testListBack, subListBack);
    assertEquals(empty, subListEmpty);
    assertEquals(testListFront, testList.sublist(-3, 15));
  }

  @Test
  public void testHashing() throws Exception {
    final IList<String> list1 = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final IList<String> list2 = List.of("Eve", "Alice", "Charlie", "Bob", "Dorothy");
    final IList<String> list3 = List.of("Bob", "Charlie", "Dorothy", "Eve");
    final IList<String> list4 = LazyList.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");

    assertTrue(list1.hashCode() != list2.hashCode());
    assertTrue(!list1.equals(list2));
    assertTrue(!list2.equals(list1));

    assertTrue(list3.add("Alice").hashCode() == list1.hashCode());
    assertTrue(list3.add("Alice").equals(list1));
    assertTrue(list1.equals(list3.add("Alice")));
    assertTrue(list1.hashCode() == list4.hashCode());
    assertTrue(list1.equals(list4));
    assertTrue(list4.equals(list1));
  }

  @Test
  public void testRangeInt() throws Exception {
    final IList<Integer> range4 = List.rangeInt(0, 99);
    assertEquals(100, range4.length());
    assertEquals(50, range4.limit(50).length());
    assertEquals(100, range4.limit(200).length());

    assertEquals((Integer) 50, range4.nth(50).getOrElse(-1));

    // make sure we can go backwards as well
    final IList<Integer> descending = List.rangeInt(10, 0, -1);
    assertEquals(List.of(10,9,8,7,6,5,4,3,2,1,0), descending);
  }

  @Test
  public void testZip() throws Exception {
    final IList<Integer> range1 = LazyList.rangeInt(0, 999);
    final IList<Integer> range2 = LazyList.rangeInt(1000, 1999);
    final IList<Pair<Integer, Integer>> joint = range1.zip(range2, Pair::new);
    joint.foreach(pair -> assertTrue(pair.a + 1000 == pair.b));
  }

  @Test
  public void testUpdateNth() throws Exception {
    final IList<String> strings = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final IList<String> strings2 = strings.updateNth(2, x -> Option.some(x.toUpperCase()));
    assertEquals(List.of("Alice", "Bob", "CHARLIE", "Dorothy", "Eve"), strings2);
    final IList<String> strings3 = strings.updateNth(27, x -> Option.some(x.toUpperCase()));
    assertEquals(strings3, strings);
  }

  @Test
  public void testSort() throws Exception {
    final IList<Integer> numbersInOrder = List.of(1,2,3,4,5,5,6,7,9);
    final IList<Integer> numbersBackwards = numbersInOrder.reverse();

    assertFalse(numbersBackwards.isSorted());
    assertTrue(numbersInOrder.isSorted());
  }

  @Test(expected = NullPointerException.class)
  public void testNullVarargs() throws Exception {
    // the null value should be rejected, causing an exception to be thrown
    final IList<String> names = List.of("Alice", "Bob", null, "Charlie", "Dorothy");

    assertTrue(names.contains("Nobody")); // control flow will never get here
    fail();
  }
}