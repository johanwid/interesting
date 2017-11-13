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

import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.regex.Token;
import edu.rice.sexpr.Scanner.SexprPatterns;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.regex.Pattern;

import static edu.rice.sexpr.Scanner.SexprPatterns.*;
import static edu.rice.sexpr.Scanner.scanSexpr;
import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class SimpleScannerTest {

  @Test
  public void testSimpleScannerExamples() throws Exception {
    final IList<Token<SexprPatterns>> tokenList = scanSexpr("(add (multiply 3 4) 5)");
//        System.out.println(tokenList.toString());

    final IList<Token<SexprPatterns>> expectedTokens = List.of(
        new Token<>(OPEN, "("),
        new Token<>(WORD, "add"),
        new Token<>(OPEN, "("),
        new Token<>(WORD, "multiply"),
        new Token<>(WORD, "3"),
        new Token<>(WORD, "4"),
        new Token<>(CLOSE, ")"),
        new Token<>(WORD, "5"),
        new Token<>(CLOSE, ")"));

    assertEquals(expectedTokens, tokenList);

    //Let's also make sure that we deal with empty-sexprs properly

    final IList<Token<SexprPatterns>> tokenList2 = scanSexpr("(() hello)");
    final IList<Token<SexprPatterns>> expectedTokens2 = List.of(
        new Token<>(OPEN, "("),
        new Token<>(OPEN, "("),
        new Token<>(CLOSE, ")"),
        new Token<>(WORD, "hello"),
        new Token<>(CLOSE, ")"));

    assertEquals(expectedTokens2, tokenList2);
  }

  @Test
  public void testBasicRegexs() throws Exception {
    final Pattern openPattern = Pattern.compile(OPEN.pattern);
    final Pattern closePattern = Pattern.compile(CLOSE.pattern);
    final Pattern wordPattern = Pattern.compile(WORD.pattern);

    assertFalse(openPattern.matcher("[").matches());
    assertFalse(openPattern.matcher(")").matches());
    assertTrue(openPattern.matcher("(").matches());

    assertFalse(closePattern.matcher("]").matches());
    assertFalse(closePattern.matcher("(").matches());
    assertTrue(closePattern.matcher(")").matches());

    assertTrue(wordPattern.matcher("hello").matches());
    assertFalse(closePattern.matcher("27").matches());
  }
}