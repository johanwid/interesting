/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week2lists;

import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.junit.Assert.*;

/**
 * These unit tests are meant to exercise your project for week2. If you pass these tests,
 * there's a good chance your code works. However, we encourage you to add additional tests.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class Week2ProjectTest {
  @Test
  public void testConcat() throws Exception {
    GList<Integer> empty = GList.makeEmpty();

    GList<Integer> list1 = empty.add(1).add(2).add(3).add(4).add(5);
    GList<Integer> list2 = list1.concat(list1);
    assertEquals("5 4 3 2 1 5 4 3 2 1", list2.toString());

    // now for a bunch of tests involving empty lists
    GList<Integer> list3 = empty.concat(empty);
    assertEquals("", list3.toString());

    GList<Integer> list4 = list1.concat(empty);
    GList<Integer> list5 = empty.concat(list1);
    assertEquals(list1.toString(), list4.toString());
    assertEquals(list1.toString(), list5.toString());

    // lastly, let's double check that we're getting ordering correct
    GList<Integer> list6 = empty.add(6).add(7);
    GList<Integer> list7 = list6.concat(list1);
    assertEquals("7 6 5 4 3 2 1", list7.toString());
  }

  @Test
  public void testLimit() throws Exception {
    GList<Integer> empty = GList.makeEmpty();

    GList<Integer> list1 = empty.add(1).add(2).add(3).add(4).add(5);
    assertEquals(5, list1.length()); // sanity test

    GList<Integer> firstThree = list1.limit(3);
    assertEquals(3, firstThree.length());
    assertEquals("5 4 3", firstThree.toString());

    // now for some crazier cases
    GList<Integer> firstTen = list1.limit(10);
    assertEquals(list1.toString(), firstTen.toString());

    GList<Integer> shouldBeEmpty = list1.limit(0);
    assertEquals("", shouldBeEmpty.toString());
    assertTrue(shouldBeEmpty.empty());

    GList<Integer> shouldAlsoBeEmpty = list1.limit(-3);
    assertTrue(shouldAlsoBeEmpty.empty());
  }

  @Test
  public void testRangeInt() throws Exception {
    GList<Integer> firstTen = GList.rangeInt(1, 10, 1);
    assertEquals("1 2 3 4 5 6 7 8 9 10", firstTen.toString());

    GList<Integer> odds = GList.rangeInt(1, 10, 2);
    assertEquals("1 3 5 7 9", odds.toString());

    GList<Integer> backwards = GList.rangeInt(10, 1, -2);
    assertEquals("10 8 6 4 2", backwards.toString());

    GList<Integer> justOne = GList.rangeInt(10, 5, 1);
    assertEquals("10", justOne.toString());
  }

  @Test
  public void testMinimum() throws Exception {
    GList<String> emptyList = GList.makeEmpty();
    GList<String> stringList = emptyList.add("Bob").add("Alice").add("Dorothy").add("Charlie");
    assertEquals("Alice", GList.minimum("Nobody", stringList));
    assertEquals("Nobody", GList.minimum("Nobody", emptyList));
  }

  @Test
  public void testMaximum() throws Exception {
    GList<String> emptyList = GList.makeEmpty();
    GList<String> stringList = emptyList.add("Bob").add("Alice").add("Dorothy").add("Charlie");
    assertEquals("Dorothy", GList.maximum("Nobody", stringList));
    assertEquals("Nobody", GList.maximum("Nobody", emptyList));
  }

  @Test
  public void testAverage() throws Exception {
    GList<Double> emptyList = GList.makeEmpty();
    GList<Double> list = emptyList.add(10.0).add(0.0).add(2.0).add(8.0);

    // we have to cast from double to Double because assertEquals() doesn't want primitive types
    assertEquals((Double) 5.0, (Double) GList.average(0.0, list));
    assertEquals((Double) 0.0, (Double) GList.average(0.0, emptyList));
  }
}
