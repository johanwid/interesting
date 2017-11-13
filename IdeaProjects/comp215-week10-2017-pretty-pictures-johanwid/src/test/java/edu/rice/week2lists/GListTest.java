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
import java.util.NoSuchElementException;

import static edu.rice.week2lists.GList.makeEmpty;
import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class GListTest {
  @Test
  public void testBasics() throws Exception {
    GList<String> emptyList = makeEmpty();
    assertTrue(emptyList.empty());
    assertEquals(0, emptyList.length());
    assertFalse(emptyList.add("Hello").empty());
    assertTrue(emptyList.add("Hello").tail().empty());

    GList<String> helloRice = emptyList.add("Hello").add("Rice").add("Owls");
    assertEquals(3, helloRice.length());
    assertEquals("Owls", helloRice.head());
    assertEquals("Rice", helloRice.tail().head());
    assertEquals("Hello", helloRice.tail().tail().head());
  }

  @Test
  public void testToString() throws Exception {
    GList<String> emptyList = makeEmpty();
    assertTrue(emptyList.empty());
    assertEquals("", emptyList.toString());

    GList<String> helloRice = emptyList.add("Hello").add("Rice").add("Owls");
    assertEquals("Owls Rice Hello", helloRice.toString());
  }

  @Test(expected = NoSuchElementException.class)
  public void testEmptyHead() throws Exception {
    GList<String> emptyList = makeEmpty();

    assertEquals("fail", emptyList.head()); // we're expecting this to throw an exception
    fail("Exception should have been thrown!");
  }

  @Test
  public void testContains() throws Exception {
    GList<String> emptyList = makeEmpty();
    GList<String> helloRice = emptyList.add("Hello").add("Rice").add("Owls");

    assertTrue(helloRice.contains("Rice"));
    assertTrue(helloRice.contains("Hello"));
    assertTrue(helloRice.contains("Owls"));
    assertFalse(helloRice.contains("Aggies"));

    assertFalse(emptyList.contains("Anybody"));
  }

  @Test
  public void testFilter() throws Exception {
    GList<Integer> emptyList = makeEmpty();
    GList<Integer> numbers = emptyList.add(1).add(2).add(3).add(4).add(5).add(6);
    GList<Integer> evens = numbers.filter(x -> (x % 2) == 0);
    assertEquals(3, evens.length());

    // this shows how you can declare a type parameter inline
    GList<Integer> alsoEven = GList.<Integer>makeEmpty().add(2).add(4).add(6);
    assertEquals(evens, alsoEven);
  }

  @Test
  public void testLexicalScope() throws Exception {
    GList<String> emptyList = makeEmpty();
    GList<String> favoriteMajors = emptyList.add("COMP").add("ELEC").add("FWIS");

    GList<String> manyClasses = emptyList.add("COMP140").add("COMP182").add("XYZY100").add("ELEC220").add("POLI450");

    GList<String> favoriteClasses =
        manyClasses.filter(
            c -> !favoriteMajors
                .filter(c::startsWith)
                .empty());

    assertEquals("ELEC220", favoriteClasses.head());
    assertEquals("COMP182", favoriteClasses.tail().head());
    assertEquals("COMP140", favoriteClasses.tail().tail().head());
    assertEquals(3, favoriteClasses.length());
  }

  @Test
  public void testFoldlRollingAccAvg() throws Exception {
    GList<Double> emptyList = makeEmpty();
    GList<Double> numbers = emptyList.add(0.0).add(4.0).add(8.0);

    //solution to left rolling accumulator average
    assertEquals(3.0,
        numbers.foldl(numbers.empty() ? 0.0 : numbers.head(), (x, y) -> (x + y) / 2),
        0.01);
  }
}