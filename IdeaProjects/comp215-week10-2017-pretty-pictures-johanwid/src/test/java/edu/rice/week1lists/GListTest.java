/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week1lists;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class GListTest {
  @Test
  public void testBasics() throws Exception {
    GList<String> emptyList = GList.makeEmpty();
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
    GList<String> emptyList = GList.makeEmpty();
    assertTrue(emptyList.empty());
    assertEquals("", emptyList.toString());

    GList<String> helloRice = emptyList.add("Hello").add("Rice").add("Owls");
    assertEquals("Owls Rice Hello", helloRice.toString());
  }

  @Test(expected = NoSuchElementException.class)
  public void testEmptyHead() throws Exception {
    assertEquals("ignored", GList.<String>makeEmpty().head()); // should throw an exception
    fail("Exception should have been thrown!");
  }

  @Test
  public void testContains() throws Exception {
    GList<String> emptyList = GList.makeEmpty();
    GList<String> helloRice = emptyList.add("Hello").add("Rice").add("Owls");

    assertTrue(helloRice.contains("Rice"));
    assertTrue(helloRice.contains("Hello"));
    assertTrue(helloRice.contains("Owls"));
    assertFalse(helloRice.contains("Aggies"));

    assertFalse(emptyList.contains("Anybody"));
  }
}