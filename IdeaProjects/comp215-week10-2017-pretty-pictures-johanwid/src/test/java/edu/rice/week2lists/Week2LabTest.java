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
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class Week2LabTest {

  @Test
  public void testNonNegativeNumbers() throws Exception {
    // Lab assignment, part 1: rewrite this predicate so the unit tests below succeed.
    Predicate<Integer> pred = x -> x >= 0;

    GList<Integer> empty = GList.makeEmpty();
    GList<Integer> list1 = empty.add(-3).add(-1); // test when list contains only negative numbers
    GList<Integer> list2 = empty.add(0); // test when list contains 0
    GList<Integer> list3 = empty.add(5).add(-1).add(0).add(-19).add(50); // test when list contains positive numbers

    assertEquals("",list1.filter(pred).toString());
    assertEquals("0",list2.filter(pred).toString());
    assertEquals("50 0 5",list3.filter(pred).toString());
  }

  @Test
  public void testStringLengthBetweenFiveAndTen() throws Exception {
    // Lab assignment, part 2: rewrite this predicate so the unit tests below succeed. You may wish to
    // consult the Javadoc for java.lang.String:
    // https://docs.oracle.com/javase/8/docs/api/java/lang/String.html
    Predicate<String> pred = x -> (x.length() >= 5) && (x.length() <= 10);

    GList<String> empty = GList.makeEmpty();
    GList<String> list1 = empty.add("").add("hat").add("apples"); // test when some string lengths are less than 5
    GList<String> list2 = empty.add("oranges").add("caterpillar"); // test when some strings lengths are greater than 10

    assertEquals("apples", list1.filter(pred).toString());
    assertEquals("oranges", list2.filter(pred).toString());

    GList<String> list3 = empty.add("helicopter").add("fruit").add("pear"); // test string length of 5 and 10
    assertEquals("fruit helicopter", list3.filter(pred).toString());
  }

  @Test
  public void testLetterCappearsAnywhereBeforeLetterA() throws Exception {
    // Lab assignment, part 3: rewrite this predicate so the unit tests below succeed. You may wish to
    // consult the Javadoc for java.lang.String:
    // https://docs.oracle.com/javase/8/docs/api/java/lang/String.html
    Predicate<String> pred = x -> {
      String lx = x.toLowerCase();
      return lx.contains("c") && lx.contains("a") && (lx.indexOf("c") < lx.lastIndexOf("a"));
    };

    GList<String> empty = GList.makeEmpty();
    GList<String> list1 = empty.add("dog").add("horse").add("cat"); // test when c is immediately before a
    GList<String> list2 = empty.add("Dog").add("Horse").add("Cat").add("CAT").add("cAt"); // test capitalization

    assertEquals("cat", list1.filter(pred).toString());
    assertEquals("cAt CAT Cat", list2.filter(pred).toString());

    // test when c is both after one a and before another a
    GList<String> list3 = empty.add("a cactus").add("a tree").add("a plant");
    assertEquals("a cactus", list3.filter(pred).toString());

    // test when c is not immediately before a
    GList<String> list4 = empty.add("d o g").add("h o r s e").add("c a t");
    GList<String> list5 = empty.add("ADDER").add("ACCUMULATOR");
    GList<String> list6 = empty.add("accounting").add("accountable").add("calCium");

    assertEquals("c a t", list4.filter(pred).toString());
    assertEquals("ACCUMULATOR", list5.filter(pred).toString());
    assertEquals("calCium accountable",list6.filter(pred).toString());
  }
}
