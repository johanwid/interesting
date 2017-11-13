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

import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.util.Log;
import org.intellij.lang.annotations.Language;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.regex.RegexScanner.scanPatterns;
import static edu.rice.regex.RegexScannerTest.SimpleTokenPatterns.*;
import static org.junit.Assert.assertEquals;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class RegexScannerTest {
  private static final String TAG = "RegexScannerTest";

  @Test
  public void testGetNamedGroupMatches() throws Exception {
    IList<Token<SimpleTokenPatterns>> results =
        scanPatterns("{ hello = fun; world=aw3some; }",
            SimpleTokenPatterns.class,
            new Token<>(FAIL, ""))
        .filter(token -> token.type != WHITESPACE);

    IList<Token<SimpleTokenPatterns>> expectedResult = List.of(
        new Token<>(OPENCURLY, "{"),
        new Token<>(WORD, "hello"),
        new Token<>(EQUALS, "="),
        new Token<>(WORD, "fun"),
        new Token<>(SEMICOLON, ";"),
        new Token<>(WORD, "world"),
        new Token<>(EQUALS, "="),
        new Token<>(WORD, "aw3some"),
        new Token<>(SEMICOLON, ";"),
        new Token<>(CLOSECURLY, "}"));

    assertEquals(expectedResult, results);
  }

  @Test
  public void testGetNamedGroupMatchesWithFail() throws Exception {
    Log.i(TAG, "Testing scanning of tokens that don't match the patterns: expect logs of failures!");
    IList<Token<SimpleTokenPatterns>> results =
        scanPatterns("{ hello = fun; !!! world=aw3some; }",
            SimpleTokenPatterns.class,
            new Token<>(FAIL, ""))
        .filter(token -> token.type != WHITESPACE);

    IList<Token<SimpleTokenPatterns>> expectedResult = List.of(
        new Token<>(OPENCURLY, "{"),
        new Token<>(WORD, "hello"),
        new Token<>(EQUALS, "="),
        new Token<>(WORD, "fun"),
        new Token<>(SEMICOLON, ";"),
        new Token<>(FAIL, ""));

    assertEquals(expectedResult, results);
  }

  enum SimpleTokenPatterns implements TokenPatterns {
    OPENCURLY("\\{"),
    CLOSECURLY("}"),
    WHITESPACE("\\s+"),
    EQUALS("="),
    SEMICOLON(";"),
    WORD("\\p{Alnum}+"),
    FAIL("");

    public final String pattern;

    SimpleTokenPatterns(@Language("RegExp") String pattern) {
      this.pattern = pattern;
    }

    @Override
    public String pattern() {
      return pattern;
    }
  }
}