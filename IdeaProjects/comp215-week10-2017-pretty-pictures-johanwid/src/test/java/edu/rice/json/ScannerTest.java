/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.json;

import edu.rice.io.Files;
import edu.rice.json.Scanner.JsonPatterns;
import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.regex.Token;
import edu.rice.util.Log;
import org.intellij.lang.annotations.Language;
import org.junit.Test;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.regex.Pattern;

import static edu.rice.json.Scanner.JsonPatterns.*;
import static edu.rice.json.Scanner.scanJson;
import static edu.rice.util.Strings.stringToUnixLinebreaks;
import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
public class ScannerTest {
  private static final String TAG = "ScannerTest";

  // This file is an example of a completely legal JSON expression.
  private static final @Language("JSON") String bigJson =
      stringToUnixLinebreaks(Files.readResource("bigJson.json").getOrElse(""));

  // This file has the sort of thing shows up sometimes in JavaScript programs,
  // but is *not* legal JSON and should be rejected.
  private static final @Language("JSON") String noQuotesJson =
      stringToUnixLinebreaks(Files.readResource("bigJsonMalformed.notjson").getOrElse(""));

  @Test
  public void testJsonExamples() throws Exception {
    assertTrue(!bigJson.equals("")); // make sure the file read operations succeeded
    assertTrue(!noQuotesJson.equals(""));

    final IList<Token<JsonPatterns>> tokenList = scanJson(bigJson);

    final IList<Token<JsonPatterns>> expectedTokens = List.of(
        new Token<>(OPENCURLY, "{"),
        new Token<>(STRING, "itemCount"),
        new Token<>(COLON, ":"),
        new Token<>(NUMBER, "2"),
        new Token<>(COMMA, ","),
        new Token<>(STRING, "subtotal"),
        new Token<>(COLON, ":"),
        new Token<>(STRING, "$15.50"));

    // there are more tokens after this, so we're only testing that the first ones are what we expect
    assertEquals(expectedTokens, tokenList.limit(expectedTokens.length()));

    // now, switch to the input version that's missing quotation marks. This should cause the
    // lexer to fail to find a token on the first non-quoted string.

    Log.i(TAG, "Testing scanning of non-compliant JSON tokens: expect logs of failures!");
    final IList<Token<JsonPatterns>> tokenListNoQuotes = scanJson(noQuotesJson);
    final IList<Token<JsonPatterns>> expectedTokensNoQuotes = List.of(
        new Token<>(OPENCURLY, "{"),
        new Token<>(FAIL, ""));

    assertEquals(List.of(new Token<>(FAIL, "")), scanJson(x("####")));

    assertEquals(expectedTokensNoQuotes, tokenListNoQuotes);

    final IList<Token<JsonPatterns>> uglyNumbersExpected = List.of(
        new Token<>(NUMBER, "2"),
        new Token<>(FAIL, ""));

    final IList<Token<JsonPatterns>> uglyNumbers = scanJson(x("2 0000 33 11"));
    assertEquals(uglyNumbersExpected, uglyNumbers);

    final IList<Token<JsonPatterns>> nullnullnullList = scanJson(x("nullnullnull"));
    assertEquals(List.of(new Token<>(FAIL, "")), nullnullnullList);
  }

  @Test
  public void testBasicRegexs() throws Exception {
    final Pattern stringPattern = Pattern.compile(STRING.pattern);

    final Pattern numberPattern = Pattern.compile(NUMBER.pattern);
    final Pattern truePattern = Pattern.compile(TRUE.pattern);
    final Pattern falsePattern = Pattern.compile(FALSE.pattern);
    final Pattern nullPattern = Pattern.compile(NULL.pattern);
    final Pattern openCurlyPattern = Pattern.compile(OPENCURLY.pattern);

    assertFalse(stringPattern.matcher("hello").matches());
    assertTrue(stringPattern.matcher("\"hello\"").matches());
    assertTrue(stringPattern.matcher("\"hello, world\"").matches());
    assertTrue(stringPattern.matcher("\"hello, world\\n\"").matches());
    assertTrue(stringPattern.matcher("\"hello, \\\"world\\\"\\n\"").matches());
    assertFalse(numberPattern.matcher("hello").matches());
    assertTrue(numberPattern.matcher("93.2").matches());
    assertTrue(numberPattern.matcher("93").matches());
    assertTrue(numberPattern.matcher("-93").matches());
    assertTrue(numberPattern.matcher("-93e24").matches());
    assertTrue(numberPattern.matcher("-0.2e24").matches());
    assertFalse(numberPattern.matcher("-.2e24").matches());
    assertFalse(truePattern.matcher("Hello").matches());
    assertFalse(truePattern.matcher("false").matches());
    assertTrue(truePattern.matcher("true").matches());
    assertFalse(falsePattern.matcher("Hello").matches());
    assertFalse(falsePattern.matcher("true").matches());
    assertTrue(falsePattern.matcher("false").matches());
    assertFalse(nullPattern.matcher("Hello").matches());
    assertFalse(nullPattern.matcher("true").matches());
    assertTrue(nullPattern.matcher("null").matches());
    assertFalse(openCurlyPattern.matcher("Hello").matches());
    assertFalse(openCurlyPattern.matcher("t{ue").matches());
    assertTrue(openCurlyPattern.matcher("{").matches());
  }

  /**
   * This no-op function is here solely to defeat the regex checks specifically for cases where we're deliberately
   * feeding malformed or weird data and we're checking that the scanner works properly. We don't want to have the
   * static checker yelling at us.
   */
  private static String x(String input) {
    return input;
  }
}
