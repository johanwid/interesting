/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.regex;

import org.intellij.lang.annotations.Pattern;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.junit.Assert.assertEquals;

/**
 * This test exercises the @Language and @Pattern annotations that IntelliJ supports to warn you
 * if your regex is improperly formatted and to require that a String input to a function matches
 * a given pattern.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class PatternAnnotationTest {
  // Notice that we don't need to actually declare that lettersPattern is a regular expression?
  // IntelliJ figures it out because it's used in a @Pattern declaration later on.
  private static final String lettersPattern = "[a-z ]*";

  interface Internal {
    // The @Pattern annotation doesn't work here, see: https://youtrack.jetbrains.com/issue/IDEA-171173
    static String toUpper2(// @Pattern(lettersPattern)
                               String input) {
      return input.toUpperCase();
    }
  }

  public static String toUpper1(@Pattern(lettersPattern) String input) {
    return input.toUpperCase();
  }

  private static final String START = "hello world";
  private static final String INVALID = "Hello world";
  private static final String UPPER = "HELLO WORLD";

  @Test
  public void testPatterns() throws Exception {
    assertEquals(UPPER, toUpper1(START));
    assertEquals(UPPER, Internal.toUpper2(START));
  }


  // This test is actually testing whether the assertions are checked at runtime,
  // versus statically. There's a long discussion of this in NotNullTest.
  @Ignore
  @Test(expected = AssertionError.class)
  public void testPatternErrors() throws Exception {
    // You can see that "INVALID" is highlighted here as a warning because it doesn't
    // fit the required @Pattern.
    assertEquals(UPPER, toUpper1(INVALID));
  }

  @Ignore
  @Test(expected = AssertionError.class)
  public void testPatternErrors2() throws Exception {
    // You should also see that "INVALID" is highlighted here, but we disabled the relevant
    // @Pattern for toUpper2() because of a bug in the annotation processor.
    assertEquals(UPPER, Internal.toUpper2(INVALID));
  }
}
