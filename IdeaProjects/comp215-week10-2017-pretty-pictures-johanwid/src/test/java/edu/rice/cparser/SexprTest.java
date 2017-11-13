/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.cparser;

import edu.rice.list.List;
import edu.rice.sexpr.Scanner.SexprPatterns;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.cparser.Expression.exprTerminal;
import static edu.rice.cparser.Result.resultError;
import static edu.rice.cparser.Result.resultOk;
import static edu.rice.cparser.SExpression.parseSexpr;
import static edu.rice.sexpr.Scanner.SexprPatterns.WORD;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class SexprTest {
  final Expression<SexprPatterns> aliceTerminal = exprTerminal(WORD, "alice");
  final Expression<SexprPatterns> bobTerminal = exprTerminal(WORD, "bob");
  final Expression<SexprPatterns> charlieTerminal = exprTerminal(WORD, "charlie");

  @Test
  public void testBasics() throws Exception {
    assertEquals(resultError(), parseSexpr("(alice bob charlie) alice"));
    assertEquals(resultError(), parseSexpr("alice"));
    assertEquals(resultError(), parseSexpr("("));
    assertEquals(resultError(), parseSexpr("(()"));
    assertEquals(resultError(), parseSexpr("())"));

    assertEquals(
        resultOk(SExpression.make(List.of(aliceTerminal, bobTerminal, charlieTerminal)), List.makeEmpty()),
        parseSexpr("(alice bob charlie)"));
  }

  @Test
  public void testDeeper() throws Exception {
    assertEquals(
        resultOk(SExpression.make(List.of(aliceTerminal, bobTerminal, SExpression.make(List.of(charlieTerminal)))),
            List.makeEmpty()),
        parseSexpr("(alice bob (charlie))"));
  }

  // for testToValue(), we're going to run a whole bunch of tests which are independent of one another. We want them
  // to continue running even if one test fails, so we're going to use JUnit's ErrorCollector framework again. We used
  // this before when testing trees (see the tests for edu.rice.tree.TreeSuite, TreeTest, and TreapTest).

  @Rule
  public final ErrorCollector collector = new ErrorCollector();

  @Test
  public void testToValue() throws Exception {

    // we're going to run the original s-expression parser and the new one and verify they return the same answer
    List.of(
        "()",
        "(  alice)",
        "(alice    bob  ( charlie ) dorothy)")

        .foreach(str ->
            collector.checkThat(str,
                edu.rice.sexpr.Parser.parseSexpr(str).get(),
                equalTo(((SExpression) parseSexpr(str).asOk().production).toValue())));

    // errors are indicated differently by each parser, but they should fail on all the same things
    List.of(
        "",
        "( ) alice)",
        "( ) alice",
        "(",
        "((()")

        .foreach(str -> {
          collector.checkThat(str, edu.rice.sexpr.Parser.parseSexpr(str).isNone(), equalTo(Boolean.TRUE));
          collector.checkThat(str, parseSexpr(str).isError(), equalTo(Boolean.TRUE));
        });
  }
}
