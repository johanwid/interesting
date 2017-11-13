/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.util;

import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.util.Strings.stringToUnixLinebreaks;
import static org.junit.Assert.assertEquals;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class StringsTest {
  @Test
  public void testStringToUnixLinebreaks() throws Exception {
    assertEquals("hello world", stringToUnixLinebreaks("hello world"));
    assertEquals("hello\nworld\n", stringToUnixLinebreaks("hello\nworld\n"));
    assertEquals("hello\nworld\n", stringToUnixLinebreaks("hello\r\nworld\r"));
    assertEquals("hello\nworld\n", stringToUnixLinebreaks("hello\r\nworld\r\n"));
    assertEquals("hello\\r\\nworld\n", stringToUnixLinebreaks("hello\\r\\nworld\r\n"));
  }
}