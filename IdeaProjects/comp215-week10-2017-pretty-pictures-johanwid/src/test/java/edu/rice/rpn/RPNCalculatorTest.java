/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.rpn;

import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.regex.Token;
import edu.rice.util.Option;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.junit.Assert.assertEquals;

@CheckReturnValue
@ParametersAreNonnullByDefault
public class RPNCalculatorTest {
  private static final String TAG = "RPNCalculatorTest";

  @Test
  public void testScan() throws Exception {
    IList<Token<RPNCalculator.RPNTokenPatterns>> scan1 = RPNCalculator.scan("1 2 + 3 *");
    IList<Token<RPNCalculator.RPNTokenPatterns>> expectedResult1 = List.of(
        new Token<>(RPNCalculator.RPNTokenPatterns.NUMBER, "1"),
        new Token<>(RPNCalculator.RPNTokenPatterns.NUMBER, "2"),
        new Token<>(RPNCalculator.RPNTokenPatterns.PLUS, "+"),
        new Token<>(RPNCalculator.RPNTokenPatterns.NUMBER, "3"),
        new Token<>(RPNCalculator.RPNTokenPatterns.TIMES, "*"));

    assertEquals(expectedResult1, scan1);

    IList<Token<RPNCalculator.RPNTokenPatterns>> scan2 = RPNCalculator.scan("1 2 + 3 yuck");
    IList<Token<RPNCalculator.RPNTokenPatterns>> expectedResult2 = List.of(
        new Token<>(RPNCalculator.RPNTokenPatterns.NUMBER, "1"),
        new Token<>(RPNCalculator.RPNTokenPatterns.NUMBER, "2"),
        new Token<>(RPNCalculator.RPNTokenPatterns.PLUS, "+"),
        new Token<>(RPNCalculator.RPNTokenPatterns.NUMBER, "3"),
        new Token<>(RPNCalculator.RPNTokenPatterns.FAIL, ""));

    assertEquals(expectedResult2, scan2);
  }

  @Test
  public void testCalc() throws Exception {
    RPNCalculator calculator = new RPNCalculator();
    assertEquals("9.0", calculator.calc("1 2 + 3 *"));
    assertEquals("81.0", calculator.calc("1 2 + 3 * dup *"));

    // those last two values should still be on the stack
    assertEquals("81.0", calculator.calc("="));
    assertEquals("9.0", calculator.calc("drop"));
    assertEquals("Empty stack", calculator.calc("drop"));
    assertEquals("Error!", calculator.calc("drop"));

    // let's test the non-commutative operators as well
    assertEquals("1.0", calculator.calc("3 2 -"));
    assertEquals("2.0", calculator.calc("4 2 /"));

    // make sure that division by zero fails correctly
    assertEquals("Error!", calculator.calc("4 0 /"));

    // and make sure swap works
    assertEquals("1.0", calculator.calc("2 3 swap -"));

    // and clear
    assertEquals("Empty stack", calculator.calc("clear"));

    // and errors shouldn't change anything on the stack
    assertEquals("2.0", calculator.calc("1 2"));
    assertEquals("Error!", calculator.calc("oops"));
    assertEquals("2.0", calculator.calc("="));

    // fun property: after an error, subsequent operations won't fix it; it will stay an error
    assertEquals("Error!", calculator.calc("clear 2 / 2 3 +"));

    // another fun property: clear, after an error, yields a clean stack and computation can continue!
    assertEquals("5.0", calculator.calc("clear 2 / clear 2 3 +"));

  }

  @Test
  public void testOperatorComposition() throws Exception {

    // f(x) = (x + 10) * 27
    final RPNCalculator.CalcOp push27 = RPNCalculator.numberPusher(27);
    final RPNCalculator.CalcOp push10 = RPNCalculator.numberPusher(10);
    final RPNCalculator.CalcOp add = RPNCalculator::add;
    final RPNCalculator.CalcOp mult = RPNCalculator::multiply;

    final RPNCalculator.CalcOp f =
        push10.andThen(add).andThen(push27).andThen(mult);

    final Option<IList<Double>> expectedResult = Option.some(List.of((3.0 + 10.0) * 27.0));

    // first, let's verify that our understanding of RPNCalculator.oget() is correct
    assertEquals(expectedResult, RPNCalculator.of((3.0 + 10.0) * 27.0).oget());

    // okay, now let's start applying our functions
    assertEquals(expectedResult, f.apply(RPNCalculator.of(3.0)).oget());

    // here's the same thing written more concisely!
    final RPNCalculator.CalcOp f2 =
        RPNCalculator.numberPusher(10)
            .andThen(RPNCalculator::add)
            .andThen(RPNCalculator.numberPusher(27))
            .andThen(RPNCalculator::multiply);
    assertEquals(expectedResult, f2.apply(RPNCalculator.of(3.0)).oget());

    // this time, let's show off how cool the logWrap method can be.
    final RPNCalculator.CalcOp f3 =
        push10.logWrap(TAG)
            .andThen(add.logWrap(TAG))
            .andThen(push27.logWrap(TAG))
            .andThen(mult.logWrap(TAG));
    assertEquals(expectedResult, f3.apply(RPNCalculator.of(3.0)).oget());

    // lastly, let's push all the numbers first, then do the operations, proving to ourselves that
    // we understand the order in which things are landing on the stack
    assertEquals(expectedResult, mult.apply(add.apply(RPNCalculator.of(3.0, 10.0, 27.0))).oget());

    // another variant using operator composition
    assertEquals(expectedResult, add.andThen(mult).apply(RPNCalculator.of(3.0, 10.0, 27.0)).oget());
  }
}