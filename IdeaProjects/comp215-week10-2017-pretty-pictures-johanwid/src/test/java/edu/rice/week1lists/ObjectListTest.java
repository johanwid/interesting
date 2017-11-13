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

public class ObjectListTest {
  @Test
  public void testBasics() throws Exception {
    ObjectList emptyList = ObjectList.makeEmpty();
    assertTrue(emptyList.empty());
    assertEquals(0, emptyList.length());
    assertFalse(emptyList.add("Hello").empty());
    assertTrue(emptyList.add("Hello").tail().empty());

    ObjectList helloRice = emptyList.add("Hello").add("Rice").add("Owls");
    assertEquals(3, helloRice.length());
    assertEquals("Owls", helloRice.head());
    assertEquals("Rice", helloRice.tail().head());
    assertEquals("Hello", helloRice.tail().tail().head());
  }

  @Test
  public void testToString() throws Exception {
    ObjectList emptyList = ObjectList.makeEmpty();
    assertEquals("", emptyList.toString());

    ObjectList helloRice = emptyList.add("Hello").add("Rice").add("Owls");
    assertEquals("Owls Rice Hello", helloRice.toString());
  }

  @Test(expected = NoSuchElementException.class)
  public void testEmptyHead() throws Exception {
    ObjectList emptyList = ObjectList.makeEmpty();

    String noHead = (String) emptyList.head(); // we're expecting this to throw an exception (noted in the @Test above)
    assertEquals("fail", noHead);
    fail("Exception should have been thrown!");
  }

  @Test
  public void testContains() throws Exception {
    ObjectList emptyList = ObjectList.makeEmpty();
    ObjectList helloRice = emptyList.add("Hello").add("Rice").add("Owls");

    assertTrue(helloRice.contains("Rice"));
    assertTrue(helloRice.contains("Hello"));
    assertTrue(helloRice.contains("Owls"));
    assertFalse(helloRice.contains("Aggies"));

    assertFalse(emptyList.contains("Anybody"));
  }
}