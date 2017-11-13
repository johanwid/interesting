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

import static org.junit.Assert.*;

public class MListTest {

  @Test
  public void testBasics() throws Exception {
    MList ml = new MList();
    assertTrue(ml.empty());
    ml.add("Hello");
    ml.add("Rice");
    ml.add("Owls");
    assertFalse(ml.empty());
    assertTrue(ml.contains("Rice"));
    assertFalse(ml.contains("Harvard"));
    assertEquals("Owls", ml.getHead());
    assertEquals("Rice", ml.getHead());
    assertEquals("Hello", ml.getHead());
  }
}