/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.sexpr;

import edu.rice.util.Log;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class SimpleValueTest {
  @Test
  public void basicTest() throws Exception {
    Value value = Parser.parseSexpr("(add (multiply 3 4) 5)").get();
    assertTrue(value instanceof Value.Sexpr);
    Value.Sexpr sexpr = value.asSexpr();
    assertEquals("add", sexpr.getList().head().asWord().get());
    assertEquals("multiply", sexpr.getList().tail().head().asSexpr().getList().head().asWord().get());
    assertEquals("3", sexpr.getList().tail().head().asSexpr().getList().tail().head().asWord().get());
    assertEquals("4", sexpr.getList().tail().head().asSexpr().getList().tail().tail().head().asWord().get());
    assertEquals("5", sexpr.nth(2).get().asWord().get()); // let's test nth as well
    assertTrue(sexpr.nth(3).isNone());

    // Now some tests to see whether parser accepts/rejects different constructs
    assertTrue(Parser.parseSexpr("(() ()()(()))").isSome());
    assertTrue(Parser.parseSexpr("word").isSome());

    Log.i("SexprParser", "testing the parser with erroneous inputs; expect errors in the log!");
    assertFalse(Parser.parseSexpr("(() ()()(())) word").isSome());
    assertFalse(Parser.parseSexpr("word (() ()()(()))").isSome());
    assertFalse(Parser.parseSexpr("(() ()()(())").isSome());
  }
}
